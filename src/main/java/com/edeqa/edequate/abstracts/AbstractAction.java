package com.edeqa.edequate.abstracts;

import com.edeqa.eventbus.AbstractEntityHolder;

import org.json.JSONObject;

@SuppressWarnings({"unused", "WeakerAccess"})
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
    public static final String ARGUMENTS = "arguments";
    public static final String REQUEST = "request";
    public static final String STATUS = "status";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_DELAYED = "delayed";

    public static final int CODE_STRING = 1;
    public static final int CODE_LIST = 2;
    public static final int CODE_JSON = 3;
    public static final int CODE_HTML = 4;

    public static final int CODE_MOVED_TEMPORARILY = 302;

    public static final int ERROR_ACTION_NOT_DEFINED = 1;
    public static final int ERROR_RUNTIME = 2;

    /**
     * The server cannot or will not process the request due to an apparent client error (e.g., malformed request syntax, size too large, invalid request message framing, or deceptive request routing).
     */
    public static final int ERROR_BAD_REQUEST = 400;

    /**
     * Similar to 403 Forbidden, but specifically for use when authentication is required and has failed or has not yet been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. See Basic access authentication and Digest access authentication. 401 semantically means "unauthenticated", i.e. the user does not have the necessary credentials.
     * Note: Some sites issue HTTP 401 when an IP address is banned from the website (usually the website domain) and that specific address is refused permission to access a website.
     */
    public static final int ERROR_UNAUTHORIZED = 401;

    /**
     * Reserved for future use. The original intention was that this code might be used as part of some form of digital cash or micropayment scheme, as proposed for example by GNU Taler, but that has not yet happened, and this code is not usually used. Google Developers API uses this status if a particular developer has exceeded the daily limit on requests.
     */
    public static final int ERROR_PAYMENT_REQUIRED = 402;

    /**
     * The request was valid, but the server is refusing action. The user might not have the necessary permissions for a resource, or may need an account of some sort.
     */
    public static final int ERROR_FORBIDDEN = 403;

    /**
     * The requested resource could not be found but may be available in the future. Subsequent requests by the client are permissible.
     */
    public static final int ERROR_NOT_FOUND = 404;

    /**
     * A request method is not supported for the requested resource; for example, a GET request on a form that requires data to be presented via POST, or a PUT request on a read-only resource.
     */
    public static final int ERROR_METHOD_NOT_ALLOWED = 405;

    /**
     * The requested resource is capable of generating only content not acceptable according to the Accept headers sent in the request. See Content negotiation.
     */
    public static final int ERROR_NOT_ACCEPTABLE = 406;

    /**
     * The server timed out waiting for the request. According to HTTP specifications: "The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time."
     */
    public static final int ERROR_REQUEST_TIMEOUT = 408;

    /**
     * Indicates that the request could not be processed because of conflict in the request, such as an edit conflict between multiple simultaneous updates.
     */
    public static final int ERROR_CONFLICT = 409;

    /**
     * Indicates that the resource requested is no longer available and will not be available again. This should be used when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code, the client should not request the resource in the future. Clients such as search engines should remove the resource from their indices. Most use cases do not require clients and search engines to purge the resource, and a "404 Not Found" may be used instead.
     */
    public static final int ERROR_GONE = 410;

    /**
     * The request did not specify the length of its content, which is required by the requested resource.
     */
    public static final int ERROR_LENGTH_REQUIRED = 411;

    /**
     * The request is larger than the server is willing or able to process. Previously called "Request Entity Too Large".
     */
    public static final int ERROR_PAYLOAD_TOO_LARGE = 413;

    /**
     * The URI provided was too long for the server to process. Often the result of too much data being encoded as a query-string of a GET request, in which case it should be converted to a POST request. Called "Request-URI Too Long" previously.
     */
    public static final int ERROR_URI_TOO_LONG = 414;

    /**
     * The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images use a different format.
     */
    public static final int ERROR_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity (hence a 415 (Unsupported Media Type) status code is inappropriate), and the syntax of the request entity is correct (thus a 400 (Bad Request) status code is inappropriate) but was unable to process the contained instructions. For example, this error condition may occur if an XML request body contains well-formed (i.e., syntactically correct), but semantically erroneous, XML instructions.
     */
    public static final int ERROR_UNPROCESSABLE_ENTITY = 422;

    /**
     * The resource that is being accessed is locked.
     */
    public static final int ERROR_LOCKED = 423;

    /**
     * The request failed because it depended on another request and that request failed (e.g., a PROPPATCH).
     */
    public static final int ERROR_FAILED_DEPENDENCY = 424;

    /**
     * The user has sent too many requests in a given amount of time. Intended for use with rate-limiting schemes.
     */
    public static final int ERROR_TOO_MANY_REQUESTS = 429;


    /**
     * A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.
     */
    public static final int ERROR_INTERNAL_SERVER_ERROR = 500;

    /**
     * The server either does not recognize the request method, or it lacks the ability to fulfil the request. Usually this implies future availability (e.g., a new feature of a web-service API).
     */
    public static final int ERROR_NOT_IMPLEMENTED = 501;

    /**
     * The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.
     */
    public static final int ERROR_SERVICE_UNAVAILABLE = 503;

    /**
     * The server is unable to store the representation needed to complete the request.
     */
    public static final int ERROR_INSUFFICIENT_STORAGE = 507;

    /**
     * Further extensions to the request are required for the server to fulfil it.
     */
    public static final int ERROR_NOT_EXTENDED = 510;

    public abstract void call(JSONObject event, T object) throws Exception;
}