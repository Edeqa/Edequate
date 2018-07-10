package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.util.Iterator;

@SuppressWarnings("unused")
public class IgnoredPath extends AbstractAction<RequestWrapper> {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String REDIRECT = "redirect";

    private String type = "/rest/ignored";
    private static boolean initialized;
    private JSONObject reaction;

    public IgnoredPath() {
        setReaction(new JSONObject());
    }

    public IgnoredPath(String path) {
        this();
        setType(path);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void start() throws Exception {
        super.start();
        if(isInitialized()) return;
        setInitialized(true);

        try {
            Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
            WebPath ignoredPaths = new WebPath(arguments.getWebRootDirectory(), "data/.ignored.json");

            if(!Misc.isEmpty(ignoredPaths)) {
                EventBus<AbstractAction> restBus = (EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS);

                JSONObject map = new JSONObject(ignoredPaths.content());
                Iterator<String> iter = map.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        IgnoredPath ignoredPath = new IgnoredPath("/" + key);
                        ignoredPath.setReaction(map.getJSONObject(key));
                        restBus.registerIfAbsent(ignoredPath);
                        Misc.log("IgnoredPath", "will catch [/" + key + "]");
                    } catch (Exception e) {
                        Misc.err("IgnoredPath", "failed with [/" + key + "]", e);
                    }
                }
            }
        } catch (Exception e) {
            Misc.err("IgnoredPath", "failed, error:", e);
        }
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {

        json.put(STATUS, STATUS_DELAYED);

        int code = 404;
        String message = "Not found";
        if(getReaction().has(REDIRECT)) {
            try {
                String path = String.valueOf(getReaction().get(REDIRECT));
                request.sendRedirect(path);
                return;
            } catch(Exception e) {
                Misc.err("IgnoredPath", "failed, error:", e);
            }
        }
        if(getReaction().has(CODE)) {
            try {
                code = Integer.parseInt(String.valueOf(getReaction().get(CODE)));
            } catch(Exception e) {
                Misc.err("IgnoredPath", "failed, error:", e);
            }
        }
        if(getReaction().has(MESSAGE)) {
            try {
                message = String.valueOf(getReaction().get(MESSAGE));
            } catch(Exception e) {
                Misc.err("IgnoredPath", "failed, error:", e);
            }
        }
        request.sendError(code, message);

    }

    public void setType(String type) {
        this.type = type;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setInitialized(boolean initialized) {
        IgnoredPath.initialized = initialized;
    }

    public JSONObject getReaction() {
        return reaction;
    }

    public void setReaction(JSONObject reaction) {
        this.reaction = reaction;
    }
}
