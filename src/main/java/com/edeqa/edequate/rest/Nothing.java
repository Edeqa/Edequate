package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Nothing extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/nothing";

    private String message;
    private Throwable throwable;

    public Nothing() {
        setMessage("Action not defined.");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_ERROR);
        json.put(CODE, ERROR_METHOD_NOT_ALLOWED);
        json.put(MESSAGE, getMessage());
        if(getThrowable() != null) {
            json.put(MESSAGE, getThrowable().getMessage());
        }
        try {
            String body = request.getBody();
            if (!Misc.isEmpty(body)) {
                json.put(BODY, body);
            }
        } catch (Exception e) {}
        setMessage(null);
        setThrowable(null);
    }

    public Nothing setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Nothing setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }
}
