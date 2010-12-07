package com.canoo.grails.ulc.domainviewer.form

import com.canoo.grails.ulc.domainviewer.form.DomainPropertyModel
import groovy.beans.Bindable
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator

public class DomainClassFormModel implements PropertyChangeListener {

    @Bindable def instance

    final GrailsDomainClass domainClazz

    List technicalFields = ["id", "version"]

    private includeTechnicalFields
    private List domainPropertyModelList
    private List referencedDomainClassModels = []
    private String path

    public DomainClassFormModel(def domainInstance) {
        this(ApplicationHolder.application.getArtefact("Domain", domainInstance.class.name), "", false)
        setInstance(domainInstance)
    }

    public DomainClassFormModel(GrailsDomainClass domainClass, String path = "", boolean includeTechnicalFields = false) {
        this.domainClazz = domainClass
        this.path = path
        this.includeTechnicalFields = includeTechnicalFields
        getDomainPropertyModels()
    }


    public List getDomainPropertyModels() {
        if (!domainPropertyModelList) {
            domainPropertyModelList = createDomainPropertyModels(domainClazz, path)
        }

        return domainPropertyModelList
    }

    protected List createDomainPropertyModels(GrailsDomainClass domainClass, String path, def usedDomainClasses = []) {
        List domainPropertyModelList = []

        List domainProperties = getDomainClassProperties(domainClass)

        domainProperties.each {GrailsDomainClassProperty property ->
            String nextPath = path.length() == 0 ? property.name : "${path}.${property.name}"
            if (!property.association) {
                domainPropertyModelList << createDomainPropertyModel(property, nextPath)
            } else {
                // 1-1 -> follow
                if (property.isOneToOne() && !usedDomainClasses.contains(property.referencedDomainClass)) {
                    usedDomainClasses << property.referencedDomainClass
                    domainPropertyModelList.addAll(createDomainPropertyModels(property.referencedDomainClass, nextPath, usedDomainClasses))
                }

                if (property.isOneToMany()) {
                    domainPropertyModelList << createDomainPropertyModel(property, nextPath)
                }
            }
        }


        return domainPropertyModelList
    }

    private DomainPropertyModel createDomainPropertyModel(GrailsDomainClassProperty property, String nextPath) {
        DomainPropertyModel propertyModel = new DomainPropertyModel(path: nextPath, domainClassProperty: property, formModel: this)
//        addPropertyChangeListener("instance", propertyModel)
        return propertyModel
    }

    private List getDomainClassProperties(GrailsDomainClass domainClass) {
        List domainProperties = domainClass.getProperties() as List

        if (!includeTechnicalFields) {
            Collection technicalProperties = domainProperties.findAll {technicalFields.contains(it.name)}
            domainProperties.removeAll(technicalProperties)
        }
        Collections.sort(domainProperties, new DomainClassPropertyComparator(domainClass))

        return domainProperties
    }

    public Map getPropertyValues() {
        Map values = [:]
        domainPropertyModels.each {DomainPropertyModel propertyModel ->
            values[propertyModel.path] = propertyModel.value
        }
        return values
    }


    public void propertyChange(PropertyChangeEvent evt) {
        setInstance(evt.newValue)
    }


    public def resolvePropertyValue(String path) {
        def propertyValue
        if (instance) {

            propertyValue = instance.class.get(instance.id)
            path.tokenize(".").each {
                propertyValue = propertyValue ? propertyValue[it] : null
            }
        }
        return propertyValue
    }

}