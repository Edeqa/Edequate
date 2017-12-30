package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

import java.util.HashMap;

public class Locales implements RestAction {

    public static final String actionName = "locales";

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_JSON);

            JSONObject locales = new JSONObject(new HashMap<String,String>(){{
                put("en","English");
            }});

            json.put(MESSAGE, locales);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


