package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.util.ArrayList;

public class Content extends FileRestAction {

    public Content() {
        super();
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            String body = request.getBody();
            if(Misc.isEmpty(body)) {
                Misc.err("Content", "not performed, arguments not defined");
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_EXTENDED);
                json.put(MESSAGE, "Arguments not defined.");
                return;
            }

            JSONObject options = new JSONObject(body);
            Misc.log("Content", "requested: " + options);

            ArrayList<WebPath> files = new ArrayList<>();

            WebPath webPath = new WebPath(getWebDirectory(), getChildDirectory());
//            WebPath webPath = new WebPath("content");

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


