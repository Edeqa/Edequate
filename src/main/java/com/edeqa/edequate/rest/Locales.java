package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONArray;
import org.json.JSONObject;

public class Locales extends Files {

    public Locales() {
        super();
        setIncludeDirectories(true);
        setIncludeFiles(false);
        setChildDirectory("content");
        setActionName("/rest/locales");
    }

    @Override
    public boolean call(JSONObject json, RequestWrapper request) {
        super.call(json, request);
        if (json.getString(STATUS).equals(STATUS_SUCCESS) && json.getInt(CODE) == CODE_LIST) {
            JSONArray list = json.getJSONArray(MESSAGE);
            JSONObject map = new JSONObject();

            for(int i = 0; i < list.length(); i++) {
                String id = list.getString(i);
                String name;
                switch(id) {
                    case "en":
                        name = "English";
                        break;
                    case "ru":
                        name = "Russian";
                        break;
                    default:
                        name = id.substring(0,1).toUpperCase();
                        if(id.length() > 1) {
                            name += id.substring(1,id.length());
                        }
                }
                map.put(id, name);
            }
            json.put(CODE, CODE_JSON);
            json.put(MESSAGE, map);
        }
        return true;
    }
}


