/*
 * Copyright (c) 2000-2010 Canoo Engineering AG, Switzerland.
 */
package com.canoo.grails.ulc.server;

import com.ulcjava.base.server.IServerConfiguration;


public interface IMultipleApplicationServerConfiguration extends IServerConfiguration {
    
    String getApplicationAlias();
    
    boolean isApplet();
       
}