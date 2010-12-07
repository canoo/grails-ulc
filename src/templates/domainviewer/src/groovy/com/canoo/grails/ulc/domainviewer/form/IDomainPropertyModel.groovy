package com.canoo.grails.ulc.domainviewer.form

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

public interface IDomainPropertyModel {

    public String getPath()

    public GrailsDomainClassProperty getDomainClassProperty()

    public def getValue()

    public void setValue(def newValue)

    public boolean isDirty()
}