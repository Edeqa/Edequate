package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.DigestAuthenticator;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.edequate.rest.system.OneTime;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

import static com.edeqa.edequate.rest.admin.Admins.PASSWORD;
import static com.edeqa.edequate.rest.system.OneTime.LINK;
import static com.edeqa.edequate.rest.system.OneTime.NONCE;
import static com.edeqa.edequate.rest.system.OneTime.TIMEOUT;
import static com.edeqa.edequate.rest.system.OneTime.TIMESTAMP;

public class RestorePassword extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/restore/password";

    private static final String LOGIN = "login";
    private static final String REALM = "realm";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {

        //noinspection unchecked
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        //noinspection unchecked
        Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);

        json.put(STATUS, STATUS_ERROR);

        JSONObject options = request.fetchOptions();

        //noinspection unchecked
        OneTime.Action oneTimeAction = ((OneTime) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(OneTime.TYPE)).create();
        oneTimeAction.setRequestOptions(options);
        oneTimeAction.setOnFetchToken(DigestAuthenticator::createNonce);
        oneTimeAction.setOnError(error -> {
            Misc.err("RestorePassword", "failed:", error.getMessage(), options);
            json.put(CODE, ERROR_REQUEST_TIMEOUT);
            json.put(MESSAGE, "Request expired");
        });
        oneTimeAction.setOnWelcome(() -> {
            //noinspection unchecked
            Splash splash = (Splash) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Splash.TYPE);
            String content = splash.setScript("/js/admin/Restore.js").fetchSplash().build();
            new Content()
                    .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true))
                    .setResultCode(200)
                    .setContent(content)
                    .call(null, request);
            json.put(STATUS, STATUS_DELAYED);
        });

        if (options.has(LOGIN)) {
            String login = options.getString(LOGIN);
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
            oneTimeAction.setPayload(new HashMap<String, Serializable>() {{
                put(LOGIN, login);
                put(REALM, arguments.getRealm());
            }});
            oneTimeAction.setStrong(true);
            oneTimeAction.setOnStart(token -> {
                json.put(STATUS, STATUS_SUCCESS);
                json.put(CODE, CODE_JSON);
                json.put(MESSAGE, new JSONObject() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(TIMEOUT, oneTimeAction.getExpirationTimeout());
                    put(LINK, "https://" + request.getRequestedHost() + ":" + arguments.getHttpsAdminPort() + TYPE + "?once=" + token);
                }});
            });
            oneTimeAction.start();
        } else {
            oneTimeAction.setOnCheck(token -> {
                json.put(STATUS, STATUS_SUCCESS);
                json.put(CODE, CODE_JSON);
                json.put(MESSAGE, new JSONObject() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(TIMEOUT, oneTimeAction.getExpirationTimeout());
                    put(NONCE, token);
                }});
            });
            oneTimeAction.setOnSuccess(payload -> {
                String login = payload.getString(LOGIN);
                admins.read();
                Admins.Admin admin = admins.get(login);
                if (admin == null) {
                    Misc.err("RestorePassword", "not found", "[" + login + "]");
                    json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
                    json.put(MESSAGE, "Incorrect request");
                    return;
                }
                String password = options.getString(PASSWORD);
                try {
                    admin.storePassword(password);
                    admins.save();
                    json.put(STATUS, STATUS_SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            oneTimeAction.process();
        }
    }
}
