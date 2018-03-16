package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

public class Page extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

    public static final String CATEGORY = "category";
    public static final String CONTENT = "content";
    public static final String LANGUAGE = "language";
    public static final String NAME = "name";
    public static final String PRIORITY = "priority";
    public static final String SECTION = "section";
    public static final String TITLE = "title";

    @Override
    public String getType() {
        return TYPE;
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
        Misc.log("Page", "came: " + options.toString(4));

        try {
            int category = options.getInt(CATEGORY);
            String content = options.getString(CONTENT);
            String language = options.getString(LANGUAGE);
            String name = options.getString(NAME);
            int priority = options.getInt(PRIORITY);
//            int priority = Integer.getInteger(options.getString(PRIORITY));
            String section = options.getString(SECTION);
            String title = options.getString(TITLE);

            Misc.log("Page", category, language, priority);

            json.put(STATUS, STATUS_SUCCESS);
        } catch (Exception e) {
            Misc.err("Page", e.getMessage());

            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_RUNTIME);
            json.put(MESSAGE, e.getMessage());
        }


    }

}


