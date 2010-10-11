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

import grails.util.GrailsNameUtils
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target(main: "The description of the script goes here!") {
    depends(checkVersion, parseArguments)

    def type = 'Application'
    promptForName(type: type)
    def name = argsMap['params'][0]
    name = purgeRedundantArtifactSuffix(name, type)

    createArtifact(name: name,
        suffix: type,
        type: 'UlcApp',
        path: 'src/groovy')

    // previous call adds the following variables to the binding
    // className = capitalized class name sans suffix (simple representation)
    // propertyName = uncapitalized class name sans suffix (simple representation)

    def pkgPath = findPackage(name)

    createApplicationConfiguration(pkgPath, className)
    createResourceBundle(pkgPath, className)
}

private void createResourceBundle(pkgPath, className) {
    File packageDir = new File("${basedir}/src/java/${pkgPath}/resources")
    packageDir.mkdirs()

    File propertiesFile = new File(packageDir, "${className}Application.properties")
    if (propertiesFile.exists()) {
        return
    }

    Properties props = new Properties()
    props['Application.title'] = className + 'Application'
    props['Application.vendor'] = 'Grails'
    propertiesFile.withOutputStream {props.store(it, null) }
}

private void createApplicationConfiguration(pkgPath, className) {
    String xmlString = generateXmlString(pkgPath + (pkgPath?'.':'') + className + 'Application')

    File ulcConfigFile = new File("${basedir}/grails-app/conf/ULCApplicationConfiguration.xml")
    ulcConfigFile.text = xmlString
}

private String generateXmlString(appClassName) {
    StringWriter writer = new StringWriter()

    def builder = new StreamingMarkupBuilder()
    builder.encoding = "UTF-8"
    writer << builder.bind {mkp.xmlDeclaration()}

    def xml = new MarkupBuilder(writer)
    def params = [:]
    params."xmlns:ulc" = "http://www.canoo.com/ulc"
    params."xmlns:xsi" = "http://www.w3.org/2001/XMLSchema-instance"
    params."xsi:schemaLocation" = "http://www.canoo.com/ulc/ULCApplicationConfiguration.xsd"
    xml.'ulc:ULCApplicationConfiguration'(params) {
        'ulc:applicationClassName'(appClassName)
        'ulc:clientResources' {
            'ulc:directory'('/WEB-INF/lib/ulc-client-libs')
            'ulc:pattern'('*.jar')
        }
    }

    String xmlString = writer.toString()
    return xmlString
}


// wishing the following was public in the standard Grails scripts
private findPackage(name) {
    def pkg = null
    def pos = name.lastIndexOf('.')
    if (pos != -1) {
        pkg = name[0..<pos]
        if (pkg.startsWith("~")) {
            pkg = pkg.replace("~", createRootPackage())
        }
    }
    else {
        pkg = argsMap.skipPackagePrompt ? '' : createRootPackage()
    }
    
    return pkg
}

private createRootPackage() {
    compile()
    createConfig()
    return (config.grails.project.groupId ?: grailsAppName).replace('-','.').toLowerCase()
}


setDefaultTarget(main)
