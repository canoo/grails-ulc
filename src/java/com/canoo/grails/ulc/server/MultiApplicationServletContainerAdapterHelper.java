package com.canoo.grails.ulc.server;

import com.ulcjava.base.server.IContainerServices;
import com.ulcjava.base.server.IServerConfiguration;
import com.ulcjava.base.shared.streamcoder.CoderConfiguration;
import com.ulcjava.container.servlet.server.ContainerCommand;
import com.ulcjava.container.servlet.server.ServletContainerAdapterHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class MultiApplicationServletContainerAdapterHelper extends ServletContainerAdapterHelper {

    public MultiApplicationServletContainerAdapterHelper(ServletConfig servletConfig) {
        super(servletConfig);
    }

    protected IServerConfiguration createServerConfiguration(ServletConfig servletConfig) {
        ULCApplicationHolder.init(servletConfig.getServletContext());
        return new UlcApplicationServletConfig(servletConfig);
    }


    protected ContainerCommand createStartCommand() throws ServletException {
        return new CreateGrailsULCSessionCommand(this);
    }


    /**
     * Retrieves the configuration from the Servlet Init Parameters
     */
    private static class UlcApplicationServletConfig implements IMultipleApplicationServerConfiguration {
        private static final String APPLICATION_ALIAS_KEY = "application-alias";
        private static final String IS_APPLET_KEY = "is-applet";
        
        private final String applicationAlias;
        private final String applicationClassName;
        private final boolean isApplet;
        
        private UlcApplicationServletConfig(ServletConfig servletConfig) {
            applicationAlias = servletConfig.getInitParameter(APPLICATION_ALIAS_KEY);
            String b = servletConfig.getInitParameter(IS_APPLET_KEY);
            isApplet = b != null && b.trim().length() > 0 ? Boolean.parseBoolean(b) : false;
            applicationClassName = ULCApplicationHolder.getApplicationClassName(applicationAlias, isApplet);
        }
        
        public String getApplicationAlias() {
            return applicationAlias;
        }
        
        public boolean isApplet() {
            return isApplet;
        }
        
        public String getApplicationClassName() {
            return applicationClassName;
        }
        
        public String getCarrierStreamProviderClassName() {
            return currentServletConfig().getInitParameter(IContainerServices.CARRIER_STREAM_PROVIDER_KEY);
        }
        
        public String getServerCoderRegistryProviderClassName() {
            return currentServletConfig().getInitParameter(IContainerServices.SERVER_CODER_REGISTRY_PROVIDER_KEY);
        }
        
        public String getDataStreamProviderClassName() {
            return currentServletConfig().getInitParameter(IContainerServices.DATA_STREAM_PROVIDER_KEY);
        }
        
        public String getServerLogLevel() {
            return currentServletConfig().getInitParameter(IContainerServices.LOG_LEVEL_KEY);
        }
        
        public CoderConfiguration[] getServerCoderConfigurations() {
            return null;
        }
        
        public String getClientResourceHandlerClassName() {
            return currentServletConfig().getServletContext().getInitParameter(IContainerServices.CLIENT_RESOURCE_HANDLER_CLASS_NAME_KEY);
        }
        
        public String getClientResourcesDirectory() {
            return currentServletConfig().getServletContext().getInitParameter(IContainerServices.CLIENT_RESOURCES_DIRECTORY_KEY);
        }
        
        public String getClientResourcesPattern() {
            return currentServletConfig().getServletContext().getInitParameter(IContainerServices.CLIENT_RESOURCES_PATTERN_KEY);
        }
    }
}
