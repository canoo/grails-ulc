package com.canoo.grails.ulc.domainviewer.tree

import com.canoo.grails.ulc.domainviewer.tree.DomainClassTreeModel
import com.ulcjava.base.application.ULCScrollPane
import com.ulcjava.base.application.ULCTree
import com.ulcjava.base.application.event.TreeSelectionEvent
import groovy.beans.Bindable

public class DomainClassTreeView {

    def content
    @Bindable selectedInstance

    private DomainClassTreeModel treeModel

    public DomainClassTreeView(DomainClassTreeModel treeModel) {
        this.treeModel = treeModel
        ULCTree tree = new ULCTree(treeModel)
        content = new ULCScrollPane(tree)

        tree.valueChanged = {TreeSelectionEvent event ->
            Object instanceNode = event.path.lastPathComponent
            setSelectedInstance(treeModel.getNodeInstance(instanceNode))
        }

    }


}