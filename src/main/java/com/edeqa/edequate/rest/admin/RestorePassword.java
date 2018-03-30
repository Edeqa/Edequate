package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.DigestAuthenticator;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.Content;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import static com.edeqa.edequate.rest.admin.Admins.PASSWORD;

public class RestorePassword extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/restore/password";

    private static final String LOGIN = "login";
    private static final String TOKEN = "token";
    private static final String IP = "ip";
    private static final String MODE = "mode";
    private static final String NONCE = "nonce";
    private static final String REQUEST = "request";
    private static final String TIMEOUT = "timeout";
    private static final String TIMESTAMP = "timestamp";
    private static final String WAITING = "waiting";

    private static final long EXPIRATION_TIMEOUT = 1000 * 60 * 15L;


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {

        json.put(STATUS, STATUS_ERROR);

        JSONObject options = request.fetchOptions();
        System.out.println("RESTOREPASSWORD:"+options);
        if(options.has(LOGIN)) {
            fetchToken(json, request, options);
        } else if(options.has(TOKEN)) {
            fetchNonce(json, request, options);
        } else if(options.has(NONCE)) {
            resetPassword(json, request, options);
        } else {
            Splash splash = (Splash) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Splash.TYPE);
            String content = splash.fetchSplash(false, true).build();
            new Content()
                    .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true))
                    .setResultCode(200)
                    .setContent(content)
                    .call(null, request);
            json.put(STATUS, STATUS_DELAYED);
        }
    }

    private void fetchToken(JSONObject json, RequestWrapper request, JSONObject options) throws IOException {
        String login = options.getString(LOGIN);

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);
        admins.read();

        Admins.Admin admin = admins.get(login);

        if (admin == null) {
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "Incorrect login");
            return;
        }
        if (admin.getEmail() == null) {
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "E-mail not defined for " + login);
            return;
        }

        String token = DigestAuthenticator.createNonce();

        String link = "https://" + request.getRequestedHost() + ":" + arguments.getHttpsAdminPort() + TYPE + "?request=" + token;

        WebPath tokensFile = new WebPath(arguments.getWebRootDirectory() + "/data", ".restore.json");
        JSONObject tokens;
        try {
            tokens = new JSONObject(tokensFile.content());
        } catch(Exception e) {
            tokens = new JSONObject();
        }

        Iterator<String> iter = tokens.keys();
        while(iter.hasNext()) {
            String key = iter.next();
            JSONObject entry = tokens.getJSONObject(key);
            if(System.currentTimeMillis() - entry.getLong(TIMESTAMP) > EXPIRATION_TIMEOUT) {
                iter.remove();
            }
        }

        tokens.put(token, new JSONObject(){{put(LOGIN, login);put(TIMESTAMP, System.currentTimeMillis()); put(IP, request.getRemoteAddress().getAddress().getHostAddress());put(MODE, REQUEST);}});

        tokensFile.save(tokens.toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_JSON);
        JSONObject jsonResult = new JSONObject(){{put(TOKEN, token);put(TIMESTAMP,System.currentTimeMillis());put(TIMEOUT, EXPIRATION_TIMEOUT);put("link", link);}};
        json.put(MESSAGE, jsonResult);
    }

    private void fetchNonce(JSONObject json, RequestWrapper request, JSONObject options) throws IOException {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        WebPath tokensFile = new WebPath(arguments.getWebRootDirectory() + "/data", ".restore.json");

        JSONObject tokens;
        try {
            tokens = new JSONObject(tokensFile.content());
        } catch(Exception e) {
            tokens = new JSONObject();
        }
        String token = options.getString(TOKEN);
        if(!tokens.has(token)) {
            Misc.err("RestorePassword", "not found token", options);
            json.put(CODE, ERROR_REQUEST_TIMEOUT);
            json.put(MESSAGE, "Request not found");
            return;
        }
        JSONObject requested = tokens.getJSONObject(token);

        tokens.remove(token);

        String login = requested.getString(LOGIN);
        Long timestamp = requested.getLong(TIMESTAMP);

        if(System.currentTimeMillis() - timestamp > EXPIRATION_TIMEOUT) {
            Misc.err("RestorePassword", "expired token");
            json.put(CODE, ERROR_REQUEST_TIMEOUT);
            json.put(MESSAGE, "Request expired");
            return;
        }
        String nonce = DigestAuthenticator.createNonce();

        tokens.put(nonce, new JSONObject(){{put(LOGIN, login);put(TIMESTAMP, System.currentTimeMillis()); put(IP, request.getRemoteAddress().getAddress().getHostAddress());put(MODE, WAITING);}});

        tokensFile.save(tokens.toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_JSON);
        JSONObject jsonResult = new JSONObject(){{put("nonce",nonce);put(TIMESTAMP,System.currentTimeMillis());put(TIMEOUT, EXPIRATION_TIMEOUT);}};
        json.put(MESSAGE, jsonResult);
    }

    private void resetPassword(JSONObject json, RequestWrapper request, JSONObject options) throws Exception {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        WebPath tokensFile = new WebPath(arguments.getWebRootDirectory() + "/data", ".restore.json");

        JSONObject tokens;
        try {
            tokens = new JSONObject(tokensFile.content());
        } catch(Exception e) {
            tokens = new JSONObject();
        }
        String nonce = options.getString(NONCE);
        if(!tokens.has(nonce)) {
            Misc.err("RestorePassword", "not found nonce", options);
            json.put(CODE, ERROR_REQUEST_TIMEOUT);
            json.put(MESSAGE, "Request expired");
            return;
        }
        JSONObject requested = tokens.getJSONObject(nonce);

        tokens.remove(nonce);
        tokensFile.save(tokens.toString(2));

        String login = requested.getString(LOGIN);
        Long timestamp = requested.getLong(TIMESTAMP);

        if(System.currentTimeMillis() - timestamp > EXPIRATION_TIMEOUT) {
            Misc.err("RestorePassword", "expired nonce");
            json.put(CODE, ERROR_REQUEST_TIMEOUT);
            json.put(MESSAGE, "Request expired");
            return;
        }

        Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);
        admins.read();
        Admins.Admin admin = admins.get(login);
        if (admin == null) {
            Misc.err("RestorePassword", "not found", "[" + login + "]");
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "Incorrect request");
            return;
        }

        String password = options.getString(PASSWORD);
        admin.storePassword(password);
        admins.save();

        json.put(STATUS, STATUS_SUCCESS);
    }
}
