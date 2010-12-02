package ulc

import groovy.xml.MarkupBuilder


class UlcTagLib {
    static namespace = 'ulc'
    
    /**
     * Writes appropriate HTML code to display an ULC application as an applet.<p>
     * The following attributes are supported
     * <ul>
     *   <li><strong>name</strong>: specifies the name of the application ro run. <strong>REQUIRED</strong>.</li>
     *   <li><strong>width</strong>: width in pixels. Default: 480</li>
     *   <li><strong>height</strong>: height in pixels. Default: 320</li>
     *   <li><strong>javaversion</strong>: Java version to use. Default: '1.5.0'</li>
     * </ul>
     * Any additional parameters set on this tag will be set as parameters on the applet as is.
     */
    def applet = { attrs, body ->
        Map aliases = getApplicationAliases(servletContext)      
        
        if(!attrs.name || !aliases[attrs.name]) {
            throw new IllegalArgumentException("'${attrs.name}' is not a recognized ULC application alias.")
        }
        
        String alias = attrs.remove('name')
        String width = attrs.remove('width') ?: '480'
        String height = attrs.remove('height') ?: '320'
        String javaVersion = attrs.remove('javaversion') ?: '1.5.0'
        String className = aliases[alias] - 'Application'
        List jars = collectJars(request, servletContext, alias)
        String additionalArgs = attrs.collect([]){ "${it.key}: '${it.value}'"}.join(', ')
        additionalArgs = additionalArgs? ", $additionalArgs" : ''
        
        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.script(src: 'http://java.com/js/deployJava.js')
        builder.script("""
var attributes = {
    id: '${alias}',
    codebase: '${request.contextPath}',
    code: '${className}AppletLauncher',
    archive: '${jars.join(',')}',
    width: '${width}',
    height: '${height}'${additionalArgs}
}

var parameters = {
    java_arguments: '-Djnlp.packEnabled=true',
    jnlp_href: '${request.contextPath}/${alias}-applet.jnlp'
}

deployJava.runApplet(attributes, parameters, '${javaVersion}')
        """)
        
        out << writer.toString()
    }
    
    private static collectJars(request, servletContext, alias) {
        List jars = []
        
        File rootDir = new File(servletContext.getRealPath('/'))
        File ulcClientLibs = new File(rootDir, 'ulc-client-libs')
        File appClientLibs = new File(ulcClientLibs, alias)
        jars << "ulc-client-libs/${alias}/application-${alias}-client.jar"
        appClientLibs.eachFileMatch(~/.*\.jar/) { f ->
            if(f.name == "application-${alias}-client.jar") return      
            jars << "ulc-client-libs/${alias}/${f.name}"
        }
        ulcClientLibs.eachFileMatch(~/.*\.jar/) { f ->      
            jars << "ulc-client-libs/${f.name}"
        }
        
        jars
    }
    
    private static Map<String, String> getApplicationAliases(servletContext) {
        final Map<String, String> applications = new LinkedHashMap<String, String>()
        Properties props = new Properties()
        try {
            props.load(servletContext.getResourceAsStream("/WEB-INF/resources/ulc-applications.properties"))
            Enumeration<Object> keys = props.keys()
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement()
                String value = (String)props.get(key)
                if (value != null) {
                    applications.put((String)key, value)
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read '/WEB-INF/resources/ulc-applications.properties'.")
        }
        return applications
    }
}
