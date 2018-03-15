package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

public class Page extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        String body = request.getBody();
        if(Misc.isEmpty(body)) {
            Misc.err("Resource", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        JSONObject options = new JSONObject(body);
        Misc.log("Page", "came: " + options);

        json.put(STATUS, STATUS_SUCCESS);

    }

}


