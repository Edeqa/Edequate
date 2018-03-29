package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.Content;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

public class RestorePassword extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/restore/password";

    private static final String LOGIN = "login";

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
            String login = options.getString(LOGIN);

            Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);
            admins.read();
            Admins.Admin admin = admins.get(login);

            System.out.println("ADMIN:"+admin);

            if(admin == null) {
                json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
                json.put(MESSAGE, "Incorrect login");
                return;
            }
            if(admin.getEmail() == null) {
                json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
                json.put(MESSAGE, "E-mail not defined for " + login);
                return;
            }

            String token = Misc.getUnique();
            admin.setRestore(token);
            json.put(CODE, CODE_JSON);
            json.put(MESSAGE, "Reset link has been sent to " + login + "'s email. It will be valid only 1 hour.");
            json.put(STATUS, STATUS_SUCCESS);
        } else {
            Splash splash = (Splash) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Splash.TYPE);

            new Content()
                    .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true))
                    .setResultCode(200)
                    .setContent(splash.fetchSplash(false, true).build())
                    .call(null, request);
            json.put(STATUS, STATUS_DELAYED);
        }
    }
}
