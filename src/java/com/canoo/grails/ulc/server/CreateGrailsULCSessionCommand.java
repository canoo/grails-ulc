package com.canoo.grails.ulc.server;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.springframework.context.ApplicationContext;

import com.ulcjava.base.application.event.IRoundTripListener;
import com.ulcjava.base.application.event.RoundTripEvent;
import com.ulcjava.base.server.ApplicationConfiguration;
import com.ulcjava.base.server.IContainerServices;
import com.ulcjava.base.server.ISession;
import com.ulcjava.base.server.ULCSession;
import com.ulcjava.container.servlet.server.CreateSessionCommand;
import com.ulcjava.container.servlet.server.ICommandInfo;

public class CreateGrailsULCSessionCommand extends CreateSessionCommand {
    public CreateGrailsULCSessionCommand(ICommandInfo commandInfo) {
        super(commandInfo);
    }
    
    protected ISession createSession(String applicationClassName, IContainerServices containerServices) {
        String alias = GrailsULCApplicationConfiguration.getApplicationAlias(applicationClassName);
        ApplicationConfiguration config = GrailsULCApplicationConfiguration.getInstance(alias);
        if (config != null) {
            return config.getSessionProvider(containerServices).createSession();
        }
        return GrailsULCSessionInitializer.createSession(applicationClassName, containerServices);
    }
    
}