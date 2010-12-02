package com.canoo.grails.ulc.server;

import com.ulcjava.base.server.IServerApplicationListener;
import com.ulcjava.base.server.IServerConfiguration;
import com.ulcjava.base.server.ServerApplicationEvent;

public class GrailsServerApplicationListener implements IServerApplicationListener {
    @Override
    public void applicationInitialized(ServerApplicationEvent event) {
        Object source = event.getSource();
        String alias = null;
        
        if(source instanceof MultiApplicationServletContainerAdapterHelper) {
            MultiApplicationServletContainerAdapterHelper adapterHelper = (MultiApplicationServletContainerAdapterHelper) source;
            IMultipleApplicationServerConfiguration config = (IMultipleApplicationServerConfiguration) adapterHelper.getConfiguration();
            alias = config.getApplicationAlias();
        } else {
            // must be running in Development mode
            // assume -Dgrails.ulc.application.alias was set
            alias = System.getProperty("grails.ulc.application.alias");
        }
        
        ULCApplicationHolder.setCoderRegistry(alias, event.getCoderRegistry());
    }
}