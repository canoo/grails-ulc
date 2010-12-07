package com.canoo.grails.ulc.domainviewer

import com.canoo.groovy.ulc.ULCBuilder
import com.ulcjava.base.application.AbstractApplication
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.ULCFrame
import com.ulcjava.base.application.ULCRootPane
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import com.ulcjava.base.application.ULCBoxPane

class UlcDomainViewerApplication extends AbstractApplication {
    protected final ULCBuilder builder = new ULCBuilder()

    protected ULCComponent createMainContent() {

        def content = builder.tabbedPane() {
            ApplicationHolder.getApplication().getArtefacts("Domain").each {GrailsDomainClass domainClass ->
                createDomainClassTab(domainClass)
            }
        }

        return content

    }

    private def createDomainClassTab(GrailsDomainClass domainClass) {

        DomainClassMasterModel masterModel = new DomainClassMasterModel(domainClass)
        def view = new DomainClassMasterDetailView(masterModel)

        builder.boxPane(title: domainClass.name) {
            widget(view.content, constraints: ULCBoxPane.BOX_EXPAND_EXPAND)
        }
    }

    protected ULCRootPane createRootPane() {
        builder.frame(title: 'UlcDomainViewer', visible: true,size:[640,480],
                defaultCloseOperation: ULCFrame.TERMINATE_ON_CLOSE)
    }

    void start() {
        ULCRootPane rootPane = createRootPane()
        rootPane.add(createMainContent())
    }
}
