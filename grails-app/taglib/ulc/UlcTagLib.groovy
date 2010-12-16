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
     *   <li><strong>javaversion</strong>: Java version to use. Default: '1.6'</li>
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
        String javaVersion = attrs.remove('javaversion') ?: '1.6'
        String className = aliases[alias] - 'Application'
        String additionalArgs = attrs.collect([]){ "${it.key}: '${it.value}'"}.join(', ')
        additionalArgs = additionalArgs? ", $additionalArgs" : ''

        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.script(src: 'http://java.com/js/deployJava.js','')
        // the '' forces a closing tag. Some browsers do not handle the <script ../> correctly

        builder.script("""
var attributes = {
    id: '${alias}',
    codebase: '${request.contextPath}',
    code: '${className}AppletLauncher',
    width: '${width}',
    height: '${height}'${additionalArgs}
}

var parameters = {
    jnlp_href: '${alias}-applet.jnlp'
}

deployJava.runApplet(attributes, parameters, '${javaVersion}')
        """)

        out << writer.toString()
    }

    /**
     * Writes appropriate HTML code to display an ULC application as an (deprecated) applet tag.<p>
     * This is still useful when the "applet" tag above is not displayed correctly
     * The following attributes are supported
     * <ul>
     *   <li><strong>name</strong>: specifies the name of the application ro run. <strong>REQUIRED</strong>.</li>
     *   <li><strong>archive</strong>: comma-separated list of client jars as passed onto the applet tag <strong>REQUIRED</strong>.</li>
     *   <li><strong>width</strong>: width in pixels. Default: 480</li>
     *   <li><strong>height</strong>: height in pixels. Default: 320</li>
     * </ul>
     * No additional parameters are set.
     */
    def appletTag = { attrs, body ->
        Map aliases = getApplicationAliases(servletContext)

        if(!attrs.name || !aliases[attrs.name]) {
            throw new IllegalArgumentException("'${attrs.name}' is not a recognized ULC application alias.")
        }

        String alias    = attrs.name
        String archive  = attrs.archive
        String width    = attrs.width  ?: '480'
        String height   = attrs.height ?: '320'
        String className = aliases[alias] - 'Application'
        String contextURL = request.requestURL.toString() - request.requestURI + request.contextPath

        MarkupBuilder builder = new MarkupBuilder(out)
        builder.applet(
                width:  width,
                height: height,
                codebase: contextURL,
                code:"${className}AppletLauncher.class",
                archive: archive) {
            param(name:"url-string", value:"${contextURL}/${alias}-applet.ulc")
            param(name:"keep-alive-interval", value:"900")
            param(name:"log-level", value:"WARNING")
            'Your browser does not support JDK 1.5 or higher for applets.'
        }
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
