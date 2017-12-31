package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.Common;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Nothing implements RestAction {

    public static final String actionName = "nothing";

    @Override
    public void call(JSONObject json, RequestWrapper request) {

        try {
            json.put(STATUS, STATUS_ERROR);
            json.put(MESSAGE, "Action not defined.");
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(BODY, request.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
