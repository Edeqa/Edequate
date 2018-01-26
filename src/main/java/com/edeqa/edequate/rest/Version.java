package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, com.edeqa.edequate.helpers.Version.getVersionCode());
        json.put(MESSAGE, com.edeqa.edequate.helpers.Version.getVersionName());
        json.put(EXTRA, com.edeqa.edequate.helpers.Version.getVersion());
    }
}
