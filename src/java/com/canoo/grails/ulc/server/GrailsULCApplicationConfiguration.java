package com.canoo.grails.ulc.server;

import com.ulcjava.base.server.ApplicationConfiguration;
import com.ulcjava.base.server.OpenApplicationConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GrailsULCApplicationConfiguration extends OpenApplicationConfiguration {
    private static final long serialVersionUID = 500782197706238987L;
    private static final Map<String, String> aliasToClassname = new LinkedHashMap<String, String>();
    private static final Map<String, GrailsULCApplicationConfiguration> aliasToConfig = new LinkedHashMap<String, GrailsULCApplicationConfiguration>();
    
    synchronized public static final void setAliases(Map<String, String> aliases) {
        if (aliases != null && !aliases.isEmpty()) {
            aliasToClassname.putAll(aliases);
        }
    }

    synchronized public static Map<String, String> getAliases() {
        return Collections.<String, String> unmodifiableMap(aliasToClassname);
    }

    synchronized public static final String getApplicationAlias(String className) {
        for (Map.Entry<String, String> entry : aliasToClassname.entrySet()) {
            if (entry.getValue().equals(className)) {
                return entry.getKey();
            }
        }
        return null;
    }

    synchronized public static final ApplicationConfiguration getInstance(String alias) {
        GrailsULCApplicationConfiguration config = aliasToConfig.get(alias);
        if (config == null && aliasToClassname.containsKey(alias)) {
            config = new GrailsULCApplicationConfiguration(toResource(aliasToClassname.get(alias)));
        }
        return config != null && config.isLoaded() ? config : null;
    }

    private static String toResource(String className) {
        String resourceName = className.replace('.', '/');
        return "/" + resourceName + "UlcConfiguration.xml";
    }

    GrailsULCApplicationConfiguration(String applicationConfigurationResource) {
        super(applicationConfigurationResource);
    }
}