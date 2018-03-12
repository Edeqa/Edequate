package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable1;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Files extends FileRestAction {

    private FilenameFilter filenameFilter;
    private Comparator<File> comparator;
    private boolean includeDirectories;
    private boolean includeFiles;
    private Callable1<String,String> filenameProcess = new Callable1<String, String>() {
        @Override
        public String call(String name) {
            return name;
        }
    };

    public Files() {
        super();
        setIncludeDirectories(false);
        setIncludeFiles(true);
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
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
                    files.add(getFilenameProcess().call(file.getName()));
                }
            }
        }
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_LIST);
        json.put(MESSAGE, files);
        json.put(EXTRA, webPath.web());
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

    public Callable1<String, String> getFilenameProcess() {
        return filenameProcess;
    }

    public Files setFilenameProcess(Callable1<String, String> filenameProcess) {
        this.filenameProcess = filenameProcess;
        return this;
    }
}


