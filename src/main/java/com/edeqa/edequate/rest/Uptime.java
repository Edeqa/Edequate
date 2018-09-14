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

        json.put(MESSAGE, "Uptime is " + fetchString());
        json.put(EXTRA, fetchLong());
    }

    public Long fetchLong() {
        return getTimer().total();
    }

    public String fetchString() {
        Long total = fetchLong();

        long seconds = total / 1000;
//        total -= seconds*1000;

        long minutes = seconds / 60;
        seconds -= minutes*60;

        long hours = minutes / 60;
        minutes -= hours*60;

        long days = hours / 24;
        hours -= days*24;

        StringBuilder message = new StringBuilder();
        if(days > 0) message.append(days).append(" days ");
        if(hours > 0) message.append(hours).append(" hours ");
        if(minutes > 0) message.append(minutes).append(" minutes ");
        if(seconds > 0) message.append(seconds).append(" seconds");

        return message.toString();
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void setTimer(Timer timer) {
        Uptime.timer = timer;
    }
}
