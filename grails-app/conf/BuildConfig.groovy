grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

ulcVersion = '7.0.1'

grails.project.dependency.resolution = {
    inherits('global') 
    log 'warn'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenRepo name: 'Canoo Public Releases', root: 'https://ci.canoo.com/nexus/content/repositories/public-releases', m2compatible: true
        mavenCentral()
    }
    dependencies {
        compile("com.canoo.ulc:ulc-core-server:$ulcVersion") { excludes 'ulc-developer-key' }
        runtime('org.codehaus.mojo.webstart:webstart-jnlp-servlet:1.0-6.0.02_ea_b02.1') { excludes 'servlet-api' }
        runtime("com.canoo.ulc:ulc-core-client:$ulcVersion") { excludes 'ulc-developer-key' }
        runtime('com.canoo.ulc:ulcbuilder-core:1.0') {
            excludes 'groovy'
            excludes 'ulc-core-server'
            excludes 'ulc-developer-key'
        }
    }
}
