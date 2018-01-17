package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Files extends FileRestAction {

    private FilenameFilter filenameFilter;
    private Comparator<File> comparator;
    private boolean includeDirectories;
    private boolean includeFiles;

    public Files() {
        super();
        setIncludeDirectories(false);
        setIncludeFiles(true);
    }

    @Override
    public String getApiVersion() {
        return "v1";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            Misc.log("Files", "list for:", getWebDirectory() + "/" + getChildDirectory(), "[", isIncludeDirectories() ? "directories" : "", isIncludeFiles() ? "files" : "", "]");

            WebPath webPath = new WebPath(getWebDirectory(), getChildDirectory());

            List<String> files = new ArrayList<>();
            File[] list;
            if(getFilenameFilter() != null) {
                list = webPath.path().listFiles(getFilenameFilter());
            } else {
                list = webPath.path().listFiles();
            }

            if(list != null && list.length > 0) {
                if(getComparator() != null) {
                    Arrays.sort(list, comparator);
                }
                for (File file : list) {
                    if (isIncludeDirectories() && file.isDirectory()) {
                        files.add(file.getName());
                    }
                    if (isIncludeFiles() && file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }

            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_LIST);
            json.put(MESSAGE, files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isIncludeDirectories() {
        return includeDirectories;
    }

    public Files setIncludeDirectories(boolean includeDirectories) {
        this.includeDirectories = includeDirectories;
        return this;
    }

    private boolean isIncludeFiles() {
        return includeFiles;
    }

    public Files setIncludeFiles(boolean includeFiles) {
        this.includeFiles = includeFiles;
        return this;
    }

    private FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }

    public Files setFilenameFilter(FilenameFilter filenameFilter) {
        this.filenameFilter = filenameFilter;
        return this;
    }

    public Comparator<File> getComparator() {
        return comparator;
    }

    public Files setComparator(Comparator<File> comparator) {
        this.comparator = comparator;
        return this;
    }
}


