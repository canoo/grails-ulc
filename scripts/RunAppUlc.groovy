import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import grails.util.Metadata

includeTargets << grailsScript('Package')
includeTargets << grailsScript('Bootstrap')

ant.property(environment: 'env')
ulcHome = ant.antProject.properties.'env.ULC_HOME'
userHome = ant.antProject.properties.'env.HOME'

target(runAppUlc: 'Run the ULC application in standalone mode') {
    depends(checkVersion, configureProxy, packageApp, classpath)

    def urls = [classesDir.toURI().toURL(), grailsSettings.resourcesDir.toURI().toURL()]
    def file = new File(ulcHome, 'license')
    if(file.exists()) file.eachFileMatch(~/.*\.jar/){f -> urls << f.toURI().toURL()}
    file = new File(userHome, ".ulc-${Metadata.current.'app.ulc.version'}") 
    if(file.exists()) urls << file.toURI().toURL()

    urls.each{rootLoader.addURL(it)}

    classLoader = new URLClassLoader(urls as URL[], rootLoader)
    Thread.currentThread().setContextClassLoader(classLoader)
    loadApp()
    configureApp()

    try {
        GrailsConfigUtils.executeGrailsBootstraps(grailsApp, appCtx, null)
        grailsApp.classLoader.loadClass('com.canoo.grails.ulc.UlcGrailsRunner').start(appCtx)
    } catch (Exception e) {
        event('StatusFinal', ["Error starting application: ${e.message}"])
    }
}

setDefaultTarget(runAppUlc)
