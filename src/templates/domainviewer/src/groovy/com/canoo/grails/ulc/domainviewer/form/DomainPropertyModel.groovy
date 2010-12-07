package com.canoo.grails.ulc.domainviewer.form

import com.canoo.grails.ulc.domainviewer.form.DomainClassFormModel
import com.canoo.grails.ulc.domainviewer.form.IDomainPropertyModel
import groovy.beans.Bindable
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

public class DomainPropertyModel implements IDomainPropertyModel, PropertyChangeListener {

    @Bindable def value

    boolean dirty

    String path

    @Delegate GrailsDomainClassProperty domainClassProperty

    DomainClassFormModel formModel

    public void propertyChange(PropertyChangeEvent evt) {
        def newValue = evt.newValue
        if (newValue) {
            path.tokenize(".").each {newValue = newValue ? newValue[it] : null}
        }
        setValue(newValue)
    }

    public def getValue() {
        if (dirty) {
            return value
        }
        return formModel.resolvePropertyValue(path)
    }

    public void setValue(def newValue) {

        if (newValue != value) {
            dirty = true
            def oldValue = value
            value = newValue
            firePropertyChange("value", oldValue, newValue)
        }

    }
}