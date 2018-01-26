package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Nothing implements NamedCall<RequestWrapper> {

    private String message;
    private Throwable throwable;

    public Nothing() {
        setMessage("Action not defined.");
    }

    @Override
    public String getName() {
        return "nothing";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_ERROR);
        json.put(MESSAGE, getMessage());
        json.put(CODE, ERROR_NOT_EXTENDED);
        if(getThrowable() != null) {
            json.put(EXTRA, getThrowable().getMessage());
        }

        String body = request.getBody();
        if (!Misc.isEmpty(body)) {
            json.put(BODY, request.getBody());
        }
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
