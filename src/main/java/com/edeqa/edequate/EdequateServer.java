package com.edeqa.edequate;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.DigestAuthenticator;
import com.edeqa.edequate.helpers.ServletHandlerOptions;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.rest.SecureContext;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.File;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    private static final EventBus<AbstractAction> restBus;
    private static final EventBus<AbstractAction> systemBus;
    private static HttpServer server;
    private static HttpsServer sslServer;
    private static HttpsServer adminServer;
    private static Arguments arguments;

    static {
        EventBus.setMainRunner(EventBus.RUNNER_SINGLE_THREAD);
        //noinspection unchecked
        restBus = (EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS);
        //noinspection unchecked
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

    private static void setupServletHandlers() {
        RedirectServletHandler redirectServer = new RedirectServletHandler().applyRedirections(getServer()).applyRedirections(getSslServer());
        MainServletHandler mainServletHandler = new MainServletHandler();
        RestServletHandler restServletHandler = new RestServletHandler();
        AdminServletHandler adminServletHandler = new AdminServletHandler();

        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(mainServletHandler));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest/").setServletHandler(restServletHandler));

        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(adminServletHandler));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/admin").setServletHandler(adminServletHandler).setAuthenticator(new DigestAuthenticator(getArguments().getRealm())));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/admin/logout").setServletHandler(adminServletHandler));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/admin/restore").setServletHandler(adminServletHandler));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest/").setServletHandler(restServletHandler));

    }


    protected static void startServer() throws Exception {
        ServletHandlerOptions.getOrCreate(getServer()).forEach(servletHandler -> {
            getServer().createContext(servletHandler.getContext(), servletHandler.getServletHandler());
            Misc.log(LOG, "starting", servletHandler.getServletHandler().getClass().getSimpleName(), "on HTTP", getArguments().getHttpPort(), "[" + getArguments().getWrappedHttpPort() + servletHandler.getContext() + "]");

        });
        ServletHandlerOptions.getOrCreate(getSslServer()).forEach(servletHandler -> {
            getSslServer().createContext(servletHandler.getContext(), servletHandler.getServletHandler());
            Misc.log(LOG, "starting", servletHandler.getServletHandler().getClass().getSimpleName(), "on HTTPS:", getArguments().getHttpsPort(), "[" +  getArguments().getWrappedHttpsPort() + servletHandler.getContext() + "]");

        });
        ServletHandlerOptions.getOrCreate(getAdminServer()).forEach(servletHandler -> {
            HttpContext context = getAdminServer().createContext(servletHandler.getContext(), servletHandler.getServletHandler());
            if(servletHandler.getAuthenticator() != null) {
                context.setAuthenticator(servletHandler.getAuthenticator());
            }
            Misc.log(LOG, "starting", servletHandler.getServletHandler().getClass().getSimpleName(), "on HTTPS:", getArguments().getHttpsAdminPort(), "[:" + getArguments().getHttpsAdminPort() + servletHandler.getContext() + "]");
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

    protected static Arguments getArguments() {
        return arguments;
    }

    protected static void setArguments(Arguments arguments) {
        EdequateServer.arguments = arguments;
    }
}