package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Mime;

import org.json.JSONArray;

import java.util.LinkedHashMap;
import java.util.Map;

public class MimeTypes {

    private Map<String, MimeType> types;
    private Map<String, MimeType> fullNames;

    private MimeType defaultMime;

    public MimeTypes() {
        types = new LinkedHashMap<>();
        fullNames = new LinkedHashMap<>();
        setDefaultMime(new MimeType().setMime(Mime.APPLICATION_OCTET_STREAM).setText(false));
    }

    public void add(MimeType mimeType) {
        types.put(mimeType.getType(), mimeType);
    }

    public MimeType fetchMimeFor(String fileName) {
        fileName = fileName.toLowerCase();
        for(Map.Entry<String,MimeType> entry: fullNames.entrySet()) {
            if(fileName.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        String[] x = fileName.split("\\.");
        if(x.length > 1) {
            fileName = x[x.length -1];
        } else {
            fileName = "";
        }
        for(Map.Entry<String,MimeType> entry: types.entrySet()) {
            if(fileName.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return getDefaultMime();
    }

    public MimeType getDefaultMime() {
        return defaultMime;
    }

    public void setDefaultMime(MimeType defaultMime) {
        this.defaultMime = defaultMime;
    }

    public MimeTypes parse(JSONArray types) {
        for(int i = 0; i < types.length(); i++) {
            add(new MimeType(types.getJSONObject(i)));
        }
        return this;
    }

    public MimeTypes useDefault() {
        add(new MimeType().setFullName("manifest.json").setMime(Mime.APPLICATION_X_WEB_APP_MANIFEST_JSON).setText(true));

        add(new MimeType().setType("html").setMime(Mime.TEXT_HTML).setText(true));
        add(new MimeType().setType("js").setMime(Mime.APPLICATION_JAVASCRIPT).setText(true));
        add(new MimeType().setType("css").setMime(Mime.TEXT_CSS).setText(true));
        add(new MimeType().setType("xml").setMime(Mime.APPLICATION_XML).setText(true));
        add(new MimeType().setType("json").setMime(Mime.APPLICATION_JSON).setText(true));
        add(new MimeType().setType("gif").setMime(Mime.IMAGE_GIF));
        add(new MimeType().setType("png").setMime(Mime.IMAGE_PNG));
        add(new MimeType().setType("jpg").setMime(Mime.IMAGE_JPG));
        add(new MimeType().setType("ico").setMime(Mime.IMAGE_ICO));
        add(new MimeType().setType("svg").setMime(Mime.IMAGE_SVG_XML).setText(true));
        add(new MimeType().setType("mp3").setMime(Mime.AUDIO_MP3));
        add(new MimeType().setType("ogg").setMime(Mime.AUDIO_OGG));
        add(new MimeType().setType("m4r").setMime(Mime.AUDIO_AAC));

        return this;
    }
}
