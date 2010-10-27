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

import grails.util.GrailsNameUtils

/**
 * @author ulcteam
 */
class UlcGrailsPlugin {
    // the plugin version
    def version = "0.3-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.4 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def author = "Canoo Engineering AG"
    def authorEmail = "ulc-team@canoo.com"
    def title = "ULC-Grails-Integration"
    def description = '''
Provides ULC (Canoo RIA Suite) integration for Grails
http://www.canoo.com/ulc
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/ulc"

    def doWithWebDescriptor = {webXml ->
        String appClassName = extractApplicationClassName()
        // Do not perform configuration if no application
        // is available
        if(!appClassName) return

        def servlets = webXml.'servlet'
        def servletDefinition = { name, klass, initParams = [:] ->
            def servletElement = servlets[servlets.size() - 1]
            servletElement + {
                'servlet' {
                    'servlet-name'(name)
                    'servlet-class'(klass)
                    initParams.each { k, v ->
                        'init-param' {
                            'param-name'(k)
                            'param-value'(v)
                        }
                    }
                }
            }
        }

        def servletMappings = webXml.'servlet-mapping'
        def servletMappingDefinition = { name, pattern ->
            servletMappings + {
                'servlet-mapping' {
                    'servlet-name'(name)
                    'url-pattern'(pattern)
                }
            }
        }

        def mimeMappings = webXml.'mime-mapping'
        def mimeMappingDefinition = { name, type ->
            mimeMappings + {
                'mime-mapping' {
                    'extension'(name)
                    'mime-type'(type)
                }
            }
        }

        servletDefinition('ClientJarDownloader',
                          'jnlp.sample.servlet.JnlpDownloadServlet')
        servletDefinition('ResourceDownloader',
                          'com.ulcjava.easydeployment.server.ResourceDownloader')
        // TODO externalize gsp/jnlp file name
        servletDefinition('IndexServlet',
                          'com.ulcjava.easydeployment.server.IndexServlet',
                          ['applet-redirect': 'start.jsp',
                           'jnlp-redirect': 'start.jnlp'])
        servletDefinition('ServletContainerAdapter',
                          'com.ulcjava.container.servlet.server.ServletContainerAdapter')
        servletDefinition('ConfigPropertiesDownloader',
                          'com.ulcjava.container.servlet.server.servlets.ConfigPropertiesDownloader')

        def listeners = webXml.'listener'
        def listenerElement = listeners[listeners.size() - 1]
        listenerElement + {
            'listener' {
                'listener-class'('com.ulcjava.easydeployment.server.ClientJarPreparationListener')
            }
        }

        def applicationPackage = getPackageName(appClassName).replace('.' as char, '/' as char)

        servletMappingDefinition('ClientJarDownloader', '*.jar')
        servletMappingDefinition('ResourceDownloader', "/${applicationPackage}/resources/*")
        servletMappingDefinition('ConfigPropertiesDownloader', '/clientconfig.properties')
        servletMappingDefinition('ClientJarDownloader', '*.jnlp')
        servletMappingDefinition('ServletContainerAdapter', '/ulc')
        servletMappingDefinition('IndexServlet', '/start')

        mimeMappingDefinition('jnlp', 'application/x-java-jnlp-file')
        mimeMappingDefinition('html', 'text/html')
        mimeMappingDefinition('gif', 'image/gif')
        mimeMappingDefinition('jpg', 'image/jepg')
        mimeMappingDefinition('png', 'image/png')
    }

    private extractApplicationClassName = { ->
        File ulcConfigFile = new File("grails-app/conf/ULCApplicationConfiguration.xml")
        if(!ulcConfigFile.exists()) return null
        new XmlSlurper().parse(ulcConfigFile).applicationClassName
    }

    private getPackageName = { String className ->
        if(GrailsNameUtils.isBlank(className)) return ''
        String packageName = className - GrailsNameUtils.getShortName(className)
        packageName.endsWith('.') ? packageName[0..-2] : packageName
    }
}
