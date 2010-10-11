import org.apache.ivy.plugins.report.ReportOutputter

ULC_CLIENT_LIBS = []
ULC_CLIENT_LIBS_DIR = new File("${basedir}/web-app/WEB-INF/lib/ulc-client-libs")

private collectAllDeps(d) {
    ULC_CLIENT_LIBS << "${d.id.name}-${d.id.revision}.jar".toString()
    d.getDependencies('','runtime').each { d2 ->
        collectAllDeps(d2)
    }
}

private output = { report, cacheMgr, options ->
    r = report.getConfigurationReport('runtime')
    List deps = []

    r.moduleIds.each { mid ->
        r.getNodes(mid).each { dep ->
            def mrid = dep.id
            if(deps.contains(mrid)) return

            if(mrid.name =~ /.*-client/) {
                deps << dep
                collectAllDeps(dep)
            }
        }
    }
}

eventCleanEnd = {
    if(ULC_CLIENT_LIBS_DIR.exists()) ant.delete(dir: ULC_CLIENT_LIBS_DIR, quiet: false, failonerror: false)
}

/**
 * This event handler is responsible to handle client-jars provided by the plugin
 */
eventPackagePluginStart = {pluginName ->
    if(compilingUlcPlugin()) return

    ULC_CLIENT_LIBS = []
    if(ULC_CLIENT_LIBS_DIR.exists()) ant.delete(dir: ULC_CLIENT_LIBS_DIR, quiet: true)
    ant.mkdir(dir: ULC_CLIENT_LIBS_DIR)

    copyClientJars(true)
}

/**
 * This event handler is responsible to handle client-jars provided by the application
 */
eventPackagingEnd = {
    if(compilingUlcPlugin()) return

try{
    copyClientJars()

    def r = grailsSettings.dependencyManager.resolveDependencies('runtime')
    r.output([[
        output: {report, cacheMgr, options ->
            output(report, cacheMgr, options)
        },
        getName: {' UlcReportOutputter' }
      ] as ReportOutputter
    ] as ReportOutputter[], null, null)

    def jnlpJars = []
    ULC_CLIENT_LIBS.each { jar ->
        if(jar =~ /ulc-core-client.*\.jar/){
            jnlpJars << "        <jar href='$jar' main='true' />"
        } else {
            jnlpJars << "        <jar href='$jar' />"
        }
    }

    // copy & replace ULC templates
    def ulcTemplates = new File("${basedir}/ulc-templates")
    ant.copy(todir: "${basedir}/web-app") {
        fileset(dir: "${basedir}/ulc-templates", excludes: "**/.svn/**, **/CVS/**")
    }
    ant.fileset(dir: "${basedir}/web-app").each {
        String fileName = it.toString()
        ant.replace(file: fileName) {
            replacefilter(token: '@grails.app.name@', value: grailsAppName)
            replacefilter(token: '@ulc.client.libs@', value: ULC_CLIENT_LIBS.join(','))
            replacefilter(token: '@ulc.client.jars@', value: jnlpJars.join('\n'))
        }
    }

    def dependencies = grailsSettings.runtimeDependencies
    ULC_CLIENT_LIBS.each { libName ->
        def f = dependencies.find{it.name == libName}
        if(f) copyFile(f, true)
    }
}catch(x){x.printStackTrace()}
}

eventCreateWarStart = { warName, stagingDir ->
    // remove duplicate jar files
    ULC_CLIENT_LIBS.each { libName ->
        ant.delete(file: new File("${stagingDir}/WEB-INF/lib/${libName}"), quiet: true, failonerror: false)
    }
}

void copyClientJars(boolean overwrite = false) {
    event('BeforeCopyClientJars', [])
    File clientLibDir = new File(basedir, 'lib/ulc-client')
    if(clientLibDir.exists()) {
        clientLibDir.eachFile {File f ->
            if(f.name.endsWith('.jar')) {
                ULC_CLIENT_LIBS << f.name
                copyFile(f, overwrite)
            }
        }
    }
}

void copyFile(File jarFile, boolean overwrite = false) {
    File destFile = new File(ULC_CLIENT_LIBS_DIR, jarFile.name)
    if(!destFile.exists() || jarFile.lastModified() > destFile.lastModified() || overwrite) {
        ant.copy(file: jarFile, tofile: destFile, overwrite: true)
    }
}

private boolean compilingUlcPlugin() {
    getPluginDirForName('ulc')?.file?.canonicalPath == basedir
}
