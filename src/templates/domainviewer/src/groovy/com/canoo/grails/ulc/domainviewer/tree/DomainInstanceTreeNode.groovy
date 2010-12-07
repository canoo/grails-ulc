package com.canoo.grails.ulc.domainviewer.tree

import com.ulcjava.base.application.tree.DefaultMutableTreeNode

public class DomainInstanceTreeNode extends DefaultMutableTreeNode {

    Class instanceClass
    def instanceId

    public DomainInstanceTreeNode(instance) {
        super(instance)
        instanceClass = instance.class
        instanceId = instance.id
    }

    public Object getUserObject() {
        instanceClass.get(instanceId)// TODO (Dec 2, 2009, msh): user service to get instance
    }

    public String toString() {
        getUserObject().toString()
    }


}