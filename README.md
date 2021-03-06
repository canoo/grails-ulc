grails-ulc-plugin
=================

The grails-ulc plugin provides integration with Canoo RIA Suite, a better way to
build Rich Internet Applications for the Java platform with an emphasis on
beauty and productivity.

Canoo RIA Suite empowers developers to implement pure server-side web
applications with the user experience of a desktop (Swing) application.
Key features are:

* Rich user interface (Swing)

* Pure server-side programming

* Swing-like programming model with APIs being almost identical to Swing

* No application-specific code sent to the client, no JavaScript (not even generated JavaScript)

* 100% pure Java with full IDE and other tool support; using Groovy is optional

* Custom extensions are possible

* Many business components are available

* Java security model

* Low bandwidth demands

Please find more information about the Canoo RIA Suite under
<http://www.canoo.com/ulc>.

Getting Started
===============

The ULC Core technology requires a valid developer license in order to be used in a
development environment. A (free) runtime license is also required to deploy an
application to a server or as a war. The plugin can check for the availability
of a valid license on the development library.

Follow these steps to get started with your first ULC application.

1. Install the grails-ulc plugin by invoking the `install-plugin` command, like
   this

        grails install-plugin ulc

2. Upon installing the plugin it will attempt to locate a valid license on your
system. If such a license is found then you're good to go to the next step. If
no license is found the the plugin will ask you for an access token used to
download a personalized license. If you do not have an access token then hit
enter to continue, the plugin will resort to downloading an evaluation license.
The required runtime license is automatically generated for you when using the
commands mentioned below. You can enforce the runtime license generation with the
command:

         grails install-license
         
   If you habe already used an older version of the plugin, or want to upgrade
   your license this command is recommended.


3. Create an ULC application. This step is performed by invoking the following
command

        grails create-app-ulc <nameOfYourApplication>

    A webapp application may contain several ULC applications. Each single 
    ULC application is referred by an alias which is created from the lowercase
    formatted &lt;nameOfYourApplication&gt;.
    The alias is also used to group the resource provided by each single ULC application.
    Such resources can be:
      - Application specific launcher classes (grails-app/src/ulc-client/<alias>)
      - Clientside jar files (lib/ulc-client/<alias>)
      - Application specific template for the JNLP-file generation (ulc-templates/<alias>.jnlp`)

4. Locate the generated application class file under *src/groovy*. This is the
entry point for your newly created ULC application. The following snippet
reproduces the content of the evaluated application template

        package com.acme

        import com.ulcjava.applicationframework.application.SingleFrameApplication
        import com.ulcjava.base.application.ULCComponent
        import com.ulcjava.base.application.ULCFrame
        import com.canoo.groovy.ulc.ULCBuilder

        class SampleApplication extends SingleFrameApplication {
            private final ULCBuilder builder = new ULCBuilder()

            protected ULCComponent createStartupMainContent() {
                builder.label('Content Goes Here')
            }

            protected void initFrame(ULCFrame frame) {
                super.initFrame(frame)
                frame.setLocationRelativeTo(null)
            }
        }


    You'll notice the `createStartupMainContentMethod()`. This is the only method
you must implement to get an ULC application running. The template suggest you
using **ULCBuilder** to create the UI elements. This builder works in the same
way that Groovy's SwingBuilder does; if you're familiar with Grails builders
and a bit of Swing then you're good to go.

5. Run the application. This can be achieved in two modes: development or
production mode. The difference strives in the need of a runtime license for the
latter mode. This runtime license should have been downloaded in the second step.
Running the application in development mode is as easy as invoking

        grails run-app-ulc

    This command will package the application and bootstrap it using a special
launcher. Running the application in production mode should be done by packaging
the whole application in a war. For this grails offers two choices once more:

        grails run-war

    Packages the application as a war and runs it inside an embedded container.

        grails war

    Packages the application as a war. You're tasked with deploying this file to an
application server of your choice.

   To start your ULC application either point your browser to "grailsApp/&lt;alias&gt;.jnlp"
   or create a gsp with the ulc applet tag provided by the included TagLib
   
         <ulc:applet name='<alias>' />

6. To purge all created artifacts you can use the command

        grails clean


This is all that you need to get started developing an ULC application for Grails.
You will find more information about ULC and available components by browsing
the PDF files that are bundled with the plugin.
Additional information in form of videos, sample applications, testimonials and
forums can be reached by pointing your browser to
<http://www.canoo.com/ulc>

Happy coding!

Demo application
================

You can install an example ULC application for browsing your domain objects.

      grails install-domain-viewer
   
   A complete ULC application with the alias 'ulcdomainviewer' will be generated
   into your grails application. For running this application you can use
   
      grails run-app-ulc ulcdomainviewer
   
   or start you grails application an navigate your browser to 

    .../your-appname/ulcdomainviewer.jnlp
    
   or point your browser to
   
    .../your-appname/domainviewer/ulcdomainviewer.gsp

   TODO: The demo application will not be removed if you decide to uninstall the ulc plugin.
   You have to delete the artifacts manually!
