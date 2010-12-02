package com.canoo.grails.ulc.server;

import com.ulcjava.base.server.DefaultSessionProvider;
import com.ulcjava.base.server.ULCSession;

public class GrailsULCSessionProvider extends DefaultSessionProvider {
    @Override
    public ULCSession createSession() {
        return new GrailsULCSession(getApplicationClassName(), getContainerServices());
    }
}