package com.canoo.grails.ulc.server;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.ulcjava.base.application.IApplication;
import com.ulcjava.base.server.IContainerServices;
import com.ulcjava.base.server.ULCSession;

public class GrailsULCSession extends ULCSession {
    public GrailsULCSession(String applicationClassName, IContainerServices containerServices) {
        super(applicationClassName, containerServices);
    }
    
    @Override
    protected IApplication createApplication() {
        IApplication app = super.createApplication();
        performInjections(app);
        return app;
    }
    
    private void performInjections(IApplication app) {
        ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
        ctx.getAutowireCapableBeanFactory().autowireBeanProperties(app, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
}
