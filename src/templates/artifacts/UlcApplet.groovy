@artifact.package@import com.ulcjava.base.application.ULCRootPane
import com.ulcjava.environment.applet.application.ULCAppletPane

class @artifact.name@ extends @application.name@ {
    protected ULCRootPane createRootPane() {
        ULCAppletPane.getInstance()
    }
}
