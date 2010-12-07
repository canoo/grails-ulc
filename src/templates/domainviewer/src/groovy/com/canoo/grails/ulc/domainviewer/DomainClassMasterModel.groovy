package com.canoo.grails.ulc.domainviewer

import com.canoo.grails.ulc.domainviewer.DomainClassDetailModel
import com.canoo.grails.ulc.domainviewer.table.DomainClassTableModel
import groovy.beans.Bindable
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass

public class DomainClassMasterModel {

    private GrailsDomainClass domainClass

    DomainClassTableModel tableModel
    DomainClassDetailModel detailModel


    @Bindable def selectedDetailInstance

    public DomainClassMasterModel(Class clazz) {
        this(ApplicationHolder.application.getArtefact("Domain", clazz.name))
    }


    public DomainClassMasterModel(GrailsDomainClass domainClass) {
        this.domainClass = domainClass
        tableModel = new DomainClassTableModel(domainClass)
        detailModel = new DomainClassDetailModel(domainClass)
    }

    public setSelectedDetail(selectedInstance) {
        setSelectedDetailInstance(selectedInstance)
        detailModel.instance = selectedInstance
    }

}