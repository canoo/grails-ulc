package com.canoo.grails.ulc.domainviewer

import com.canoo.grails.ulc.domainviewer.DomainClassDetailView
import com.canoo.grails.ulc.domainviewer.DomainClassMasterModel
import com.canoo.grails.ulc.domainviewer.table.DomainClassTableView
import com.ulcjava.base.application.ULCSplitPane
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

public class DomainClassMasterDetailView implements PropertyChangeListener {

    def content

    private DomainClassMasterModel model

    public DomainClassMasterDetailView(DomainClassMasterModel model) {
        this.model = model

        DomainClassTableView masterView = new DomainClassTableView(model.tableModel)
        DomainClassDetailView detailView = getDetailView(model)

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.VERTICAL_SPLIT)
        splitPane.setDividerLocation(new Double(0.33d))
        splitPane.topComponent = masterView.content
        splitPane.bottomComponent = detailView.content

        masterView.addPropertyChangeListener("selectedRow", this)

        content = splitPane
    }

    protected def getDetailView(DomainClassMasterModel model) {
        return new DomainClassDetailView(model.detailModel)
    }

    public void propertyChange(PropertyChangeEvent evt) {
        model.selectedDetail = model.tableModel.getEntityAt(evt.newValue)
    }


}