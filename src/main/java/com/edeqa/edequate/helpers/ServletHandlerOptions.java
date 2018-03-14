package com.edeqa.edequate.helpers;

import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.helpers.interfaces.Runnable1;
import com.sun.net.httpserver.HttpServer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServletHandlerOptions {

    private static Map<HttpServer, Bundle> bundles = new HashMap<>();

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
        private Map<String, ServletHandlerOptions> items;

        Bundle(HttpServer server) {
            this.server = server;
            items = new LinkedHashMap<>();
        }

        public void put(ServletHandlerOptions servletHandlerOptions) {
            items.put(servletHandlerOptions.getContext(), servletHandlerOptions);
        }

        public void putIfAbsent(ServletHandlerOptions servletHandlerOptions) {
            if(items.containsKey(servletHandlerOptions.getContext())) {
//                return items.get(servletHandlerOptions.getContext());
            } else {
                items.put(servletHandlerOptions.getContext(), servletHandlerOptions);
//                return servletHandlerOptions;
            }
        }

        public void forEach(Runnable1<ServletHandlerOptions> callback) {
            for(ServletHandlerOptions x: items.values()) {
                callback.call(x);
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
