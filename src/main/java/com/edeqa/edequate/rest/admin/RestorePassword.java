package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONObject;

public class RestorePassword extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/restore/password/admin";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {

        System.out.println("RESTOREPASSWORD");

    }


}
