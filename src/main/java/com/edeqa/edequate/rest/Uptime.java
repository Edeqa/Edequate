package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Timer;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class Uptime extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/uptime";

    private static Timer timer;


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void start() {
        setTimer(new Timer());
        getTimer().start();
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        SimpleDateFormat format = new SimpleDateFormat("dd hh:mm:ss");

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_STRING);

        Long tick = getTimer().tick();

//        json.put(MESSAGE, "Uptime is " + format.format(tick));
        json.put(EXTRA, tick);
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void setTimer(Timer timer) {
        Uptime.timer = timer;
    }
}
