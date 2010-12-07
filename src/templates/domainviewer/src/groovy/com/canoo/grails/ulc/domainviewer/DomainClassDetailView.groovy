package com.canoo.grails.ulc.domainviewer

import com.canoo.grails.ulc.domainviewer.form.DomainClassFormModel
import com.canoo.grails.ulc.domainviewer.form.DomainClassFormView
import com.canoo.grails.ulc.domainviewer.form.FormViewFactory
import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeView
import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCCardPane
import com.ulcjava.base.application.ULCScrollPane
import com.ulcjava.base.application.ULCSplitPane
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.codehaus.groovy.grails.commons.GrailsDomainClass

public class DomainClassDetailView implements PropertyChangeListener {

    def content
    DomainClassDetailModel model

    ULCCardPane formViewCardPane
    Map formViews = [:]

    public DomainClassDetailView(DomainClassDetailModel model) {
        this.model = model
        content = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT)
        content.setDividerLocation(new Double(0.33d))
        DomainClassTreeView treeView = new DomainClassTreeView(model.treeModel)
        formViewCardPane = new ULCCardPane()

        content.leftComponent = treeView.content
        content.rightComponent = new ULCScrollPane(formViewCardPane);

        treeView.addPropertyChangeListener("selectedInstance", this)
    }

    public void propertyChange(PropertyChangeEvent evt) {
        selectCard(evt.newValue)
    }


    private void selectCard(def instance) {
        String key = instanceKey(instance)
        if (!(formViewCardPane.names as List).contains(key)) {
            DomainClassFormModel formModel = model.getFormModel(instance)
            DomainClassFormView view = FormViewFactory.createFormView(formModel)
            formViews[key] = view
            formViewCardPane.addCard(ULCBoxPane.BOX_EXPAND_TOP, key, view.content)
        } else {
            formViews[key].model = model.getFormModel(instance)
        }

        formViewCardPane.selectedName = instanceKey(instance)
    }

    private String instanceKey(def instance) {
        return "${instance.class.name}"
    }

    private String instanceKey(GrailsDomainClass instance) {
        return "${instance.name}"
    }

}