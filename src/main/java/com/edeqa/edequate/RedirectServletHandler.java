package com.edeqa.edequate;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created 10/5/16.
 */
public class RedirectServletHandler extends AbstractServletHandler {

    private final static String LOG = "Redirect";
    private final Map<String,JSONObject> redirections = new HashMap<>();

    @Override
    public void perform(RequestWrapper requestWrapper) {
        try {
            URI uri = requestWrapper.getRequestURI();
            int port = requestWrapper.getRequestedPort();
            String host = requestWrapper.getRequestedHost();
            String referer = requestWrapper.getReferer();

            Misc.log(LOG, "[" + requestWrapper.getRemoteAddress() + "]", uri.getPath(), (referer != null ? "referer: " + referer : ""));

            ArrayList<String> parts = new ArrayList<>(Arrays.asList(uri.getPath().split("/")));

            String path = "" + port + uri.getPath();
            Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
            if(getRedirections().containsKey(path)) {
                process(getRedirections().get(path), requestWrapper, arguments);
                return;
            } else {
                for (Map.Entry<String, JSONObject> entry : getRedirections().entrySet()) {
                    if (path.startsWith(entry.getKey())) {
                        process(entry.getValue(), requestWrapper, arguments);
                        return;
                    }
                }
            }
            requestWrapper.sendRedirect("https://" + host + arguments.getWrappedHttpsPort() + uri.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
            requestWrapper.sendRedirect("https://" + requestWrapper.getRequestedHost() + arguments.getWrappedHttpsPort() + "/404.html");
        }
    }

    private void process(JSONObject json, RequestWrapper requestWrapper, Arguments arguments) {
        int code = 404;
        String message = "Not found";
        String mode = "error";
        if(json.has("redirect")) {
            try {
                String path = String.valueOf(json.get("redirect"));
                Misc.log(LOG, "-> " + path);
                requestWrapper.sendRedirect(path);
                return;
            } catch(Exception e) {
                Misc.err(LOG, "failed, error:", e);
            }
        }
        if(json.has("destination_port")) {
            try {
                boolean secured = true;
                if(json.has("destination_secured")) {
                    secured = json.getBoolean("destination_secured");
                }
                URI uri = requestWrapper.getRequestURI();
                code = Integer.parseInt(String.valueOf(json.get("destination_port")));

                URI newUri = new URI(secured ? "https" : "http", null, requestWrapper.getRequestedHost(), code, uri.getPath(), null, null);
                Misc.log(LOG, "-> " + newUri.toString());
                requestWrapper.sendRedirect(newUri.toString());
            } catch(Exception e) {
                Misc.err(LOG, "failed, error:", e);
            }
        }
        if(json.has("mode")) {
            try {
                mode = String.valueOf(json.get("mode"));
                if("content".equals(mode)) {
                    WebPath webPath = new WebPath(arguments.getWebRootDirectory(), requestWrapper.getRequestURI().getPath());
                    if(webPath.path().exists()) {
                        Misc.log(LOG, "-> " + webPath.path().length() + " byte(s)");
                        new Content()
                                .setMimeType(new MimeType().setMime(Mime.TEXT_PLAIN))
                                .setWebPath(webPath)
                                .setResultCode(200)
                                .call(null, requestWrapper);
                        return;
                    }
                }
            } catch(Exception e) {
                Misc.err(LOG, "failed, error:", e);
            }
        }
        if(json.has("code")) {
            try {
                code = Integer.parseInt(String.valueOf(json.get("code")));
            } catch(Exception e) {
                Misc.err(LOG, "failed, error:", e);
            }
        }
        if(json.has("message")) {
            try {
                message = String.valueOf(json.get("message"));
            } catch(Exception e) {
                Misc.err(LOG, "failed, error:", e);
            }
        }
        Misc.log(LOG, "->", code);
        requestWrapper.sendError(code, message);

    }

    public RedirectServletHandler applyServer(HttpServer server) {
        applyServer(server, false);
        return this;
    }

    public RedirectServletHandler applySslServer(HttpsServer sslServer) {
        applyServer(sslServer, true);
        return this;
    }

    private void applyServer(HttpServer server, boolean secured) {
        try {
            Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
            WebPath ignoredPaths = new WebPath(arguments.getWebRootDirectory(), "data/.redirections.json");
            if(ignoredPaths.path().exists()) {
                JSONObject paths = new JSONObject(ignoredPaths.content());
                Iterator<String> iter = paths.keys();
                while (iter.hasNext()) {
                    String path = iter.next();
                    JSONObject options = paths.getJSONObject(path);

                    server.createContext("/" + path, this);
                    getRedirections().put(server.getAddress().getPort() + "/" + path, options);
                    Misc.log(this.getClass().getSimpleName(), "will catch", "[:" + server.getAddress().getPort() + "/" + path + "]");
                }
            }
        } catch (Exception e) {
            Misc.err(LOG, "failed, error:", e);
        }
    }

    private Map<String, JSONObject> getRedirections() {
        return redirections;
    }
}
