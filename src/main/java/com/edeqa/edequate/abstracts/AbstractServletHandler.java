/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.edeqa.edequate.abstracts;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract public class AbstractServletHandler extends HttpServlet implements HttpHandler {

    private String webDirectory;

    protected AbstractServletHandler() {
        try {
            setWebDirectory(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);
        internalPerform(requestWrapper);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);
        internalPerform(requestWrapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpExchange(exchange);
        internalPerform(requestWrapper);
    }

    abstract public void perform(RequestWrapper requestWrapper) throws IOException;

    private void internalPerform(RequestWrapper requestWrapper) throws IOException {
        perform(requestWrapper);
    }

    public void setWebDirectory(String webDirectory) {
        this.webDirectory = webDirectory;
    }

    public String getWebDirectory() {
        return webDirectory;
    }
}
