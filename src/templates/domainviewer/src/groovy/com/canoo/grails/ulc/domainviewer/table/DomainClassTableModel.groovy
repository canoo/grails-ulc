package com.canoo.grails.ulc.domainviewer.table

import com.ulcjava.base.application.table.AbstractTableModel
import grails.persistence.Event
import groovy.beans.Bindable
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator

public class DomainClassTableModel extends AbstractTableModel {
    // TODO (Dec 9, 2009, msh): selection is a view aspect. remove it from model
    @Bindable def selectedInstance

    private GrailsDomainClass domainClass
    private List entityIds
    private List domainClassProperties

    public DomainClassTableModel(GrailsDomainClass domainClass) {
        this.domainClass = domainClass

        def excludedProps = Event.allEvents.toList()
        def allowedNames = domainClass.persistentProperties*.name << 'id' << 'dateCreated' << 'lastUpdated'
        def props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) }
        Collections.sort(props, new DomainClassPropertyComparator(domainClass))
        props.add(1, domainClass.properties.find {it.name == GrailsDomainClassProperty.VERSION})

        this.domainClassProperties = props

        entityIds = domainClass.clazz.withCriteria {
            projections {
                property("id")
            }
        }

    }

    public int getRowCount() {
        return entityIds.size()
    }

    public int getColumnCount() {
        return domainClassProperties.size()
    }

    def Class getColumnClass(int columnIndex) {
        GrailsDomainClassProperty property = domainClassProperties[columnIndex]
        return returnRawValue(property.getType()) ? property.getType() : String
    }

    public Object getValueAt(int row, int column) {
        def entity = getEntityAt(row)
        GrailsDomainClassProperty property = domainClassProperties[column]
        def value = entity."${property.name}"

        if (returnRawValue(property.type)) {
            return value
        }
        return String.valueOf(value)
    }

    private boolean returnRawValue(Class columnClass) {
        return Number.isAssignableFrom(columnClass) || Date.isAssignableFrom(columnClass)
    }

    public String getColumnName(int column) {
        return domainClassProperties[column].naturalName
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false
    }

    public def getEntityAt(int index) {
        if (index < 0) return null
        domainClass.clazz.get(entityIds[index])
    }

    public void setSelectedIndex(int index) {
        setSelectedInstance(getEntityAt(index))
    }
}