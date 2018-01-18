package com.edeqa.edequate.rest;

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
            json.put(CODE, com.edeqa.edequate.helpers.Version.getVersionCode());
            json.put(MESSAGE, com.edeqa.edequate.helpers.Version.getVersionName());
            json.put(EXTRA, com.edeqa.edequate.helpers.Version.getVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
