package com.canoo.grails.ulc.domainviewer.form

import com.canoo.grails.ulc.domainviewer.form.DomainPropertyModel
import com.canoo.grails.ulc.domainviewer.form.IDomainPropertyModel
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

public class DomainPropertyModelProxy implements IDomainPropertyModel, PropertyChangeListener {

    DomainPropertyModel propertyModel

    @Delegate PropertyChangeSupport pcs = new PropertyChangeSupport(this)

    public DomainPropertyModelProxy(DomainPropertyModel propertyModel) {
        this.propertyModel = propertyModel
        this.propertyModel.addPropertyChangeListener("value", this)
    }

    public Object getValue() {
        return propertyModel.value
    }

    public void setValue(Object value) {
        propertyModel.value = value
    }

    public String getPath() {
        return propertyModel.getPath()
    }

    public GrailsDomainClassProperty getDomainClassProperty() {
        return propertyModel.domainClassProperty
    }

    public boolean isDirty() {
        return propertyModel.isDirty()
    }


    public void setPropertyModel(DomainPropertyModel newModel) {
        propertyModel?.removePropertyChangeListener("value", this)
        newModel.addPropertyChangeListener("value", this)

        def oldValue = propertyModel?.value
        def newValue = newModel.value

        this.propertyModel = newModel

        firePropertyChange("value", oldValue, newValue)
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.propertyName, evt.oldValue, evt.newValue)
    }


}