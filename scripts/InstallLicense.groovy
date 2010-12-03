includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")

target(installLicense: "Creates the ulc-deployment-key.jar into the applications lib directory") {
    checkLicense()

    ant.jar(destfile: "${basedir}/lib/ulc-deployment-key.jar") {
        fileset(dir: ulcLicenseDir, includes: 'DEPLOYMENT-*.lic')
    }
}

setDefaultTarget(installLicense)
