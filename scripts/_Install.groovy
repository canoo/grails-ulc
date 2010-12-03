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

includeTargets << new File("${ulcPluginDir}/scripts/InstallLicense.groovy")

ant.mkdir(dir: "${basedir}/grails-app/ulc")
ant.mkdir(dir: "${basedir}/lib/ulc-client")
ant.mkdir(dir: "${basedir}/src/ulc-client/common")
updateMetadata('app.ulc.version': '7.0.1')

ant.mkdir(dir: "${basedir}/ulc-signatures")
ant.copy(todir: "${basedir}/ulc-signatures") {
    fileset(dir: "${ulcPluginDir}/src/templates/signatures")
}
ant.mkdir(dir: "${basedir}/ulc-templates")
ant.copy(todir: "${basedir}/ulc-templates") {
    fileset(dir: "${ulcPluginDir}/src/templates/launchers")
}

try {
    installLicense()
}catch(Exception x) {
    System.exit(1)
}

println('*' * 80)
println("*${' '.center(78,' ')}*")
println("*${'Welcome to Canoo RIA Suite plugin for Grails'.center(78,' ')}*")
println("*${' '.center(78,' ')}*")
println('*' * 80)
println """Licenses are located in $ulcLicenseDir
You'll find full PDF guides in ${ulcPluginDir.absolutePath}/docs

Please report any issues you may find to the plugin's issue tracker located at

    http://github.com/canoo/grails-ulc/issues

Comments? Questions? Please go to the grails-user mailing list and post, we'll be
more than happy to answer them.

    http://xircles.codehaus.org/lists/user@grails.codehaus.org

Additional information about Canoo RIA Suite in form of videos, sample applications,
testimonials and forums can be reached by pointing your browser to

    http://www.canoo.com/ulc
"""
