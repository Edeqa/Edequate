package com.edeqa.edequate.interfaces;

import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONObject;

@SuppressWarnings("unused")
public interface RestAction {

    String BODY = "body";
    String CALLBACK = "callback";
    String CODE = "code";
    String FALLBACK = "fallback";
    String LOCALE = "locale";
    String MESSAGE = "message";
    String EXTRA = "extra";
    String REQUEST = "request";
    String STATUS = "status";
    String STATUS_ERROR = "error";
    String STATUS_SUCCESS = "success";

    int CODE_STRING = 1;
    int CODE_LIST = 2;
    int CODE_JSON = 3;
    int CODE_REDIRECT = 302;

    int ERROR_ACTION_NOT_DEFINED = 1;
    int ERROR_NOT_FOUND = 404;
    int ERROR_GONE = 410;
    int ERROR_NOT_EXTENDED = 510;

    String getApiVersion();

    String getActionName();

    void call(JSONObject json, RequestWrapper request);
}
