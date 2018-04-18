package com.edeqa.edequate.rest.system;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.interfaces.Runnable1;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class OneTime extends AbstractAction<Void> {

    public static final String TYPE = "/rest/onetime";

    private static final String FINISHED = "finished";
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

    private static WebPath tokensFile;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject event, Void object) {
    }

    public Action create() {
        return new Action();
    }

    /**
     * One-time guaranteed browser interacted call.<p>
     * Sequence, shortly:<p>
     * - {@code start} generates public token and stores payload<p>
     * - {@code process} checks public token, generates and then verifies private token<p>
     */
    @SuppressWarnings("WeakerAccess")
    public static class Action {
        private Callable<String> onFetchToken;
        private Runnable onWelcome;
        private Runnable1<String> onStart;
        private Runnable1<String> onCheck;
        private Runnable1<JSONObject> onSuccess;
        private Runnable1<Throwable> onError;
        private HashMap<String, Serializable> payload;
        private JSONObject requestOptions;
        private Long expirationTimeout;
        private boolean strong;

        public Action() {
            //noinspection unchecked
            Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
            tokensFile = new WebPath(arguments.getWebRootDirectory() + "/data", ".once.json");
            setExpirationTimeout(EXPIRATION_TIMEOUT);
        }

        /**
         * Generates and stores the token using the payload was set with {@code setPayload}. After all calls {@code onStart} with token. Thus, {@code setPayload} and {@code setOnStart} must be called before {@code start}.
         */
        public void start() throws Exception {
            removeExpiredActions();

            JSONObject json = new JSONObject();
            json.put(TIMESTAMP, System.currentTimeMillis());
            json.put(MODE, TOKEN);
            json.put(PAYLOAD, getPayload());
            json.put(TIMEOUT, getExpirationTimeout());
            if (isStrong()) json.put(STRONG, true);

            String nonce = onFetchToken.call();
            addAction(nonce, json);
            getOnStart().call(nonce);
        }

        protected void addAction(String nonce, JSONObject json) throws IOException {
            JSONObject tokens = new JSONObject(tokensFile.path().exists() ? tokensFile.content() : "{}");
            tokens.put(nonce, json);
            tokensFile.save(tokens.toString(2));
        }

        public void process() throws Exception {
            if (getRequestOptions().has(TOKEN)) {
                String token = getRequestOptions().getString(TOKEN);
                JSONObject requested = getAction(token);
                if (requested == null) {
                    getOnError().call(new Throwable("Token not found"));
                    return;
                }
                if(requested.has(FINISHED)) {
                    getOnError().call(new Throwable("Token is already used"));
                    return;
                }
                if (System.currentTimeMillis() - requested.getLong(TIMESTAMP) > requested.getLong(TIMEOUT)) {
                    getOnError().call(new Throwable("Token expired"));
                    return;
                }
                if(requested.has(STRONG) && requested.getBoolean(STRONG)) {
                    finishAction(token);
                }
                if(getOnCheck() != null) {
                    requested.put(ORIGIN, token);
                }
                if(getOnCheck() == null) {
                    getOnSuccess().call(requested.getJSONObject(PAYLOAD));
                } else {
                    requested.put(TIMESTAMP, System.currentTimeMillis());
                    token = onFetchToken.call();
                    addAction(token, requested);
                    getOnCheck().call(token);
                }
            } else if (getRequestOptions().has(NONCE)) {
                String nonce = getRequestOptions().getString(NONCE);
                JSONObject requested = getAction(nonce);
                if (requested == null) {
                    getOnError().call(new Throwable("Intent not registered"));
                    return;
                }
                if(requested.has(FINISHED)) {
                    getOnError().call(new Throwable("Intent is already used"));
                    return;
                }
                if (requested.has(ORIGIN)) {
                    finishAction(requested.getString(ORIGIN));
                }
                finishAction(nonce);
                if (System.currentTimeMillis() - requested.getLong(TIMESTAMP) > requested.getLong(TIMEOUT)) {
                    getOnError().call(new Throwable("Intent expired"));
                    return;
                }
                getOnSuccess().call(requested.getJSONObject(PAYLOAD));
            } else {
                getOnWelcome().run();
            }
        }

        protected JSONObject getAction(String token) throws IOException {
            JSONObject tokens = new JSONObject(tokensFile.path().exists() ? tokensFile.content() : "{}");
            if(tokens.has(token)) {
                return tokens.getJSONObject(token);
            } else {
                return null;
            }
        }

        protected void removeAction(String token) throws IOException {
            JSONObject tokens = new JSONObject(tokensFile.path().exists() ? tokensFile.content() : "{}");
            if(tokens.has(token)) {
                tokens.remove(token);
                tokensFile.save(tokens.toString(2));
            }
        }

        protected void finishAction(String token) throws IOException {
            JSONObject tokens = new JSONObject(tokensFile.path().exists() ? tokensFile.content() : "{}");
            if(tokens.has(token)) {
                JSONObject action = tokens.getJSONObject(token);
                action.put(FINISHED, System.currentTimeMillis());
                tokensFile.save(tokens.toString(2));
            }
        }

        protected void removeExpiredActions() throws IOException {
            JSONObject tokens = new JSONObject(tokensFile.path().exists() ? tokensFile.content() : "{}");
            Iterator<String> iter = tokens.keys();
            boolean removed = false;
            while (iter.hasNext()) {
                String key = iter.next();
                JSONObject entry = tokens.getJSONObject(key);
                try {
                    if (System.currentTimeMillis() - entry.getLong(TIMESTAMP) > entry.getLong(TIMEOUT)) {
                        iter.remove();
                        removed = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    iter.remove();
                    removed = true;
                }
            }
            if(removed) {
                tokensFile.save(tokens.toString(2));
            }
        }

        public void setOnFetchToken(Callable<String> onFetchToken) {
            this.onFetchToken = onFetchToken;
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
}
