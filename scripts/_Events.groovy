/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author ulcteam
 */

import org.apache.ivy.plugins.report.ReportOutputter
import org.apache.ivy.util.filter.ArtifactTypeFilter
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Pack200
import java.util.jar.Pack200.Unpacker
import java.util.zip.GZIPOutputStream
import grails.util.GrailsUtil
import grails.util.BuildSettingsHolder
import static groovy.io.FileType.ANY
import grails.util.GrailsNameUtils
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

includeTargets << grailsScript('_GrailsCompile')
includeTargets << grailsScript('_PluginDependencies')
includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")

if (!compilingUlcPlugin()) {
    includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")
}

eventSetClasspath = { cl ->
    if (compilingUlcPlugin()) return

    checkLicense()
}

eventCleanEnd = {
    ulcClientClassesDir = new File(grailsSettings.projectWorkDir, 'ulc-client-classes')
    if (ulcClientClassesDir.exists()) ant.delete(dir: ulcClientClassesDir, quiet: false, failonerror: false)
}

eventPackagePluginEnd = {pluginName ->
    if (!compilingUlcPlugin()) return

    ant.zip(destfile: pluginZip, filesonly: true, update: true) {
        zipfileset(dir: "${basedir}/docs", includes: '*.pdf', prefix: 'docs')
    }
}

eventCompileEnd = {
    if (compilingUlcPlugin()) return

    ulcClientClassesDir = new File(grailsSettings.projectWorkDir, 'ulc-client-classes')
    ulcClientClassesDirPath = ulcClientClassesDir.absolutePath
    ulcClientClassesDir.mkdirs()

    ulcClientClassesCommonDir = new File(ulcClientClassesDir, 'common')
    ulcClientClassesCommonDir.mkdirs()

    // collect al clinte-side libs
    def ulcClientLibs = collectClientJars()
    def classpathId = 'ulc.compile.classpath'
    ant.path(id: classpathId) {
        for (jar in ulcClientLibs) {
            pathelement(location: jar.absolutePath)
        }
    }

    // compile ${basedir}/src/ulc-client/common
    try {
        ant.javac(destdir: ulcClientClassesCommonDir,
                encoding: 'UTF-8',
                classpathref: classpathId,
                fork: true) {
            src(path: "${basedir}/src/ulc-client/common")
        }
    }
    catch (Exception e) {
        event("StatusFinal", ["Compilation error: ${e.message}"])
        exit(1)
    }

    // compile ${basedir}/src/ulc-client/${alias}
    forEachUlcApplication { alias, className ->
        def ulcAppClassesDir = new File(ulcClientClassesDir, alias)
        ulcAppClassesDir.mkdirs()
        try {
            ant.javac(destdir: ulcAppClassesDir,
                    encoding: 'UTF-8',
                    classpathref: classpathId,
                    fork: true) {
                src(path: "${basedir}/src/ulc-client/${alias}")
            }
        }
        catch (Exception e) {
            event("StatusFinal", ["Compilation error: ${e.message}"])
            exit(1)
        }
    }

    ant.copy(todir: classesDirPath) {
        fileset(dir: "${basedir}/src/java", includes: '**/*.properties, **/*.xml')
    }
}

collectClientJars = {
    def ulcClientlibs = []

    def resolveReport = grailsSettings.dependencyManager.resolveDependencies('runtime')
    resolveReport.output([[
            output: {report, cacheMgr, options ->
                output(report, cacheMgr, options, ulcClientlibs, resolveReport)
            },
            getName: {' UlcReportOutputter' }
    ] as ReportOutputter
    ] as ReportOutputter[], null, null)

    File baseClientLibDir = new File(basedir, 'lib/ulc-client')
    baseClientLibDir.eachFileRecurse(ANY) { file ->
        if (!file.name.endsWith('jar')) return
        if (!ulcClientlibs.grep {it.name == file.name}) ulcClientlibs << file
    }

    // TODO: collect $pluginHome/*/lib/ulc-client

    ulcClientlibs
}

eventCreateWarStart = { warName, stagingDir ->
    generateApplicationsFile("${stagingDir}/WEB-INF/resources")

    def ulcClientLibs = collectClientJars()

    // remove duplicate jar files
    ulcClientLibs.each { libFile ->
        ant.delete(file: new File("${stagingDir}/WEB-INF/lib/${libFile.name}"), quiet: true, failonerror: false)
    }

    // jar up license files
    ant.jar(destfile: "${stagingDir}/WEB-INF/lib/ulc-deployment-key.jar") {
        fileset(dir: ulcLicenseDir, includes: 'DEPLOYMENT-*.lic')
    }

    File tmpLibs = new File(grailsSettings.projectWorkDir, 'tmp/ulc-libs')
    File ulcLibsDir = new File("${stagingDir}/ulc-client-libs")
    ulcLibsDir.mkdirs()

    // copy local common client jar (if classes are available)
    if (ulcClientClassesCommonDir.list()) {
        File commonJar = new File("${stagingDir}/ulc-client-libs/${grailsAppName}-common-client.jar")
        ant.jar(destfile: commonJar) {
            fileset(dir: ulcClientClassesCommonDir, includes: '**/*.class')
        }
        copyPackAndSignFile(commonJar, tmpLibs)
    }

    // copy, sign and pack all client libs from deps
    ulcClientLibs.each { libFile ->
        copyPackAndSignFile(libFile, tmpLibs)
    }

    File ulcTemplatesDir = new File("${basedir}/ulc-templates")
    forEachUlcApplication { applicationAlias, applicationClassName ->
        def ulcAppClassesDir = new File(ulcClientClassesDir, applicationAlias)
        File appJarDir = new File("${stagingDir}/ulc-client-libs/${applicationAlias}")
        appJarDir.mkdirs()
        File appJar = new File(appJarDir, "application-${applicationAlias}-client.jar")
        ant.jar(destfile: appJar) {
            fileset(dir: ulcAppClassesDir, includes: '**/*.class')
        }
        copyPackAndSignFile(appJar, tmpLibs)

        def libs = []
        tmpLibs.eachFileMatch(~/.*\.jar/) { f ->
            libs << "        <jar href='ulc-client-libs/$f.name' main='false'/>"
        }
        new File(tmpLibs, applicationAlias).eachFileMatch(~/.*\.jar/) { f ->
            if (f.name == "application-${applicationAlias}-client.jar") {
                libs << "        <jar href='ulc-client-libs/${applicationAlias}/$f.name' main='true'/>"
            } else {
                libs << "        <jar href='ulc-client-libs/${applicationAlias}/$f.name' main='false'/>"
            }
        }

        File applicationJnlp = new File(ulcTemplatesDir, "${applicationAlias}.jnlp")
        if (!applicationJnlp.exists()) applicationJnlp = new File(ulcTemplatesDir, 'default.jnlp')
        File destApplicationJnlp = new File("${stagingDir}/${applicationAlias}.jnlp")
        ant.copy(file: applicationJnlp, tofile: destApplicationJnlp)
        ant.replace(file: destApplicationJnlp) {
            replacefilter(token: '@application.name@', value: applicationAlias[0].toUpperCase() + applicationAlias[1..-1])
            replacefilter(token: '@application.alias@', value: applicationAlias)
            replacefilter(token: '@application.launcher@', value: applicationClassName + 'JnlpLauncher')
            replacefilter(token: '@ulc.client.libs@', value: libs.join('\n'))
        }

        applicationJnlp = new File(ulcTemplatesDir, "${applicationAlias}-applet.jnlp")
        if (!applicationJnlp.exists()) applicationJnlp = new File(ulcTemplatesDir, 'default-applet.jnlp')
        destApplicationJnlp = new File("${stagingDir}/${applicationAlias}-applet.jnlp")
        ant.copy(file: applicationJnlp, tofile: destApplicationJnlp)
        ant.replace(file: destApplicationJnlp) {
            replacefilter(token: '@application.name@', value: applicationAlias[0].toUpperCase() + applicationAlias[1..-1])
            replacefilter(token: '@application.alias@', value: applicationAlias)
            replacefilter(token: '@application.launcher@', value: applicationClassName + 'AppletLauncher')
            replacefilter(token: '@ulc.client.libs@', value: libs.join('\n'))
        }
    }
    ant.copy(todir: ulcLibsDir, overwrite: true) {
        fileset(dir: tmpLibs, includes: '**/*')
    }
    ant.delete(dir: tmpLibs, failonerror: false, quiet: true)
}

collectAllDeps = { d, list, resolveReport ->
    list << resolveReport.getArtifactsReports(d.id).find {it.type == 'jar'}.localFile
    d.getDependencies('', 'runtime').each { d2 ->
        collectAllDeps(d2, list, resolveReport)
    }
}

output = { report, cacheMgr, options, list, resolveReport ->
    r = report.getConfigurationReport('runtime')
    List deps = []

    r.moduleIds.each { mid ->
        r.getNodes(mid).each { dep ->
            def mrid = dep.id
            if (deps.contains(mrid)) return

            if (mrid.name =~ /.*-client/) {
                deps << mrid
                collectAllDeps(dep, list, resolveReport)
            }
        }
    }
}

private boolean compilingUlcPlugin() {
    getPluginDirForName('ulc')?.file?.canonicalPath == basedir
}

void copyPackAndSignFile(File srcFile, File destinationDir) {
    ant.echo(message: "Copy and pack ${srcFile.absolutePath} to ${destinationDir}")
    try {
        File destFile = new File(destinationDir, srcFile.name)
        String ancestor = srcFile.parentFile.parentFile.name
        if (ancestor == 'ulc-client' || ancestor == 'ulc-client-libs') {
            destFile = new File(destinationDir, "${srcFile.parentFile.name}/${srcFile.name}")
        }

        destFile.parentFile.mkdirs()
        String packedJarFileName = "${destFile.absolutePath}.pack.gz"

        JarFile jar = new JarFile(srcFile)

        if (!jar.entries().any {!it.isDirectory()}) {
            return
        }

        Pack200.Packer packer = Pack200.newPacker()
        FileOutputStream fileOutputStream = new FileOutputStream(packedJarFileName)
        packer.pack(jar, fileOutputStream)
        fileOutputStream.close()

        Unpacker unpacker = Pack200.newUnpacker()
        File repackedJar = destFile
        File firstPackedFile = new File(packedJarFileName)
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(repackedJar))
        unpacker.unpack(firstPackedFile, jarOut)
        jarOut.close()

        signClientJar(destFile)

        GZIPOutputStream gzout = new GZIPOutputStream(new FileOutputStream(packedJarFileName))
        packer.pack(new JarFile(destFile), gzout)
        gzout.close()
    } catch (Exception e) {
        GrailsUtil.printSanitizedStackTrace e
        ant.fail("Catched Exception ${e.message} while handling  ${srcFile.absolutePath}.")
    }
}


private def signClientJar(File jarFile) {
    def buildConfig = BuildSettingsHolder.settings.config
    String propsFile = buildConfig.grails.codesigning.propsfile ?: 'ulc-signatures/signjar.properties'
    File signJarPropertiesFile = new File("${basedir}/${propsFile}")
    if (signJarPropertiesFile.exists()) {
        Properties signProperties = new Properties()
        signProperties.load new FileInputStream(signJarPropertiesFile)

        ant.signjar(
                jar: jarFile.absolutePath,
                destDir: jarFile.parentFile,
                alias: signProperties["signjar.alias"],
                keystore: "$basedir/ulc-signatures/${signProperties['signjar.keystore']}",
                storepass: signProperties["signjar.storepass"],
                keypass: signProperties["signjar.keypass"],
                lazy: true
        )
    } else {
        ant.echo(message: "No file ${propsFile} found. Tried ${signJarPropertiesFile.absolutePath}.")
    }
}
