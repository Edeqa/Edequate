package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Common {

    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.0";

    public static String fetchBody(RequestWrapper request) {
        String body = null;
        try {
            InputStreamReader isr = new InputStreamReader(request.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            body = br.readLine();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }


    public static File getWebDirectory(String child) {
        File directory = null;
        child = Misc.isEmpty(child) ? "" : "/" + child;
        try {
            directory = new File(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0] + child);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory;
    }

}
