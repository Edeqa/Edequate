package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Redirections extends FileRestAction {

    public static final String TYPE = "/admin/rest/redirections";

    private static final int CONTENT_MAXIMUM_LENGTH = 1024 * 1024;

    private File directory;

    private static final String REMOVE = "remove";
    private static final String UPDATE = "update";

    private static final String MIME = "mime";
    private static final String PATH = "path";
    private static final String REDIRECT = "redirect";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void start() throws Exception {
        super.start();

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        directory = new File(arguments.getWebRootDirectory());
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws IOException {
        JSONObject options = request.fetchOptions();
        if(Misc.isEmpty(options)) {
            Misc.err("Redirections", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        //noinspection unchecked

        if(options.has(UPDATE)) {
            updateIgnored(json, options);
        } else if(options.has(REMOVE)) {
            removeIgnored(json, options);
        }
    }

    private void updateIgnored(JSONObject json, JSONObject options) throws IOException {
        WebPath ignoredPaths = new WebPath(directory, "data/.redirections.json");
        JSONObject map = read(ignoredPaths);

        options = options.getJSONObject(UPDATE);
        JSONObject ignored = new JSONObject();
        String path = null;
        if(options.has(PATH)) {
            path = options.getString(PATH);
            path = path.replaceFirst("^/", "");
        }
        if(options.has(CODE) && options.getString(CODE).length() > 0) {
            ignored.put(CODE, Integer.valueOf(options.getString(CODE)));
        }
        if(options.has(MESSAGE) && options.getString(MESSAGE).length() > 0) {
            ignored.put(MESSAGE, options.getString(MESSAGE));
        }
        if(options.has(REDIRECT) && options.getString(REDIRECT).length() > 0) {
            ignored.put(REDIRECT, options.getString(REDIRECT));
        }
        if(options.has(MIME) && options.getString(MIME).length() > 0) {
            ignored.put(MIME, options.getString(MIME));
        }
        if(path != null && path.length() > 0 && map.has(path)) {
            map.remove(path);
        }
        if(path != null && path.length() > 0) {
            map.put(path, ignored);
        }
        ignoredPaths.save(map.toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Redirections", "has added path:", path, "[" + ignored.toString() + "]");

    }

    private void removeIgnored(JSONObject json, JSONObject options) throws IOException {
        WebPath ignoredPaths = new WebPath(directory, "data/.redirections.json");
        JSONObject map = read(ignoredPaths);

        options = options.getJSONObject(REMOVE);

        String path = null;
        if(options.has(PATH)) {
            path = options.getString(PATH);
            path = path.replaceFirst("^/", "");
        }
        if(path != null && map.has(path)) {
            map.remove(path);
            json.put(STATUS, STATUS_SUCCESS);
            Misc.log("Redirections", "has removed path:", path);
        } else {
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Error removing path.");
            Misc.err("Redirections", "failed removing path:", path);
        }
        ignoredPaths.save(map.toString(2));
    }

    private JSONObject read(WebPath ignoredPaths) {
        JSONObject map;
        if(!Misc.isEmpty(ignoredPaths)) {
            try {
                map = new JSONObject(ignoredPaths.content());
            } catch (Exception e) {
                map = new JSONObject();
                Misc.err("Redirections", "failed reading .redirections.json, error:", e);
            }
        } else {
            map = new JSONObject();
        }
        return map;
    }

}
