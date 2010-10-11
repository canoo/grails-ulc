@artifact.package@import com.ulcjava.applicationframework.application.SingleFrameApplication
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.ULCFrame
import com.canoo.groovy.ulc.ULCBuilder

class @artifact.name@ extends SingleFrameApplication {
    private final ULCBuilder builder = new ULCBuilder()

    protected ULCComponent createStartupMainContent() {
        builder.label('Content Goes Here')
    }

    protected void initFrame(ULCFrame frame) {
        super.initFrame(frame)
        frame.setLocationRelativeTo(null)
    }
}
