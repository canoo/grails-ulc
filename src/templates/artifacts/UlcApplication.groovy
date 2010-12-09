@artifact.package@import com.ulcjava.base.application.AbstractApplication
import com.ulcjava.base.application.ULCRootPane
import com.ulcjava.base.application.ULCFrame
import com.ulcjava.base.application.ULCComponent
import com.canoo.groovy.ulc.ULCBuilder

class @artifact.name@ extends AbstractApplication {
    protected final ULCBuilder builder = new ULCBuilder()
    
    protected ULCComponent createMainContent() {
        builder.label('Content Goes Here')
    }

    protected ULCRootPane createRootPane() {
        builder.frame(title: '@artifact.name@', size:[640,480],
            defaultCloseOperation: ULCFrame.TERMINATE_ON_CLOSE)
    }

    void start() {
        ULCRootPane rootPane = createRootPane()
        rootPane.add(createMainContent())
        rootPane.visible = true
    }
}
