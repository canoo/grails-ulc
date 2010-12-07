package com.canoo.grails.ulc.domainviewer

import com.ulcjava.base.application.ULCRootPane
import com.ulcjava.environment.applet.application.ULCAppletPane

class UlcDomainViewerApplet extends UlcDomainViewerApplication {
    protected ULCRootPane createRootPane() {
        ULCAppletPane.getInstance()
    }
}
