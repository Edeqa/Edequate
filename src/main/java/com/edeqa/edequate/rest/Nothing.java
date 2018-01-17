package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Nothing implements RestAction {

    @Override
    public String getApiVersion() {
        return "v1";
    }

    @Override
    public String getActionName() {
        return "nothing";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            json.put(STATUS, STATUS_ERROR);
            json.put(MESSAGE, "Action not defined.");
            json.put(CODE, ERROR_NOT_EXTENDED);

            String body = request.getBody();
            if(!Misc.isEmpty(body)) {
                json.put(BODY, request.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
