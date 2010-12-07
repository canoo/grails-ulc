package com.canoo.grails.ulc.domainviewer.form

import com.canoo.grails.ulc.domainviewer.form.DomainClassFormModel
import com.canoo.grails.ulc.domainviewer.form.DomainClassFormView
import org.codehaus.groovy.grails.commons.ApplicationHolder

public class FormViewFactory {

    static DomainClassFormView createFormView(DomainClassFormModel formModel) {
        def viewClass
        def configuredViews = ApplicationHolder.application.config.formViewCardPane
        if (configuredViews) {
            viewClass = configuredViews[formModel.domainClazz.clazz.simpleName]
        }

        if (viewClass) {
            return viewClass.newInstance([formModel] as Object[])
        }
        return new DomainClassFormView(formModel)
    }

}