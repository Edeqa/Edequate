package com.edeqa.edequate.interfaces;

import org.json.JSONObject;

@SuppressWarnings("unused")
public interface NamedCall<T> /*extends Runnable2<JSONObject, T>*/{

    String BODY = "body";
    String CALLBACK = "callback";
    String CODE = "code";
    String EXTRA = "extra";
    String FALLBACK = "fallback";
    String LOCALE = "locale";
    String MESSAGE = "message";
    String REQUEST = "request";
    String STATUS = "status";
    String STATUS_ERROR = "error";
    String STATUS_SUCCESS = "success";

    int CODE_STRING = 1;
    int CODE_LIST = 2;
    int CODE_JSON = 3;
    int CODE_DELAYED = 4;
    int CODE_REDIRECT = 302;

    int ERROR_ACTION_NOT_DEFINED = 1;
    int ERROR_RUNTIME = 2;
    int ERROR_NOT_FOUND = 404;
    int ERROR_GONE = 410;
    int ERROR_NOT_EXTENDED = 510;

    String getName();

    void call(JSONObject json, T object) throws Exception;
}
