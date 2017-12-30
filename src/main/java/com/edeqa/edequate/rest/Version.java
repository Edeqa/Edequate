package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.Common;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version implements RestAction {

    public static final String actionName = "version";

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {

            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, Common.VERSION_CODE);
            json.put(MESSAGE, Common.VERSION_NAME);
            json.put(BODY, Common.fetchBody(request));

//            System.out.println(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
