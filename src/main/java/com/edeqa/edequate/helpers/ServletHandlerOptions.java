package com.edeqa.edequate.helpers;

import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.helpers.interfaces.Consumer;
import com.sun.net.httpserver.HttpServer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServletHandlerOptions {

    private static final Map<HttpServer, Bundle> bundles = new HashMap<>();

    private HttpServer server;
    private String context;
    private AbstractServletHandler servletHandler;
    private DigestAuthenticator authenticator;

    public ServletHandlerOptions setServer(HttpServer server) {
        this.server = server;
        return this;
    }

    public HttpServer getServer() {
        return server;
    }

    public ServletHandlerOptions setContext(String context) {
        this.context = context;
        return this;
    }

    public ServletHandlerOptions setServletHandler(AbstractServletHandler servletHandler) {
        this.servletHandler = servletHandler;
        return this;
    }

    public String getContext() {
        return context;
    }

    public AbstractServletHandler getServletHandler() {
        return servletHandler;
    }

    public ServletHandlerOptions setAuthenticator(DigestAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public DigestAuthenticator getAuthenticator() {
        return authenticator;
    }


    public static class Bundle {
        private final HttpServer server;
        private final Map<String, ServletHandlerOptions> items;

        Bundle(HttpServer server) {
            this.server = server;
            items = new LinkedHashMap<>();
        }

        public void put(ServletHandlerOptions servletHandlerOptions) {
            items.put(servletHandlerOptions.getContext(), servletHandlerOptions);
        }

        public void putIfAbsent(ServletHandlerOptions servletHandlerOptions) {
            if(!items.containsKey(servletHandlerOptions.getContext())) {
                items.put(servletHandlerOptions.getContext(), servletHandlerOptions);
            }
        }

        public void forEach(Consumer<ServletHandlerOptions> callback) {
            for(ServletHandlerOptions x: items.values()) {
                callback.accept(x);
            }
        }
    }

    public static Bundle getOrCreate(HttpServer server) {
        if(bundles.containsKey(server)) return bundles.get(server);
        Bundle bundle = new Bundle(server);
        bundles.put(server, bundle);
        return bundle;
    }
}
