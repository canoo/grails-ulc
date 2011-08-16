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
 * The general approach is to hook into the grails commands such that
 * the user can do 'grails run-app' and the likes without worrying about
 * the ulc artifacts.
 *
 * Uptodate checks are performed on the generated artifacts. Note that this
 * may lead to stale *.jnlp (and possibly more) files - no guarantees given.
 * Therefore, you should do 'grails clean'
 * before  producing production-ready war files.
 *
 * @author ulcteam
 */

import grails.util.BuildSettingsHolder
import grails.util.GrailsUtil
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Pack200
import java.util.jar.Pack200.Unpacker
import java.util.zip.GZIPOutputStream
import org.apache.ivy.plugins.report.ReportOutputter
import static groovy.io.FileType.ANY
import org.apache.ivy.core.report.ResolveReport

includeTargets << grailsScript('_GrailsCompile')
includeTargets << grailsScript('_PluginDependencies')
includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")


eventSetClasspath = { cl ->
    if (compilingUlcPlugin()) return

    checkLicense()
}

eventCleanEnd = {
    deleteClientClasses()
    deleteJNLPFiles()

}

private def deleteClientClasses() {
    ulcClientClassesDir = new File(grailsSettings.projectWorkDir, 'ulc-client-classes')
    ulcClientLibsDir = new File("$basedir/web-app", "ulc-client-libs")
    [ulcClientClassesDir, ulcClientLibsDir].each {dir ->
        ant.delete(dir: dir, quiet: true, failonerror: false)
    }
}

private def deleteJNLPFiles() {
    forEachUlcApplication { alias, className ->
        File applicationJnlpFile = new File("$basedir/web-app/${alias}-applet.jnlp")
        File appletJnlpFile = new File("$basedir/web-app/${alias}.jnlp")
        [applicationJnlpFile, appletJnlpFile].each {file ->
            ant.delete(file: file, quiet: true, failonerror: false)
        }
    }
}

eventPackagePluginEnd = {pluginName ->
    if (!compilingUlcPlugin()) return

    ant.zip(destfile: pluginZip, filesonly: true, update: true) {
        zipfileset(dir: "${basedir}/docs", includes: '*.pdf', prefix: 'docs')
    }
}


eventPackagingEnd = {
    if (compilingUlcPlugin()) return

    if (System.getProperty("runMode") == "runAppUlc") {
        println "Skipping prepareApplication."
        return
    }

    prepareApplication "$basedir/web-app"
}


eventCompileEnd = {
    if (compilingUlcPlugin()) return

    ulcClientClassesDir = new File(grailsSettings.projectWorkDir, 'ulc-client-classes')
    ulcClientClassesDirPath = ulcClientClassesDir.absolutePath
    ulcClientClassesDir.mkdirs()

    ulcClientClassesCommonDir = new File(ulcClientClassesDir, 'common')
    ulcClientClassesCommonDir.mkdirs()

    // collect all client-side libs
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
            ant.copy(todir: ulcAppClassesDir) {
                fileset(dir: "${basedir}/src/ulc-client/${alias}",
                        excludes: '**/*.groovy, **/*.java, **/*.svn/**, **/CVS/**')
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

eventCreateWarStart = { warName, stagingDir ->
    def ulcClientLibs = collectClientJars()
    ulcClientLibs.each { libFile ->
        ant.delete(file: new File("${stagingDir}/WEB-INF/lib/${libFile.name}"), quiet: true, failonerror: false)
    }
}




private def collectAllDependencies(List jarFiles, dependency, resolveReport) {
    def jarFile = resolveReport.getArtifactsReports(dependency.id).find {it.type == 'jar'}
    if(jarFile) {
        jarFiles << jarFile.localFile
        dependency.getDependencies('', 'runtime').each { transitiveDependency ->
            collectAllDependencies(jarFiles, transitiveDependency, resolveReport)
        }
    }
}

private def fillJarFileList(List jarFiles, report, resolveReport) {
    def runtimeReport = report.getConfigurationReport('runtime')
    List processedDependencies = []

    runtimeReport.moduleIds.each { moduleId ->
        runtimeReport.getNodes(moduleId).each { dependencyNode ->
            def mrid = dependencyNode.id
            if (processedDependencies.contains(mrid)) return

            if (mrid.name =~ /.*-client/) {
                processedDependencies << mrid
                collectAllDependencies(jarFiles, dependencyNode, resolveReport)
            }
        }
    }
}

private def collectClientJars() {
    def ulcClientlibs = []

    def resolveReport = grailsSettings.dependencyManager.resolveDependencies('runtime')
    resolveReport.output([[
            output: {report, cacheMgr, options ->
                fillJarFileList(ulcClientlibs, report, resolveReport)
            },
            getName: {' UlcReportOutputter' }
    ] as ReportOutputter
    ] as ReportOutputter[], null, null)

    File baseClientLibDir = new File(basedir, 'lib/ulc-client')
    baseClientLibDir.eachFileRecurse() {if(it.name.endsWith(".jar")) ulcClientlibs << it }

//    ulcClientlibs.unique{it.name} // jars with same name are only collected once. Do we want this?
    return ulcClientlibs
}


private def prepareApplication(stagingDir) {
    generateApplicationsFile("${stagingDir}/WEB-INF/resources")


    File tmpUlcClientLibDir = new File(grailsSettings.projectWorkDir, 'tmp/ulc-client-libs')
    File ulcClientLibsDir = new File("${stagingDir}/ulc-client-libs")
    ulcClientLibsDir.mkdirs()

    // copy local common client jar (if classes are available)
    if (ulcClientClassesCommonDir.list()) {
        File commonJar = new File("${stagingDir}/ulc-client-libs/${grailsAppName}-common-client.jar")
        ant.jar(destfile: commonJar) {
            fileset(dir: ulcClientClassesCommonDir, includes: '**/*.class')
        }
        copyPackAndSignFile(commonJar, tmpUlcClientLibDir, ulcClientLibsDir)
    }

    // copy, sign and pack all client libs from configured dependencies (BuildConfig.groovy)
    collectClientJars().each { libFile ->
        copyPackAndSignFile(libFile, tmpUlcClientLibDir, ulcClientLibsDir)
    }

    File ulcTemplatesDir = new File("${basedir}/ulc-templates")
    forEachUlcApplication {String applicationAlias, String applicationClassName ->
        def aliasClientClassesDir = new File(ulcClientClassesDir, applicationAlias)
        File aliasClientJarDir = new File("${stagingDir}/ulc-client-libs/${applicationAlias}")
        aliasClientJarDir.mkdirs()
        File aliasClientJar = new File(aliasClientJarDir, "application-${applicationAlias}-client.jar")


        ant.uptodate(property: "aliasClientJarUpToDate", targetfile: aliasClientJar) {
            srcfiles(dir: aliasClientClassesDir, includes: '**/*.class')
        }

        if (!ant.project.properties.aliasClientJarUpToDate) {
            ant.jar(destfile: aliasClientJar) {
                fileset(dir: aliasClientClassesDir)
            }
        }
        copyPackAndSignFile(aliasClientJar, tmpUlcClientLibDir, ulcClientLibsDir)

        generateJnlpFiles(tmpUlcClientLibDir, applicationAlias, ulcTemplatesDir, stagingDir, applicationClassName)
    }


    ant.copy(todir: ulcClientLibsDir, overwrite: true) {
        fileset(dir: tmpUlcClientLibDir, includes: '**/*')
    }
    ant.delete(dir: tmpUlcClientLibDir, failonerror: false, quiet: true)
}

private def generateJnlpFiles(File tmpUlcClientLibDir, String applicationAlias, File ulcTemplatesDir, String stagingDir, String applicationClassName) {
    def libs = []
    tmpUlcClientLibDir.eachFileMatch(~/.*\.jar/) { tmpClientJar ->
        libs << "        <jar href='ulc-client-libs/$tmpClientJar.name' main='false'/>"
    }
    new File(tmpUlcClientLibDir, applicationAlias).eachFileMatch(~/.*\.jar/) { tmpClientAliasJar ->
        boolean mainJar = (tmpClientAliasJar.name == "application-${applicationAlias}-client.jar")
        libs << "        <jar href='ulc-client-libs/${applicationAlias}/$tmpClientAliasJar.name' main='$mainJar'/>"
    }

    writeJnlpFile(ulcTemplatesDir, applicationAlias, stagingDir, applicationClassName, 'JnlpLauncher', "", libs)
    writeJnlpFile(ulcTemplatesDir, applicationAlias, stagingDir, applicationClassName, 'AppletLauncher', "-applet", libs)
}

private def writeJnlpFile(File ulcTemplatesDir, String applicationAlias, String stagingDir, String applicationClassName, String launcher, String suffix,def libs) {
    File aliasJnlpTemplate = new File(ulcTemplatesDir, "${applicationAlias}${suffix}.jnlp")
    if (!aliasJnlpTemplate.exists()) aliasJnlpTemplate = new File(ulcTemplatesDir, "default${suffix}.jnlp")

    File aliasJnlpFile = new File("${stagingDir}/${applicationAlias}${suffix}.jnlp")
    ant.copy(file: aliasJnlpTemplate, tofile: aliasJnlpFile)
    ant.replace(file: aliasJnlpFile) {
        replacefilter(token: '@application.name@', value: applicationAlias[0].toUpperCase() + applicationAlias[1..-1])
        replacefilter(token: '@application.alias@', value: applicationAlias)
        replacefilter(token: '@application.launcher@', value: applicationClassName + launcher)
        replacefilter(token: '@ulc.client.libs@', value: libs.join('\n'))
    }
}


private boolean compilingUlcPlugin() {
    getPluginDirForName('ulc')?.file?.canonicalPath == basedir
}

void copyPackAndSignFile(File srcFile, File destinationDir, File deploymentDir) {
    ant.echo(message: "Copy and pack ${srcFile.absolutePath} to ${destinationDir}")
    try {
        File destFile = resolveDestinationFile(destinationDir, srcFile)
        File deployedFile = resolveDestinationFile(deploymentDir, srcFile)

        if (deployedFile.exists() && deployedFile.lastModified() > srcFile.lastModified()) {
            println "Skipping copyPackAndSignFile for $srcFile.name as it is up to date."
            return
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

private File resolveDestinationFile(File destinationDir, File srcFile) {
    File destFile = new File(destinationDir, srcFile.name)
    String ancestor = srcFile.parentFile.parentFile.name
    if (ancestor == 'ulc-client' || ancestor == 'ulc-client-libs') {
        destFile = new File(destinationDir, "${srcFile.parentFile.name}/${srcFile.name}")
    }
    return destFile
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
