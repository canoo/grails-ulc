class UlcGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.4 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
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

        // TODO externalize
        def applicationPackage = ''

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
}