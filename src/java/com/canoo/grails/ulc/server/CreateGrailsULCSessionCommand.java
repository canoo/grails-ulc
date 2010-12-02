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
            return configureSession(config.getSessionProvider(containerServices).createSession());
        }
        return configureSession(new GrailsULCSession(applicationClassName, containerServices));
    }
    
    private ULCSession configureSession(ULCSession session) {
        session.addRoundTripListener(new IRoundTripListener() {
            private ApplicationContext getApplicationContext() {
                return ApplicationHolder.getApplication().getMainContext();
            }
            
            @Override
            public void roundTripWillEnd(RoundTripEvent event) {
                for (PersistenceContextInterceptor interceptor : getApplicationContext()
                        .getBeansOfType(PersistenceContextInterceptor.class).values()) {
                    interceptor.flush();
                    interceptor.destroy();
                }
            }
            
            @Override
            public void roundTripDidStart(RoundTripEvent event) {
                for (PersistenceContextInterceptor interceptor : getApplicationContext()
                        .getBeansOfType(PersistenceContextInterceptor.class).values()) {
                    interceptor.init();
                }
            }
        });
        return session;
    }
}