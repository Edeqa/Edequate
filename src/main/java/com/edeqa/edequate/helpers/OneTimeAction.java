package com.edeqa.edequate.helpers;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.interfaces.Runnable1;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;

import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;

public class OneTimeAction {

    private static final String IP = "ip";
    public static final String LINK = "link";
    private static final String MODE = "mode";
    public static final String NONCE = "nonce";
    private static final String ORIGIN = "origin";
    private static final String PAYLOAD = "payload";
    private static final String STRONG = "strong";
    public static final String TIMEOUT = "timeout";
    public static final String TIMESTAMP = "timestamp";
    public static final String TOKEN = "token";

    private static final long EXPIRATION_TIMEOUT = 1000 * 60 * 15L;

    private Callable<String> onFetchToken;
    private Runnable onWelcome;
    private Runnable1<String> onStart;
    private Runnable1<String> onCheck;
    private Runnable1<JSONObject> onSuccess;
    private Runnable1<Throwable> onError;
    private RequestWrapper requestWrapper;
    private HashMap<String, Serializable> payload;
    private JSONObject requestOptions;
    private WebPath tokensFile;
    private Long expirationTimeout;
    private boolean strong;


    /**
     * One-time guaranteed browser interacted call.<p>
     * Sequence, shortly:<p>
     * - {@code start} generates public token and stores payload<p>
     * - {@code process} checks public token, generates and then verifies private token<p>
     */
    public OneTimeAction() {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        tokensFile = new WebPath(arguments.getWebRootDirectory() + "/data", ".once.json");
        setExpirationTimeout(EXPIRATION_TIMEOUT);
    }

    /**
     * Generates and stores the token using the payload was set with {@code setPayload}. After all calls {@code onStart} with token. Thus, {@code setPayload} and {@code setOnStart} must be called before {@code start}.
     * @throws Exception
     */
    public void start() throws Exception {
        JSONObject tokens;
        try {
            tokens = new JSONObject(tokensFile.content());
        } catch (Exception e) {
            tokens = new JSONObject();
        }

        Iterator<String> iter = tokens.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            JSONObject entry = tokens.getJSONObject(key);
            try {
                if (System.currentTimeMillis() - entry.getLong(TIMESTAMP) > entry.getLong(TIMEOUT)) {
                    iter.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
                iter.remove();
            }
        }

        JSONObject json = new JSONObject();
        json.put(TIMESTAMP, System.currentTimeMillis());
        json.put(IP, getRequestWrapper().getRemoteAddress().getAddress().getHostAddress());
        json.put(MODE, TOKEN);
        json.put(PAYLOAD, getPayload());
        json.put(TIMEOUT, getExpirationTimeout());
        if (isStrong()) json.put(STRONG, true);

        String nonce = onFetchToken.call();
        tokens.put(nonce, json);

        tokensFile.save(tokens.toString(2));

        getOnStart().call(nonce);
    }

    public void process() throws Exception {
        JSONObject tokens;
        if (getRequestOptions().has(TOKEN)) {
            try {
                tokens = new JSONObject(tokensFile.content());
            } catch (Exception e) {
                tokens = new JSONObject();
            }
            String token = getRequestOptions().getString(TOKEN);
            if (!tokens.has(token)) {
                getOnError().call(new Throwable("Token not found"));
                return;
            }
            JSONObject requested = tokens.getJSONObject(token);

            if (requested.has(STRONG) && requested.getBoolean(STRONG)) {
                tokens.remove(token);
            } else {
                requested.put(ORIGIN, token);
            }

            if (System.currentTimeMillis() - requested.getLong(TIMESTAMP) > requested.getLong(TIMEOUT)) {
                getOnError().call(new Throwable("Token expired"));
                return;
            }

            requested.put(TIMESTAMP, System.currentTimeMillis());
            requested.put(IP, getRequestWrapper().getRemoteAddress().getAddress().getHostAddress());

            token = onFetchToken.call();
            tokens.put(token, requested);

            tokensFile.save(tokens.toString(2));

            getOnCheck().call(token);
        } else if (getRequestOptions().has(NONCE)) {
            try {
                tokens = new JSONObject(tokensFile.content());
            } catch (Exception e) {
                tokens = new JSONObject();
            }
            String nonce = getRequestOptions().getString(NONCE);
            if (!tokens.has(nonce)) {
                getOnError().call(new Throwable("Nonce not registered"));
                return;
            }
            JSONObject requested = tokens.getJSONObject(nonce);

            if (requested.has(ORIGIN) && tokens.has(requested.getString(ORIGIN))) {
                tokens.remove(requested.getString(ORIGIN));
            }

            tokens.remove(nonce);
            tokensFile.save(tokens.toString(2));

            if (System.currentTimeMillis() - requested.getLong(TIMESTAMP) > requested.getLong(TIMEOUT)) {
                getOnError().call(new Throwable("Nonce expired"));
                return;
            }
            getOnSuccess().call(requested.getJSONObject(PAYLOAD));
        } else {
            getOnWelcome().run();
        }
    }

    public void setOnFetchToken(Callable<String> onFetchToken) {
        this.onFetchToken = onFetchToken;
    }

    public void setRequestWrapper(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    public RequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestOptions(JSONObject requestOptions) {
        this.requestOptions = requestOptions;
    }

    private JSONObject getRequestOptions() {
        return requestOptions;
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public Runnable1<Throwable> getOnError() {
        return onError;
    }

    public void setOnError(Runnable1<Throwable> onError) {
        this.onError = onError;
    }

    public void setPayload(HashMap<String, Serializable> payload) {
        this.payload = payload;
    }

    private HashMap<String, Serializable> getPayload() {
        return payload;
    }

    public void setOnCheck(Runnable1<String> onCheck) {
        this.onCheck = onCheck;
    }

    private Runnable1<String> getOnCheck() {
        return onCheck;
    }

    public void setOnWelcome(Runnable onWelcome) {
        this.onWelcome = onWelcome;
    }

    private Runnable getOnWelcome() {
        return onWelcome;
    }

    public void setOnStart(Runnable1<String> onStart) {
        this.onStart = onStart;
    }

    private Runnable1<String> getOnStart() {
        return onStart;
    }

    public Long getExpirationTimeout() {
        return expirationTimeout;
    }

    private void setExpirationTimeout(Long expirationTimeout) {
        this.expirationTimeout = expirationTimeout;
    }

    private boolean isStrong() {
        return strong;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }
}
