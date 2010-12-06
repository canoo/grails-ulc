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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * @author ulcteam
 */
class UlcGrailsPlugin {
    // the plugin version
    def version = "0.3"
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

        servletDefinition('JnlpDownloadServlet',
                          'jnlp.sample.servlet.JnlpDownloadServlet')
        servletMappingDefinition('JnlpDownloadServlet', '*.jnlp')
           
        forEachUlcApplication { alias, className ->
            servletDefinition("UlcApplicationServlet_${alias}",
                              'com.canoo.grails.ulc.server.UlcApplicationServlet',
                              ['application-alias': alias,
                              'is-applet': false])
            servletDefinition("UlcApplicationServlet_${alias}_applet",
                              'com.canoo.grails.ulc.server.UlcApplicationServlet',
                              ['application-alias': alias,
                              'is-applet': true])
            servletMappingDefinition("UlcApplicationServlet_${alias}", "/${alias}.ulc")
            servletMappingDefinition("UlcApplicationServlet_${alias}_applet", "/${alias}-applet.ulc")
        }

        mimeMappingDefinition('jnlp', 'application/x-java-jnlp-file')
    }
    
    private forEachUlcApplication = { callback ->
        Map<String,String> applications = [:]
        def pathResolver = new PathMatchingResourcePatternResolver(getClass().classLoader)
        pathResolver.getResources("classpath*:/**/*UlcConfiguration.xml".toString()).each { cfg ->
            String className = new XmlSlurper().parse(cfg.file).applicationClassName.toString() - 'Application'
            String alias = GrailsNameUtils.getShortName(className.toString())
            applications[alias.toLowerCase()] = className
        }
        applications.each { alias, className -> callback(alias, className) }   
    }
}
