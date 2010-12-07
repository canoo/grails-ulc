package com.canoo.grails.ulc.domainviewer

import com.canoo.grails.ulc.domainviewer.form.DomainClassFormModel
import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeBuilder
import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeModel
import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeNode
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass

public class DomainClassDetailModel {


    def instance

    DomainClassTreeModel treeModel

    private Map formModels = [:]

    public DomainClassDetailModel(GrailsDomainClass domainClass) {
        init(domainClass)
    }

    public DomainClassDetailModel(def domainInstance) {
        this.instance = domainInstance
        init(ApplicationHolder.application.getArtefact("Domain", instance.class.name))
    }

    protected DomainClassTreeModel init(GrailsDomainClass domainClass) {
        DomainClassTreeBuilder builder = new DomainClassTreeBuilder()
        DomainClassTreeNode root = builder.createHierarchyFrom(domainClass)
        treeModel = new DomainClassTreeModel(root)
    }

    public void setInstance(def newInstance) {
        this.instance = newInstance
        treeModel.domainInstance = instance
    }


    public DomainClassFormModel getFormModel(def domainInstance) {
        def model = formModels[domainInstance]
        if (!model) {
            model = new DomainClassFormModel(domainInstance)
            formModels[domainInstance] = model
        }

        return model
    }
}