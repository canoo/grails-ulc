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

import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target(main: "Creates a new ULC application along with its helper files.") {
    depends(checkVersion, parseArguments)

    def type = 'Application'
    def name = 'common'
    
    while(true) {
        promptForName(type: type)
        name = argsMap['params'][0]
        name = purgeRedundantArtifactSuffix(name, type)
        if(name == 'common') {
            println "Name 'common' is reserved. Please choose a different one."
        } else {
            break
        }
    }
    
    createArtifact(name: name,
        suffix: 'CoderRegistryHolder',
        type: 'CoderRegistryHolder',
        path: 'grails-app/ulc')
    ant.replace(file: artifactFile,
        token: "@application.alias@", value: className.toLowerCase())
    createArtifact(name: name,
        suffix: type,
        type: 'UlcApplication',
        path: 'grails-app/ulc')
    createArtifact(name: name,
        suffix: 'Applet',
        type: 'UlcApplet',
        path: 'grails-app/ulc')
    ant.replace(file: artifactFile,
        token: "@application.name@", value: "${className}Application")
    ant.mkdir(dir: "${basedir}/lib/ulc-client/${className.toLowerCase()}")

    // previous calls add the following variables to the binding
    // className = capitalized class name sans suffix (simple representation)
    // propertyName = uncapitalized class name sans suffix (simple representation)

    def pkgPath = findPackage(name)

    createApplicationConfiguration(pkgPath, className)
    createResourceBundle(pkgPath, className)
    createLaunchers(pkgPath, className)
}

private void createResourceBundle(pkgPath, className) {
    def pkg = pkgPath.replace('.' as char, '/' as char)
    File packageDir = new File("${basedir}/src/java/${pkg}/resources")
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

    def pkg = pkgPath.replace('.' as char, '/' as char)
    File ulcConfigDir = new File("${basedir}/src/java/${pkg}")
    ulcConfigDir.mkdirs()
    File ulcConfigFile = new File(ulcConfigDir, "${className}UlcConfiguration.xml")
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
        'ulc:serverSessionProviderClassName'('com.canoo.grails.ulc.server.GrailsULCSessionProvider')
        'ulc:clientResources' {
            'ulc:directory'('/ulc-client-libs')
            'ulc:pattern'('*.jar')
        }
    }

    String xmlString = writer.toString()
    return xmlString
}

private void createLaunchers(pkgPath, className) {
    pkgPath = pkgPath.replace('.' as char, '/' as char)
    createJavaArtifact(pkgPath: pkgPath,
        className: className,
        type: 'AppletLauncher')
    createJavaArtifact(pkgPath: pkgPath,
        className: className,
        type: 'JnlpLauncher')
}

private void createJavaArtifact(Map args) {
    def pkg = args.pkgPath.replace('/' as char, '.' as char)
    def artifactDir = new File("${basedir}/src/ulc-client/${args.className.toLowerCase()}/${args.pkgPath}")
    artifactDir.mkdirs()
    def artifactFile = new File(artifactDir, "${args.className}${args.type}.java")
    def templateFile = new File("${ulcPluginDir}/src/templates/artifacts/Ulc${args.type}.java")
    artifactFile.text = templateFile.text
    ant.replace(file: artifactFile,
        token: "@artifact.name@", value: className)
    if (pkg) {
        ant.replace(file: artifactFile, token: "@artifact.package@", value: "package ${pkg};\n\n")
    }
    else {
        ant.replace(file: artifactFile, token: "@artifact.package@", value: "")
    }
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
