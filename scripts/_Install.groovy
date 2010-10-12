//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")
//

includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")

ant.mkdir(dir: "${basedir}/lib/ulc-client")
updateMetadata('app.ulc.version': '7.0.1')

ant.mkdir(dir: "${basedir}/ulc-templates")
ant.copy(todir: "${basedir}/ulc-templates") {
    fileset(dir: "${ulcPluginDir}/src/templates/web-app")
}

try{
checkLicense()
}catch(x){x.printStackTrace()}
