includeTargets << grailsScript("Init")

target(main: "The description of the script goes here!") {

    println ulcPluginDir

    ant.copy(todir:"$basedir") {
        fileset(dir:"$ulcPluginDir/src/templates/domainviewer")
    }
}

setDefaultTarget(main)
