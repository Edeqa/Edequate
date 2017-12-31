package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Misc;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class WebPath {

    private String directory;
    private String child;

    public WebPath(String directory, String child) {
        this.child = Misc.isEmpty(child) ? "" : child;
        this.directory = directory;
    }

    public WebPath(String child) throws UnsupportedEncodingException {
        this(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0], child);
    }

    public File path() {
        return new File(directory, child);
    }

    public File path(String child) {
        return new File(path(), child);
    }

    public WebPath webPath(String... child) {
        StringBuilder childPath = new StringBuilder().append(this.child);
        for (String aChild : child) {
            childPath.append("/").append(aChild);
        }
        return new WebPath(directory, childPath.toString());
    }

    public String web() {
        return "/" + child;
    }

    public String web(File child) {
        return web(child.getName());
    }

    public String web(String child) {
        return "/" + this.child + "/" + child;
    }

    public String toString() {
        return path().toString();
    }
}
