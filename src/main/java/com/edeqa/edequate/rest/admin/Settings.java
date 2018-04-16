package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.SendMail;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

public class Settings extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/settings";

    private static final String ACTION = "action";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {
        JSONObject options = request.fetchOptions();

        if(Misc.isEmpty(options)) {
            Misc.err("Settings", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        Misc.err("Settings", options);

        if(options.has(ACTION)) {
            switch (options.getString(ACTION)) {
                case "smtp_test":
                    int result = new SendMail().sendMail(options.getString("smtp_server"), options.getString("smtp_port"), options.getString("smtp_login"), options.getString("smtp_password"), null, options.getString("reply_email"), options.getString("reply_name"), options.getString("target_email"), "Subject test", "Test email");
                    System.out.println("RESULT:"+result);
                    json.put(STATUS, STATUS_SUCCESS);
                    json.put(CODE, CODE_STRING);
                    json.put(MESSAGE, result);
                    break;
            }
        }
    }


}
