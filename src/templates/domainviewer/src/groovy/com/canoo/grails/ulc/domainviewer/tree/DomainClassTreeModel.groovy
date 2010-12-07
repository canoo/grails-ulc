package com.canoo.grails.ulc.domainviewer.tree

import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeNode
import com.canoo.grails.ulc.domainviewer.tree.DomainInstanceTreeNode
import com.ulcjava.base.application.tree.AbstractTreeModel
import com.ulcjava.base.application.tree.ITreeNode
import com.ulcjava.base.application.tree.TreePath

public class DomainClassTreeModel extends AbstractTreeModel {


    private DomainClassTreeNode schemaRoot
    private com.ulcjava.base.application.tree.ITreeNode root
    private def domainInstance


    public DomainClassTreeModel(DomainClassTreeNode schemaRoot) {
        this.schemaRoot = schemaRoot
        this.root = schemaRoot
    }

    public Object getRoot() {
        return root
    }

    public Object getChild(Object parent, int index) {
        return parent.getChildAt(index)
    }

    public int getChildCount(Object parent) {
        return parent.childCount
    }

    public boolean isLeaf(Object node) {
        return node.childCount == 0
    }

    public int getIndexOfChild(Object parent, Object child) {
        for (int i = 0; i < parent.childCount; i++) {
            if (parent.getChildAt(i) == child) {
                return i
            }
        }
        return -1
    }

    public def getNodeInstance(def node) {
        return node.userObject
    }

    public def getNodeInstance(DomainClassTreeNode node) {
        return node.domainClass
    }

    public void setDomainInstance(domainInstance) {
        if (domainInstance == null) {
            root = schemaRoot
        } else {
            root = buildInstanceTree(domainInstance)
        }
        nodeStructureChanged(new TreePath([root] as Object[]))
    }


    def buildInstanceTree(domainInstance) {
        def newRoot = createNode(domainInstance, schemaRoot)
        return newRoot
    }

    private def createNode(domainInstance, schemaNode) {
        if (domainInstance == null) {
            return null
        }
        def node = new DomainInstanceTreeNode(domainInstance)
        for (int i = 0; i < schemaNode.childCount; i++) {
            def schemaChildNode = schemaNode.getChildAt(i)
            def values = [domainInstance[schemaChildNode.propertyName]].flatten()
            values.each {
                DomainInstanceTreeNode childNode = createNode(it, schemaChildNode)
                if (childNode != null) {
                    node.add(childNode)
                }
            }
        }
        return node
    }


}