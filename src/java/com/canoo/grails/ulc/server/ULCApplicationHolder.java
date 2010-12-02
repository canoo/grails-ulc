package com.canoo.grails.ulc.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ulcjava.applicationframework.application.Application;
import com.ulcjava.base.shared.CoderRegistry;
import com.ulcjava.base.shared.logging.Level;
import com.ulcjava.base.shared.logging.Logger;

public class ULCApplicationHolder {
    private static final Map<String, String> ALIASES = new LinkedHashMap<String, String>();
    private static final String APPLET_PREFIX = "-applet.ulc";
    
    private static final Map<String, Application> APPLICATIONS = new LinkedHashMap<String, Application>();
    private static final Map<String, CoderRegistry> CODERS = new LinkedHashMap<String, CoderRegistry>();
    private static final Logger LOG = Logger.getLogger(ULCApplicationHolder.class);
    
    public static Application getApplication(HttpServletRequest request) {
        return getApplication(getApplicationAlias(request));
    }
    
    public static Application getApplication(String alias) {
        return alias != null ? APPLICATIONS.get(alias) : null;
    }
    
    public static String getApplicationAlias(Application application) {
        if (application == null) {
            return null;
        }
        return getApplicationAlias(application.getClass());
    }
    
    public static String getApplicationAlias(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        String alias = clazz.getSimpleName().toLowerCase();
        if (alias.endsWith("application")) {
            alias = alias.substring(0, alias.length() - 11);
        } else if (alias.endsWith("applet")) {
            alias = alias.substring(0, alias.length() - 6);
        }
        return alias;
    }
    
    public static String getApplicationAlias(HttpServletRequest request) {
        String path = request.getRequestURI();
        String alias = path.substring(path.lastIndexOf('/') + 1);
        if (alias.endsWith(APPLET_PREFIX)) {
            alias = alias.substring(0, alias.length() - 11);
        } else {
            alias = alias.substring(0, alias.length() - 4);
        }
        
        return alias;
    }
    
    public static String getApplicationClassName(HttpServletRequest request) {
        return getApplicationClassName(getApplicationAlias(request), isApplet(request));
    }
    
    public static String getApplicationClassName(String alias) {
        return getApplicationClassName(alias, false);
    }
    
    public static String getApplicationClassName(String alias, boolean isApplet) {
        String className = ALIASES.get(alias);
        if (className == null) {
            LOG.log(Level.SEVERE, "'" + alias + "' is not configured as an ULC application.");
            throw new RuntimeException("'" + alias + "' is not configured as an ULC application.");
        }
        
        return className + (isApplet ? "Applet" : "Application");
    }
    
    public static CoderRegistry getCoderRegistry(Application application) {
        return getCoderRegistry(getApplicationAlias(application));
    }
    
    public static CoderRegistry getCoderRegistry(HttpServletRequest request) {
        return getCoderRegistry(getApplicationAlias(request));
    }
    
    public static CoderRegistry getCoderRegistry(String alias) {
        return alias != null ? CODERS.get(alias) : null;
    }
    
    // Development mode
    public static void init(File basedir) {
        if (!basedir.exists()) {
            LOG.log(Level.SEVERE, "Basedir " + basedir + " does not exist");
            throw new RuntimeException("Basedir " + basedir + " does not exist");
        }
        
        File ulcApplicationPropertiesFile = new File(basedir, "resources/ulc-applications.properties");
        if (!ulcApplicationPropertiesFile.exists()) {
            LOG.log(Level.SEVERE, "File " + ulcApplicationPropertiesFile + " does not exist");
            throw new RuntimeException("File " + ulcApplicationPropertiesFile + " does not exist");
        }
        
        try {
            init(ulcApplicationPropertiesFile.toURI().toURL());
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, "File " + ulcApplicationPropertiesFile + " could not be read");
            throw new RuntimeException("File " + ulcApplicationPropertiesFile + " could not be read", e);
        }
    }
    
    // Production mode
    public static void init(ServletContext servletContext) {
        String propertiesUrlStr = "/WEB-INF/resources/ulc-applications.properties";
        try {
            init(servletContext.getResource(propertiesUrlStr));
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, "Cannot read " + propertiesUrlStr);
            throw new RuntimeException("Cannot read " + propertiesUrlStr);
        }
    }
    
    public static boolean isApplet(HttpServletRequest request) {
        String path = request.getRequestURI();
        String alias = path.substring(path.lastIndexOf('/') + 1);
        return alias.endsWith(APPLET_PREFIX);
    }
    
    public static void setApplication(Application application) {
        String alias = getApplicationAlias(application);
        if (APPLICATIONS.get(alias) == null) {
            APPLICATIONS.put(alias, application);
        }
    }
    
    public static void setCoderRegistry(String alias, CoderRegistry coderRegistry) {
        if (alias == null || coderRegistry == null) {
            return;
        }
        CODERS.put(alias, coderRegistry);
    }
    
    private static void init(URL url) {
        try {
            Properties props = new Properties();
            props.load(url.openStream());
            Enumeration<Object> keys = props.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                String value = (String)props.get(key);
                if (value != null) {
                    ALIASES.put((String)key, value);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot read " + url);
            throw new RuntimeException("Cannot read " + url);
        }
    }
}
