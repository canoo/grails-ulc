package com.canoo.grails.ulc.domainviewer.form

import com.canoo.grails.ulc.domainviewer.WidgetFactory
import com.canoo.groovy.ulc.ULCBuilder
import com.ulcjava.base.application.BorderFactory
import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.ULCLabel

public class DomainClassFormView {

    ULCComponent content
    DomainClassFormModel model
    ULCBuilder builder = new ULCBuilder()
    def propertyModelMap = [:]

    private List propertyModels


    public DomainClassFormView(DomainClassFormModel model) {
        this.model = model

        propertyModels = model.domainPropertyModels.collect {new DomainPropertyModelProxy(it)}
        propertyModels.each {
            propertyModelMap[it.path] = it
        }

        content = createContent(propertyModels)
//        ResourceMap resourceMap = Application.instance.context.getResourceMap(this.getClass())
//        resourceMap.injectComponents(content)
    }

    protected ULCComponent createContent(List propertyModels) {

        return builder.boxPane(columns: 2, horizontalGap: 5, verticalGap: 10, border: BorderFactory.createEmptyBorder(5, 5, 5, 5)) {
            propertyModels.each {propertyModel ->
                addInputRow(propertyModel)
            }
        }
    }

    protected def addInputRow(IDomainPropertyModel propertyModel) {
        builder.label(name: getLabelName(propertyModel.path), text: getLabelText(propertyModel), constraints: ULCBoxPane.BOX_LEFT_CENTER)
        builder.widget(createWidget(propertyModel), constraints: ULCBoxPane.BOX_EXPAND_TOP)
    }

    private String getLabelText(IDomainPropertyModel propertyModel) {
        return propertyModel.getDomainClassProperty().getNaturalName()
    }

    protected def addInputRow(String propertyName) {
        addInputRow(propertyModelMap[propertyName])
    }

    protected def addInputRows(String regex) {
        propertyModels.findAll { it.path ==~ regex}.each {
            addInputRow(it)
        }
    }


    public void setModel(DomainClassFormModel newModel) {
        this.model = newModel
        this.model.domainPropertyModels.each {
            def proxy = propertyModelMap[it.path]
            proxy?.propertyModel = it
        }
    }


    protected ULCComponent createWidget(IDomainPropertyModel property) {
        WidgetFactory.createWidget(property)
    }

    private String getInputFieldName(String propertyPath) {
        return "${propertyPath}.Input"
    }

    private String getLabelName(String propertyPath) {
        return "${propertyPath}.Label"
    }

}