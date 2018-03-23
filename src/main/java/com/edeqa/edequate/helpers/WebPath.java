package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Misc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class WebPath {

    private final String directory;
    private String child;

    public WebPath(String directory, String child) {
        this.child = Misc.isEmpty(child) ? "" : child;
        this.child = this.child.replaceAll("^/", "");
//        if("/".equals(this.child)) this.child = "";
        this.directory = directory;
    }

    public WebPath(String child) throws UnsupportedEncodingException {
        this(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0], child);
    }

    public WebPath(File file) {
        this.directory = file.getParent();
        this.child = file.getName();
    }

    public File path() {
        return Misc.isEmpty(this.child) ? new File(directory) : new File(directory, child);
    }

    public File path(String child) {
        return new File(path(), child);
    }

    public WebPath webPath(String... child) {
        StringBuilder childPath = new StringBuilder().append(this.child);
        for (String aChild : child) {
            if(!Misc.isEmpty(aChild)) {
                if(childPath.length() > 0) childPath.append("/");
                childPath.append(aChild);
            }
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
        return "/" + (Misc.isEmpty(this.child) ? "": this.child + "/") + child;
    }

    public String web(Integer trim) {
        List<String> parts = Arrays.asList(child.split("/"));
        if(trim > parts.size()) trim = parts.size();
        parts = parts.subList(0, trim);
        return "/" + Misc.join("/", parts);
    }

    public String toString() {
        return path().toString();
    }

    public String content() throws IOException {
        int c;
        StringBuilder fileContent = new StringBuilder();
        try(FileReader reader = new FileReader(path())) {
            while ((c = reader.read()) != -1) {
                fileContent.append((char) c);
            }
            reader.close();
        }
        return fileContent.toString();
    }

    public boolean rename(String newFilename) {
        return path().renameTo(new File(path().getParent(), newFilename));
    }
}
