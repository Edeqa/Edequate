package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Misc;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Holders implements RestAction {

    public static final String actionName = "holders";

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            WebPath webPath = new WebPath("js/main");

            List<File> files = Arrays.asList(Objects.requireNonNull(webPath.path().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.contains("Holder");
                }
            })));

            List<String> paths = new ArrayList<>();

            for(File file: files) {
                paths.add(webPath.web(file));
            }

            Misc.log("Holders", "->", paths);

            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_LIST);
            json.put(MESSAGE, paths);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


