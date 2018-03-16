package com.edeqa.edequate.abstracts;

import com.edeqa.eventbus.AbstractEntityHolder;

import org.json.JSONObject;

@SuppressWarnings("unused")
public abstract class AbstractAction<T> extends AbstractEntityHolder {

    public static final String RESTBUS = "rest";
    public static final String SYSTEMBUS = "system";

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

    public static final int CODE_MOVED_TEMPORARILY = 302;

    public static final int ERROR_ACTION_NOT_DEFINED = 1;
    public static final int ERROR_RUNTIME = 2;
    public static final int ERROR_BAD_REQUEST = 400;
    public static final int ERROR_UNAUTHORIZED = 401;
    public static final int ERROR_PAYMENT_REQUIRED = 402;
    public static final int ERROR_FORBIDDEN = 403;
    public static final int ERROR_NOT_FOUND = 404;
    public static final int ERROR_METHOD_NOT_ALLOWED = 405;
    public static final int ERROR_NOT_ACCEPTABLE = 406;
    public static final int ERROR_REQUEST_TIMEOUT = 408;
    public static final int ERROR_CONFLICT = 409;
    public static final int ERROR_GONE = 410;
    public static final int ERROR_LENGTH_REQUIRED = 411;
    public static final int ERROR_PAYLOAD_TOO_LARGE = 413;
    public static final int ERROR_URI_TOO_LONG = 414;
    public static final int ERROR_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity (hence a 415 (Unsupported Media Type) status code is inappropriate), and the syntax of the request entity is correct (thus a 400 (Bad Request) status code is inappropriate) but was unable to process the contained instructions. For example, this error condition may occur if an XML request body contains well-formed (i.e., syntactically correct), but semantically erroneous, XML instructions.
     */
    public static final int ERROR_UNPROCESSABLE_ENTITY = 422;
    public static final int ERROR_LOCKED = 423;
    public static final int ERROR_FAILED_DEPENDENCY = 424;
    public static final int ERROR_TOO_MANY_REQUESTS = 429;

    public static final int ERROR_INTERNAL_SERVER_ERROR = 500;
    public static final int ERROR_NOT_IMPLEMENTED = 501;
    public static final int ERROR_SERVICE_UNAVAILABLE = 503;
    public static final int ERROR_INSUFFICIENT_STORAGE = 507;
    public static final int ERROR_NOT_EXTENDED = 510;

    public abstract void call(JSONObject event, T object) throws Exception;

}



