package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/version";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, com.edeqa.edequate.helpers.Version.getVersionCode());
        json.put(MESSAGE, com.edeqa.edequate.helpers.Version.getVersionName());
        json.put(EXTRA, com.edeqa.edequate.helpers.Version.getVersion());
    }
}
