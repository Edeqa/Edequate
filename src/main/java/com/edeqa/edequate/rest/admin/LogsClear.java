package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;

@SuppressWarnings("unused")
public class LogsClear extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/logs/clear";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) throws Exception {

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);

        File file = new File(arguments.getLogFile());
        Misc.log(this.getClass().getSimpleName(), file.getCanonicalPath());

        PrintWriter writer = new PrintWriter(file);
        writer.close();

        json.put(STATUS, STATUS_SUCCESS);
    }
}
