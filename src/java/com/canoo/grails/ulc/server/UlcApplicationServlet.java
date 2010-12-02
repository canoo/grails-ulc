package com.canoo.grails.ulc.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UlcApplicationServlet extends HttpServlet {
    private MultiApplicationServletContainerAdapterHelper fHelper;
    
    @Override
    public void init() throws ServletException {
        fHelper = new MultiApplicationServletContainerAdapterHelper(getServletConfig());
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!fHelper.service(request, response, getServletConfig())) {
            super.service(request, response);
        }
    }
}