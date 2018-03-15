package com.edeqa.edequate;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.DigestAuthenticator;
import com.edeqa.edequate.helpers.ServletHandlerOptions;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.SecureContext;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.File;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;
import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;


/**
 * Created 3/14/18.
 */
@SuppressWarnings({"HardCodedStringLiteral", "GoogleAppEngineForbiddenCode"})
public class EdequateServer {

    private static final String LOG = "EdequateServer";
    private static EventBus<AbstractAction> restBus;
    private static EventBus<AbstractAction> systemBus;
    private static HttpServer server;
    private static HttpsServer sslServer;
    private static HttpsServer adminServer;
    private static Map<String, AbstractServletHandler> servletHandlers;
    private static Arguments arguments;

    static {
        EventBus.setMainRunner(EventBus.RUNNER_SINGLE_THREAD);
        restBus = (EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS);
        systemBus = (EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS);
    }

    @SuppressWarnings("AppEngineForbiddenCode")
    public static void main(final String[] args ) throws Exception {

        Misc.log(LOG, "====== Edequate server v" + Version.getVersion() + ". Copyright (C) 2017-18, Edeqa. http://www.edeqa.com ======");

        setArguments((Arguments) getSystemBus().registerIfAbsent(new Arguments()));
        getArguments().call(null, args);

        prepareServer();
        setupServletHandlers();
        startServer();
    }

    protected static void prepareServer() throws Exception {
        setServer(HttpServer.create());
        setSslServer(HttpsServer.create());
        setAdminServer(HttpsServer.create());
        try {
            getServer().bind(new InetSocketAddress(getArguments().getHttpPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects port in use: " + getArguments().getHttpPort() + ", server exits.");
            System.exit(1);
        }

        try {
            getSslServer().bind(new InetSocketAddress(getArguments().getHttpsPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects secured port in use:", getArguments().getHttpsPort() + ", server exits.");
            System.exit(1);
        }
        try {
            getAdminServer().bind(new InetSocketAddress(getArguments().getHttpsAdminPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects admin port in use:", getArguments().getHttpsAdminPort() + ", server exits.");
            System.exit(1);
        }

        SecureContext secureContext = (SecureContext) getSystemBus().registerIfAbsent(new SecureContext());
        secureContext.call(null, getArguments());

        Misc.log(LOG,"Server web root directory: "+new File(getArguments().getWebRootDirectory()).getCanonicalPath());

        getSslServer().setHttpsConfigurator(secureContext.getHttpsConfigurator());
        getAdminServer().setHttpsConfigurator(secureContext.getHttpsConfigurator());

    }

    private static void setupServletHandlers() throws Exception {
        MainServletHandler mainServletHandler = new MainServletHandler();
        RestServletHandler restServletHandler = new RestServletHandler();
        AdminServletHandler adminServletHandler = new AdminServletHandler();

        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(mainServletHandler));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest/").setServletHandler(restServletHandler));

        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest/").setServletHandler(restServletHandler));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(adminServletHandler).setAuthenticator(new DigestAuthenticator("edequate")));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/admin/logout").setServletHandler(adminServletHandler));
    }


    protected static void startServer() throws Exception {
        ServletHandlerOptions.getOrCreate(getServer()).forEach(new Runnable1<ServletHandlerOptions>() {
            @Override
            public void call(ServletHandlerOptions o) {
                getServer().createContext(o.getContext(), o.getServletHandler());
                Misc.log(LOG, "starting", o.getServletHandler().getClass().getSimpleName(), "on HTTP", getArguments().getHttpPort(), "[" + getArguments().getWrappedHttpPort() + o.getContext() + "]");

            }
        });
        ServletHandlerOptions.getOrCreate(getSslServer()).forEach(new Runnable1<ServletHandlerOptions>() {
            @Override
            public void call(ServletHandlerOptions o) {
                getSslServer().createContext(o.getContext(), o.getServletHandler());
                Misc.log(LOG, "starting", o.getServletHandler().getClass().getSimpleName(), "on HTTPS:", getArguments().getHttpsPort(), "[" +  getArguments().getWrappedHttpsPort() + o.getContext() + "]");

            }
        });
        ServletHandlerOptions.getOrCreate(getAdminServer()).forEach(new Runnable1<ServletHandlerOptions>() {
            @Override
            public void call(ServletHandlerOptions o) {
                HttpContext context = getAdminServer().createContext(o.getContext(), o.getServletHandler());
                if(o.getAuthenticator() != null) {
                    context.setAuthenticator(o.getAuthenticator());
                }
                Misc.log(LOG, "starting", o.getServletHandler().getClass().getSimpleName(), "on HTTPS:", getArguments().getHttpsAdminPort(), "[:" + getArguments().getHttpsAdminPort() + o.getContext() + "]");
            }
        });

        ExecutorService executor = Executors.newCachedThreadPool();
        getServer().setExecutor(executor);
        getSslServer().setExecutor(executor);
        getAdminServer().setExecutor(executor);

        getServer().start();
        getSslServer().start();
        getAdminServer().start();

        Misc.log(LOG, "handles web link", "http://" + InetAddress.getLocalHost().getHostAddress() + getArguments().getWrappedHttpPort());
        Misc.log(LOG, "handles web link", "https://" + InetAddress.getLocalHost().getHostAddress() + getArguments().getWrappedHttpsPort());
        Misc.log(LOG, "handles admin link", "https://" + InetAddress.getLocalHost().getHostAddress() + ":" + getArguments().getHttpsAdminPort() + "/admin/");

    }

    protected static EventBus<AbstractAction> getRestBus() {
        return restBus;
    }

    protected static EventBus<AbstractAction> getSystemBus() {
        return systemBus;
    }

    protected static HttpServer getServer() {
        return server;
    }

    protected static void setServer(HttpServer server) {
        EdequateServer.server = server;
    }

    protected static HttpsServer getSslServer() {
        return sslServer;
    }

    protected static void setSslServer(HttpsServer sslServer) {
        EdequateServer.sslServer = sslServer;
    }

    protected static HttpsServer getAdminServer() {
        return adminServer;
    }

    protected static void setAdminServer(HttpsServer adminServer) {
        EdequateServer.adminServer = adminServer;
    }

    protected static Map<String, AbstractServletHandler> getServletHandlers() {
        return servletHandlers;
    }

    protected static Arguments getArguments() {
        return arguments;
    }

    protected static void setArguments(Arguments arguments) {
        EdequateServer.arguments = arguments;
    }
}