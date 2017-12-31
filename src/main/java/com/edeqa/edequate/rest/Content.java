package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Misc;
import com.edeqa.edequate.helpers.Common;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Content implements RestAction {

    public static final String actionName = "content";

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            JSONObject options = new JSONObject(request.getBody());
            Misc.log("Content", "requested: " + options);

            ArrayList<WebPath> files = new ArrayList<>();

            WebPath webPath = new WebPath("content");

            if (options.has("type")) {
                if (options.has(LOCALE) && options.has("resource")) {
                    files.add(webPath.webPath(options.getString("type"), options.getString(LOCALE), options.getString("resource")));
                }
                if (options.has("resource")) {
                    files.add(webPath.webPath(options.getString("type"), "en", options.getString("resource")));
                    files.add(webPath.webPath(options.getString("type"), options.getString("resource")));
                }
            } else {
                if (options.has(LOCALE) && options.has("resource")) {
                    files.add(webPath.webPath(options.getString(LOCALE), options.getString("resource")));
                }
                if (options.has("resource")) {
                    files.add(webPath.webPath("en", options.getString("resource")));
                    files.add(webPath.webPath(options.getString("resource")));
                }
            }

            boolean exists = false;
            WebPath file = null;
            for (WebPath f : files) {
                if (f.path().exists()) {
                    file = f;
                    exists = true;
                    break;
                }
            }

            if (exists) {
                String path = file.web();
                Misc.log("Content", "->", path);

                json.put(STATUS, STATUS_SUCCESS);
                json.put(CODE, CODE_REDIRECT);
                json.put(MESSAGE, path);
            } else {
                Misc.log("Content", "not found: " + files);
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_GONE);
                json.put(MESSAGE, options);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


