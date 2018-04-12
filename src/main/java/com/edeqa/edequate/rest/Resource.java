package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Resource extends FileRestAction {

    private final static String RESOURCE = "resource";
    private final static String TYPE = "type";

    public Resource() {
        super();
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        JSONObject options = request.fetchOptions();

//        String body = request.getBody();
        if(options.length() == 0) {
            Misc.err("Page", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

//        JSONObject options = new JSONObject(body);
        Misc.log("Page", "requested: " + options);

        if(!options.has(RESOURCE)) {
            Misc.err("Page", "not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, options);
            return;
        }

        ArrayList<WebPath> files = new ArrayList<>();

//            WebPath webPath = new WebPath("content");

        ArrayList<Object> resources = new ArrayList<>();
        if(options.get(RESOURCE) instanceof JSONArray) {
            resources.addAll(((JSONArray) options.get(RESOURCE)).toList());
        } else if(options.get(RESOURCE) instanceof String) {
            resources.add(options.get(RESOURCE));
        } else {
            Misc.err("Page", "defined is invalid");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_BAD_REQUEST);
            json.put(MESSAGE, options);
            return;
        }

        WebPath webPath = new WebPath(getWebDirectory(), getChildDirectory());
        for(Object x: resources) {
            String name = (String) x;
            if (options.has("type")) {
                files.add(webPath.webPath(options.getString("type"), name));
                files.add(webPath.webPath(options.getString("type"), "en", name));
                if (options.has(LOCALE) && !"en".equals(options.getString(LOCALE))) {
                    files.add(webPath.webPath(options.getString("type"), options.getString(LOCALE), name));
                }
            } else {
                files.add(webPath.webPath(name));
                files.add(webPath.webPath("en", name));
                if (options.has(LOCALE) && !"en".equals(options.getString(LOCALE))) {
                    files.add(webPath.webPath(options.getString(LOCALE), name));
                }
            }
        }
        Iterator<WebPath> iter = files.iterator();
        while(iter.hasNext()) {
            if(!iter.next().path().exists()) {
                iter.remove();
            }
        }
        if(files.isEmpty()) {
            Misc.log("Page", "not found");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_FOUND);
            json.put(MESSAGE, options);
            return;
        }
        if(files.get(0).path().getName().startsWith(".")) {
            Misc.err("Page", "requested illegal:", options);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_FOUND);
            json.put(MESSAGE, options);
            return;
        }
        if(!files.get(files.size() - 1).path().getName().endsWith(".json")) {
            String path = files.get(files.size() - 1).web();
            Misc.log("Page", "->", path);

            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_MOVED_TEMPORARILY);
            json.put(MESSAGE, path);
            return;
        }
        Object jsonContent = null;
        iter = files.iterator();
        while(iter.hasNext()) {
            try {
                Object object = null;
                String content = iter.next().content();
                try {
                    object = new JSONObject(content);
                } catch(JSONException e) {
                    try {
                        object = new JSONArray(content);
                    } catch (JSONException e1) {
                        Misc.log("Page", "json file damaged: " + files);
//                        json.put(STATUS, STATUS_ERROR);
//                        json.put(CODE, ERROR_INTERNAL_SERVER_ERROR);
//                        json.put(MESSAGE, options);
//                        return;
                    }
                }
                jsonContent = Misc.mergeJSON(jsonContent, object);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String text = jsonContent.toString();
        if(options.has(CALLBACK)) {
            text = options.get(CALLBACK) + "(" + text + ");";
        }
        new Content()
                .setMimeType(new MimeType().setMime(Mime.APPLICATION_JSON).setText(true).setGzip(true).setType("json"))
                .setContent(text)
                .setResultCode(200)
                .call(null, request);
        json.put(STATUS, STATUS_DELAYED);
    }

}


