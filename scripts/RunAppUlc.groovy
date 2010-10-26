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

import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import grails.util.Metadata

includeTargets << grailsScript('Package')
includeTargets << grailsScript('Bootstrap')

ant.property(environment: 'env')

target(runAppUlc: 'Run the ULC application in standalone mode') {
    depends(checkVersion, configureProxy, packageApp, classpath)

    def urls = [classesDir.toURI().toURL(),
                grailsSettings.resourcesDir.toURI().toURL(),
                ulcLicenseDir.toURI().toURL()]

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
