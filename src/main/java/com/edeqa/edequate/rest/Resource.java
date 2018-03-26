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
        String body = request.getBody();
        if(Misc.isEmpty(body)) {
            Misc.err("Resource", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        JSONObject options = new JSONObject(body);
        Misc.log("Resource", "requested: " + options);

        if(!options.has(RESOURCE)) {
            Misc.err("Resource", "not defined");
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
            Misc.err("Resource", "defined is invalid");
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
            Misc.log("Resource", "not found: " + files);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_FOUND);
            json.put(MESSAGE, options);
            return;
        }
        if(files.get(0).path().getName().startsWith(".")) {
            Misc.err("Resource", "requested illegal:", options);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_FOUND);
            json.put(MESSAGE, options);
            return;
        }
        if(!files.get(files.size() - 1).path().getName().endsWith(".json")) {
            String path = files.get(files.size() - 1).web();
            Misc.log("Resource", "->", path);

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
                        Misc.log("Resource", "json file damaged: " + files);
//                        json.put(STATUS, STATUS_ERROR);
//                        json.put(CODE, ERROR_INTERNAL_SERVER_ERROR);
//                        json.put(MESSAGE, options);
//                        return;
                    }
                }
                jsonContent = deepMergeJSON(jsonContent, object);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Content()
                .setMimeType(new MimeType().setMime(Mime.APPLICATION_JSON).setText(true).setGzip(true).setType("json"))
                .setContent(jsonContent.toString())
                .setResultCode(200)
                .call(null, request);
        json.put(STATUS, STATUS_DELAYED);
    }

    private static Object deepMergeJSON(Object base, Object override) {
        if(base == null) {
            base = override;
        } else if(base instanceof JSONObject && override instanceof JSONObject) {
            Iterator<String> keys = ((JSONObject) override).keys();

            while(keys.hasNext()) {
                String key = keys.next();
//                System.out.println(key + ":" + ((JSONObject) override).get(key)+ ":"
//                + (!((JSONObject) base).has(key) || (!(((JSONObject) base).get(key) instanceof JSONObject) && !(((JSONObject) base).get(key) instanceof JSONArray))));
                if(!((JSONObject) base).has(key) || (!(((JSONObject) base).get(key) instanceof JSONObject) && !(((JSONObject) base).get(key) instanceof JSONArray))) {
                    ((JSONObject) base).put(key, ((JSONObject) override).get(key));
                } else {
                    Misc.err("Admins", "found collision merging JSONs for key:", key);
                }
            }
        } else if(base instanceof JSONArray && override instanceof JSONArray) {
            for(int i = 0; i < ((JSONArray) override).length(); i++) {
                if(((JSONArray) base).length() < i) {
                    ((JSONArray) base).put(((JSONArray) override).get(i));
                } else {
                    deepMergeJSON(((JSONArray) base).get(i), ((JSONArray) override).get(i));
                }
            }
        } else {
            Misc.err("Admins", "found issue merging JSONs", base, override);
        }
        return base;
    }

}


