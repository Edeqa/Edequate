package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.SendMail;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.IOException;

public class Settings extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/settings";

    private static final String ACTION = "action";
    private static final String SMTP_SAVE = "smtp_save";
    private static final String SMTP_TEST = "smtp_test";

    private static final String MAIL = "mail";
    private static final String SMTP_SERVER = "smtp_server";
    private static final String SMTP_PORT = "smtp_port";
    private static final String SMTP_LOGIN = "smtp_login";
    private static final String SMTP_PASSWORD = "smtp_password";
    private static final String SMTP_OAUTH2 = "smtp_oauth2";
    private static final String REPLY_NAME = "reply_name";
    private static final String REPLY_EMAIL = "reply_email";
    private static final String TARGET_EMAIL = "target_email";

    private WebPath settingsWebPath;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {
        JSONObject options = request.fetchOptions();

        if (Misc.isEmpty(options)) {
            Misc.err("Settings", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        settingsWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.settings.json");

        Misc.err("Settings", options);
        if (options.has(ACTION)) {
            switch (options.getString(ACTION)) {
                case SMTP_TEST:
                    testSmtpSettings(json, options);
                    break;
                case SMTP_SAVE:
                    saveSmtpSettings(json, options);
                    break;
            }
        }
    }

    private void saveSmtpSettings(JSONObject json, JSONObject options) throws IOException {

        JSONObject settingsJSON;
        try {
            settingsJSON = new JSONObject(settingsWebPath.content());
        } catch (IOException e) {
            e.printStackTrace();
            Misc.log("Settings", "failed to read settings, creating empty", "[" + e.getMessage() + "]");
            settingsJSON = new JSONObject();
        }
        JSONObject smtpJSON;
        if (settingsJSON.has(MAIL)) {
            smtpJSON = settingsJSON.getJSONObject(MAIL);
        } else {
            smtpJSON = new JSONObject();
            settingsJSON.put(MAIL, smtpJSON);
        }
        extractOption(options, smtpJSON, REPLY_EMAIL);
        extractOption(options, smtpJSON, REPLY_NAME);
        extractOption(options, smtpJSON, SMTP_LOGIN);
        extractOption(options, smtpJSON, SMTP_PASSWORD);
        extractOption(options, smtpJSON, SMTP_OAUTH2);
        extractOption(options, smtpJSON, SMTP_PORT);
        extractOption(options, smtpJSON, SMTP_SERVER);

        settingsWebPath.save(settingsJSON.toString(2));

        json.put(STATUS, STATUS_SUCCESS);
    }

    private void testSmtpSettings(JSONObject json, JSONObject options) throws Exception {
        int result = new SendMail()
                             .setServerHost(options.getString(SMTP_SERVER))
                             .setServerPort(options.getString(SMTP_PORT))
                             .setLogin(options.getString(SMTP_LOGIN))
                             .setPassword(options.getString(SMTP_PASSWORD))
                             .setFromEmail(options.getString(REPLY_EMAIL))
                             .setFromUsername(options.getString(REPLY_NAME))
                             .setToEmail(options.getString(TARGET_EMAIL))
                             .setSubject("Subject test")
                             .setBody("Test email").send();
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_STRING);
        json.put(MESSAGE, result);
    }

    private void extractOption(JSONObject fromJSON, JSONObject toJSON, String name) {
        if (toJSON.has(name)) toJSON.remove(name);
        if (fromJSON.has(name) && fromJSON.getString(name).length() > 0) {
            toJSON.put(name, fromJSON.getString(name));
        }
    }

}
