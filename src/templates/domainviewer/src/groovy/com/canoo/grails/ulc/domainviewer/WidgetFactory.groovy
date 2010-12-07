package com.canoo.grails.ulc.domainviewer

import com.canoo.grails.ulc.domainviewer.form.DomainPropertyModel
import com.canoo.grails.ulc.domainviewer.form.IDomainPropertyModel
import com.canoo.groovy.ulc.ULCBuilder
import java.beans.PropertyChangeListener
import com.ulcjava.base.application.*

public class WidgetFactory {

    static ULCComponent createWidget(IDomainPropertyModel propertyModel) {
        ULCComponent widget = createWidget(propertyModel.domainClassProperty.type)
        widget.name = "${propertyModel.path}.input"
        bindWidget(widget, propertyModel, new ULCBuilder())
        return widget
    }


    static ULCComponent createWidget(Class propertyType) {
        def widget
        switch (propertyType) {
            case Boolean:
                widget = new ULCCheckBox()
                break
            case String:
                widget = new ULCTextField(columns: 20)
                break
            case Collection:
                widget = new ULCList(visibleRowCount: 4)
                break
            default:
                widget = new ULCTextField()
        }
        return widget
    }

    private static void bindWidget(ULCTextField textField, IDomainPropertyModel propertyModel, builder) {
        textField.text = propertyModel.value
        builder.bind(target: propertyModel, targetProperty: "value", source: textField, sourceProperty: "text")
        builder.bind(target: textField, targetProperty: "text", source: propertyModel, sourceProperty: "value")
    }

    private static void bindWidget(ULCCheckBox checkBox, IDomainPropertyModel propertyModel, builder) {
        checkBox.selected = propertyModel.value
        builder.bind(target: propertyModel, targetProperty: "value", source: checkBox, sourceProperty: "selected")
        builder.bind(target: checkBox, targetProperty: "selected", source: propertyModel, sourceProperty: "value")
    }

    private static void bindWidget(ULCList list, IDomainPropertyModel propertyModel, builder) {
        String[] elements = propertyModel.value*.toString() ?: []
        DefaultListModel listModel = new DefaultListModel(elements)
        propertyModel.addPropertyChangeListener("value", [
                propertyChange: {evt ->
                    // TODO (Dec 8, 2009, msh): create DomainClassListModel

                    listModel.clear()
                    def newValue = evt.newValue
                    if (newValue) {
                        def listElements = newValue*.toString()
                        listModel.addAll(listElements as Object[])
                    }

                }
        ] as PropertyChangeListener)
        list.model = listModel
    }

    private static void bindWidget(ULCComponent component, DomainPropertyModel propertyModel, builder) {

    }
}