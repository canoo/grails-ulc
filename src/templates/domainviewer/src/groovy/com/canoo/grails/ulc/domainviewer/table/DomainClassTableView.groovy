package com.canoo.grails.ulc.domainviewer.table

import com.ulcjava.base.application.ULCScrollPane
import com.ulcjava.base.application.ULCTable
import groovy.beans.Bindable

public class DomainClassTableView {

    @Bindable def selectedRow


    def content

    private DomainClassTableModel tableModel

    public DomainClassTableView(DomainClassTableModel tableModel) {
        this.tableModel = tableModel
        ULCTable table = new ULCTable(tableModel)
        (0..1).each {
            def column = table.getColumnModel().getColumn(it)
            column.minWidth = 20
            column.preferredWidth = 40
            column.maxWidth = 60
        }

        content = new ULCScrollPane(table)

        table.selectionModel.valueChanged = {event ->
            setSelectedRow(table.selectedRow)
        }
    }
}