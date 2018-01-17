package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.Common;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version implements RestAction {

    @Override
    public String getApiVersion() {
        return "v1";
    }

    @Override
    public String getActionName() {
        return "version";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, Common.VERSION_CODE);
            json.put(MESSAGE, Common.VERSION_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
