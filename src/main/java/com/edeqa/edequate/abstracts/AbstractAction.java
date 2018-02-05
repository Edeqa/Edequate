package com.edeqa.edequate.abstracts;

import com.edeqa.eventbus.AbstractEntityHolder;

import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractAction<T> extends AbstractEntityHolder {

    public static final String EVENTBUS = "rest";

    public static final String BODY = "body";
    public static final String CALLBACK = "callback";
    public static final String CODE = "code";
    public static final String EXTRA = "extra";
    public static final String FALLBACK = "fallback";
    public static final String LOCALE = "locale";
    public static final String MESSAGE = "message";
    public static final String REQUEST = "request";
    public static final String STATUS = "status";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_SUCCESS = "success";

    public static final int CODE_STRING = 1;
    public static final int CODE_LIST = 2;
    public static final int CODE_JSON = 3;
    public static final int CODE_DELAYED = 4;
    public static final int CODE_REDIRECT = 302;

    public static final int ERROR_ACTION_NOT_DEFINED = 1;
    public static final int ERROR_RUNTIME = 2;
    public static final int ERROR_NOT_FOUND = 404;
    public static final int ERROR_GONE = 410;
    public static final int ERROR_NOT_EXTENDED = 510;

    public abstract void call(JSONObject event, T object) throws Exception;


}



