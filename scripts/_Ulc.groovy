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

import java.text.DateFormat
import java.text.SimpleDateFormat
import groovy.swing.SwingBuilder
import java.util.concurrent.CountDownLatch
import javax.swing.JFrame

dateFormat = new SimpleDateFormat('yyyy-MM-dd')

parseLicenseText = { text ->
    def moduleType = ''
    def productVersion = ''
    def licenseName = ''
    def license = []
    def licenses = [:]

    text.eachLine { line ->
        if(line == '--- START MODULE ---') {
            license = []
            licenseName = ''
            moduleType = ''
            productVersion = ''
        }
        license << line
        
        if(line =~ /module-type=/) {
            moduleType = line.substring('module-type='.length())
        }
        if(line =~ /product-version=/) {
            productVersion = line.substring('product-version='.length())
        }
        if(moduleType && productVersion) {
            productVersion = productVersion.collect([]){c -> c =~/[0-9]/ ? c : ''}.join()
            if(productVersion.length() == 2) productVersion += '0'
            productVersion = productVersion.padLeft(4, '0')
            licenseName = moduleType +'-'+productVersion
        }
        
        if(line == '--- END MODULE ---' && licenseName) {
            licenses[licenseName] = license.join('\n')
        }
    }

    licenses
}

checkLicense = {
    downloadLicenseIfNeeded()
    checkLicenseExpirationDate()
}

downloadLicenseIfNeeded = {
    if(checkExistingLicense()) return
    if(checkLicenseToken()) return
    checkEvalLicense()
}

checkLicenseExpirationDate = {
    File developerLicense = ulcLicenseDir.listFiles().find{it.name =~ /^DEVELOPER-\d{4}\.lic$/}
    Properties license = readLicenseText(developerLicense.text)
    Date dateShipped = dateFormat.parse(license['date-shipped'])
    int period = license.get('evaluation-period')?.toInteger() ?: 0i
    Date expirationDate = dateShipped + period
    Date today = new Date()
    Date warningDate = expirationDate - 6
 
    expirationDate.clearTime()
    warningDate.clearTime()
    today.clearTime()

    // license has expired (check for evaluation only!)
    if(period != 0) {
        if(today.after(expirationDate)) {
            showLicenseExpiredWindow(expirationDate)
        } else if(today.after(warningDate)) {
            File ulcWarningCheck = new File(System.getProperty('user.home'), '.ulc-check')
            if(ulcWarningCheck.exists()) {
                Date when = dateFormat.parse(ulcWarningCheck.text.trim())
                when.clearTime()
                if(when == today) return
            }

            ulcWarningCheck.text = dateFormat.format(today)

            int days = today - warningDate
            showLicenseWarningWindow(days)
        } // OK!
    }
}

checkExistingLicense = {
    File userHome = new File(System.getProperty('user.home'))
    List ulcDirs = []
    userHome.eachDir{ if(it.name =~ /^.ulc-\d.\d[\.\d]?/) ulcDirs << it}
 
    // has license?
    if(!ulcDirs) return false

    // sort by version
    ulcDirs.sort{ a, b ->
        String v1 = (a.name =~ /(\d\.\d(\.\d)?)/)[0][1]
        String v2 = (b.name =~ /(\d\.\d(\.\d)?)/)[0][1]
        v1.toDouble() <=> v2.toDouble()
    }

    // pick latest one
    def candidateUlcLicenseDir = ulcDirs[-1]

    // check if version >= 7.0
    String version = (candidateUlcLicenseDir.name =~ /(\d.\d(\.\d)?)/)[0][1]
    if(version < '7.0') return

    ulcLicenseDir = candidateUlcLicenseDir
    true
}

checkLicenseToken = {
    ant.input(addProperty: "access.token", message: "Do you have a license access token? If so please enter it or type [ENTER] to proceed:")
    def token = ant.antProject.properties."access.token"

    if(!token?.trim()) return false

    def text = null
    try {
        text = "https://ulc.canoo.com/rest/license/promoCore/$token".toURL().text
        println 'Access token verified.'
    } catch(x) {
        // if(x.message =~ /.*HTTP response code: 500.*/)
        // any error means no valid token or already used
        println 'Access token is invalid or it has expired.'
        return false
    } 
    downloadLicense(text)
    true
}

checkEvalLicense = {
    String text = 'https://ulc.canoo.com/rest/license/ULC%20Core'.toURL().text
    println 'Downloading evaluation license...'
    downloadLicense(text)
}

downloadLicense = { text ->
    Map<String, String> licenses = parseLicenseText(text)
 
    String version = ''
    // search for core license
    for(key in licenses.keySet()) {
        if(key =~ /DEPLOYMENT-(\d+)/) {
            Properties p = readLicenseText(licenses[key])
            version = (String) p.get('product-version')
            break
        }
    }

    ulcLicenseDir = new File(System.getProperty('user.home'), ".ulc-${version}")
    ulcLicenseDir.mkdirs()
    licenses.each { k, v ->
        File licenseFile = new File(ulcLicenseDir.absolutePath, "${k}.lic")
        licenseFile.text = v
    }
}

readLicenseText = { String text ->
    Properties p = new Properties()
    p.load(new StringReader(text))
    p
}

showLicenseExpiredWindow = { expirationDate ->
    CountDownLatch latch = new CountDownLatch(1i)
    def swing = new SwingBuilder()
    swing.edt {
        frame(title: 'Canoo RIA Suite License EXPIRED!', pack: true, visible: true, resizable: false,
              defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE, id: 'frame',
              locationRelativeTo: null, windowClosed: { latch.countDown() }) {
            borderLayout()
            scrollPane(constraints: CENTER, preferredSize: [400, 250]) {
                textArea(editable: false, wrapStyleWord: true, lineWrap: true, text: "Your license has expired on ${dateFormat.format(expirationDate)}.\n\nWe urge you to get in touch with a Canoo sales representative to find out more about the different licensing options available for Canoo RIA Suite. Or you can click the 'Buy now' button.\n\n You can contact Canoo by sending a message to sales@canoo.com")
            }
            panel(constraints: SOUTH) {
                gridLayout(cols: 2, rows: 1)
                button('Buy now', actionPerformed: { openURL('http://www.canoo.com/ulc/') })
                button('Close', actionPerformed: {frame.dispose()})
            }
        }
    }

    latch.await()
}

showLicenseWarningWindow = { int days ->
    CountDownLatch latch = new CountDownLatch(1i)
    def swing = new SwingBuilder()
    swing.edt {
        frame(title: 'Canoo RIA Suite License Check', pack: true, visible: true, resizable: false,
              defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE, id: 'frame',
              locationRelativeTo: null, windowClosed: { latch.countDown() }) {
            borderLayout()
            scrollPane(constraints: CENTER, preferredSize: [400, 250]) {
                textArea(editable: false, wrapStyleWord: true, lineWrap: true, text: "Your license will expire in ${days} day${days == 1? '': 's'}.\n\nWe encourage you to get in touch with a Canoo sales representative to find out more about the different licensing options available for Canoo RIA Suite. Or you can click the 'Buy now' button.\n\n You can contact Canoo by sending a message to sales@canoo.com")
            }
            panel(constraints: SOUTH) {
                gridLayout(cols: 2, rows: 1)
                button('Buy now', actionPerformed: { openURL('http://www.canoo.com/ulc/') })
                button('Close', actionPerformed: {frame.dispose()})
            }
        }
    }

    latch.await()
}

/*
 * The following code was adapted from
 * http://www.centerkey.com/java/browser/
 */

String[] browsers = ["google-chrome", "firefox", "opera", "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla"] as String[]
String errMsg = "Error attempting to launch web browser";

private openURL(String url) {
    try { //attempt to use Desktop library from JDK 1.6+
        Class<?> d = Class.forName("java.awt.Desktop");
        d.getDesktop().browse()
        // d.getDeclaredMethod("browse", [URI] as Class[]).invoke(d.getDeclaredMethod("getDesktop").invoke(null), [URI.create(url)] as Object[]);
        //above code mimicks: java.awt.Desktop.getDesktop().browse()
    } catch (Exception ignore) { //library not available or failed
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class.forName("com.apple.eio.FileManager").openURL(url)
                // Class.forName("com.apple.eio.FileManager").getDeclaredMethod("openURL", [String] as Class[]).invoke(null, [url] as Object[]);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux 
                String browser = null;
                for (String b : browsers) {
                    if (browser == null && Runtime.getRuntime().exec(["which", b] as String[]).getInputStream().read() != -1) {
                        Runtime.getRuntime().exec([browser = b, url] as String[]);
                    }
                }
                if (browser == null) {
                    throw new Exception(Arrays.toString(browsers));
                }
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
        }
    }
}
