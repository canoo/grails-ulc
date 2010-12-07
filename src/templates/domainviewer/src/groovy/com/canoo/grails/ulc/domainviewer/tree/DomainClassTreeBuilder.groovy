package com.canoo.grails.ulc.domainviewer.tree

import com.canoo.groovy.ulc.tree.TreeNodeBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator

public class DomainClassTreeBuilder {

    TreeNodeBuilder builder
    Set usedDomainClasses

    Object createHierarchyFrom(Class aClass) {

        GrailsDomainClass domainClass = ApplicationHolder.getApplication().getArtefact("Domain", aClass.name)
        return createHierarchyFrom(domainClass)
    }

    Object createHierarchyFrom(GrailsDomainClass domainClass) {
        usedDomainClasses = new HashSet()
        return createNode(domainClass, domainClass)
    }


    private def createNode(GrailsDomainClass domainClass, String propertyName = null, GrailsDomainClass rootDomainClass, parent = null) {
        def nodeName = propertyName ? propertyName : domainClass.naturalName.split()[-1]
        DomainClassTreeNode node = createDomainClassTreeNode(nodeName, domainClass)
        if (parent) {
            parent.add(node)
        }
        def domainClassProperties = domainClass.properties as List
        Collections.sort(domainClassProperties, new DomainClassPropertyComparator(domainClass))

        domainClassProperties.each {GrailsDomainClassProperty property ->
            if (property.isOneToMany()) {
                if (findNode(node, property.getReferencedDomainClass()) != null) {
                    node.add(createDomainClassTreeNode(property.name, property.referencedDomainClass))
                } else {
                    createNode(property.getReferencedDomainClass(), property.name, rootDomainClass, node)
                }
            }
        }
        return node
    }

    private DomainClassTreeNode createDomainClassTreeNode(String nodeName, GrailsDomainClass domainClass) {
        usedDomainClasses << domainClass
        if (domainClass.hasSubClasses()) {
            usedDomainClasses.addAll(domainClass.subClasses)
        }
        return new DomainClassTreeNode(domainClass, nodeName)
    }


    def findNode(DomainClassTreeNode node, GrailsDomainClass domainClass) {
        if (node == null) {
            return null
        }
        if (node.parent == null) {
            return null
        }
        if (node.domainClass == domainClass) {
            return node
        }
        return findNode(node.parent, domainClass)
    }

}