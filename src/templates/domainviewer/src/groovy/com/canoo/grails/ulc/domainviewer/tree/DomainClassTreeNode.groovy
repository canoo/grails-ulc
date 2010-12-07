package com.canoo.grails.ulc.domainviewer.tree

import com.ulcjava.base.application.tree.DefaultMutableTreeNode
import org.codehaus.groovy.grails.commons.GrailsDomainClass

public class DomainClassTreeNode extends DefaultMutableTreeNode {

    GrailsDomainClass domainClass
    String propertyName

    public DomainClassTreeNode(domainClass, propertyName) {
        super(propertyName, false)
        this.domainClass = domainClass
        this.propertyName = propertyName
    }

    public String toString() {
        propertyName.toUpperCase()
    }


}