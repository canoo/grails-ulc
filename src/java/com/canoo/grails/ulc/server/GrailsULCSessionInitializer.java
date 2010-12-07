package com.canoo.grails.ulc.server;

import com.ulcjava.base.application.event.IRoundTripListener;
import com.ulcjava.base.application.event.RoundTripEvent;
import com.ulcjava.base.server.IContainerServices;
import com.ulcjava.base.server.ULCSession;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.springframework.context.ApplicationContext;

public class GrailsULCSessionInitializer {

    static ULCSession createSession(String applicationClassName, IContainerServices containerServices) {
        return configureSession(new GrailsULCSession(applicationClassName, containerServices));
    }

    private static ULCSession configureSession(ULCSession session) {
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
