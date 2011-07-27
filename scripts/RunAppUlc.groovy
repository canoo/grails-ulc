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

import grails.util.Metadata
import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import com.ulcjava.base.development.DevelopmentRunner
import java.util.concurrent.CountDownLatch

includeTargets << grailsScript('Package')
includeTargets << grailsScript('Bootstrap')
includeTargets << grailsScript("_GrailsCreateArtifacts")
includeTargets << new File("${ulcPluginDir}/scripts/_Ulc.groovy")

ant.property(environment: 'env')

target(runAppUlc: 'Run an ULC application in standalone mode') {
    depends(checkVersion, configureProxy, parseArguments)
    System.setProperty("runMode", "runAppUlc")
    generateApplicationsFile("${projectTargetDir}/resources")
    
    type = 'Application'
    promptForName(type: type)
    def name = argsMap['params'][0]
    name = purgeRedundantArtifactSuffix(name, type).toLowerCase()

    boolean useGui = argsMap.useGui ?: false

    Properties props = new Properties()
    props.load(new File("${projectTargetDir}/resources/ulc-applications.properties").toURL().openStream())
    def ulcAppClassName = props.get(name)
    if(!ulcAppClassName) {
        println "Can't locate a suitable ULC Application definition for $name"
        System.exit(1)
    }

    // add all application jar to the classLoader before starting the grails application
    URLClassLoader classLoader = getExtendedClassLoader(name)

    bootstrap()

    System.setProperty('grails.ulc.application.alias', name)

    Class.forName("com.canoo.grails.ulc.server.ULCApplicationHolder", true, classLoader).init(projectTargetDir)

    try {
        GrailsConfigUtils.executeGrailsBootstraps(grailsApp, appCtx, null)
        String configurationFile = ulcAppClassName
        configurationFile = '/' + configurationFile.replace('.' as char, '/' as char) + 'UlcConfiguration.xml'
        def latch = new CountDownLatch(1)
        def t = new Thread({
            DevelopmentRunner.applicationConfigurationResource = configurationFile
            DevelopmentRunner.useGui = useGui
            DevelopmentRunner.run()
        })
        t.start()
        latch.await()
    } catch (Exception e) {
        event('StatusFinal', ["Error starting application: ${e.message}"])
    }
}

private URLClassLoader getExtendedClassLoader(name) {
    ulcClientClassesDir = new File(grailsSettings.projectWorkDir, 'ulc-client-classes')
    ulcClientClassesCommonDir = new File(ulcClientClassesDir, 'common')
    ulcClientClassesAppDir = new File(ulcClientClassesDir, name)

    def urls = [classesDir.toURI().toURL(),
            pluginClassesDir.toURI().toURL(),
            projectTargetDir.toURI().toURL(),
            grailsSettings.resourcesDir.toURI().toURL(),
            ulcLicenseDir.toURI().toURL(),
            ulcClientClassesCommonDir.toURI().toURL(),
            ulcClientClassesAppDir.toURI().toURL()]

    File baseClientLibDir = new File(basedir, 'lib/ulc-client')
    baseClientLibDir.eachFileMatch(~/.*\.jar/) { jar -> urls << jar.toURI().toURL() }

    File appClientLibDir = new File(baseClientLibDir, name)
    appClientLibDir.mkdirs()
    appClientLibDir.eachFileMatch(~/.*\.jar/) { jar -> urls << jar.toURI().toURL() }

    classLoader = new URLClassLoader(urls as URL[], rootLoader)
    Thread.currentThread().setContextClassLoader(classLoader)
    return classLoader
}

setDefaultTarget(runAppUlc)