package com.canoo.grails.ulc.server;

import com.ulcjava.base.application.event.IRoundTripListener;
import com.ulcjava.base.application.event.RoundTripEvent;
import com.ulcjava.base.server.IContainerServices;
import com.ulcjava.base.server.ULCSession;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.springframework.context.ApplicationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GrailsULCSessionInitializer {
    private static final Log LOG = LogFactory.getLog(GrailsULCSessionInitializer.class);

    static ULCSession createSession(String applicationClassName, IContainerServices containerServices) {
        return configureSession(new GrailsULCSession(applicationClassName, containerServices));
    }

    private static ULCSession configureSession(ULCSession session) {
        session.addRoundTripListener(new IRoundTripListener() {
            private ApplicationContext getApplicationContext() {
                return ApplicationHolder.getApplication().getMainContext();
            }

            public void roundTripWillEnd(RoundTripEvent event) {
                for (PersistenceContextInterceptor interceptor : getApplicationContext()
                        .getBeansOfType(PersistenceContextInterceptor.class).values()) {
                    try {
                        interceptor.flush();
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("An error occurred while attempting to flush interceptor " + interceptor, e);
                        }
                    } finally {
                        try {
                            interceptor.destroy();
                        } catch (Exception e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("An error occurred while attempting to destroy interceptor " + interceptor, e);
                            }
                        }
                    }
                }
            }

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
