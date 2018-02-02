package com.edeqa.edequate.abstracts;

import com.edeqa.edequate.helpers.RequestWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@SuppressWarnings("WeakerAccess")
public abstract class FileRestAction extends AbstractAction<RequestWrapper> {

    private String childDirectory;
    private String webDirectory;
    private String actionName;

    public FileRestAction() {
        try {
            //noinspection ConstantConditions
            setWebDirectory(URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setChildDirectory("content");
    }

    @Override
    public String getType() {
        if(actionName != null) return actionName;
        return getChildDirectory();
    }

    public String getChildDirectory() {
        return childDirectory;
    }

    public FileRestAction setChildDirectory(String childDirectory) {
        this.childDirectory = childDirectory;
        return this;
    }

    public String getWebDirectory() {
        return webDirectory;
    }

    public FileRestAction setWebDirectory(String webDirectory) {
        this.webDirectory = webDirectory;
        return this;
    }

    public FileRestAction setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

}



