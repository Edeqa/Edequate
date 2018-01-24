package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Mime;

import org.json.JSONObject;

public class MimeType {

    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String MIME = "mime";
    private static final String TEXT = "text";
    private static final String GZIP = "gzip";

    private String fullName;
    private String type;
    private String mime;
    private String charset;
    private boolean text;
    private boolean gzip;
    private static boolean gzipEnabled = false;

    public MimeType() {
        setType("");
        setMime(Mime.APPLICATION_UNKNOWN);
        setText(false);
        setGzip(false);
        setCharset("UTF-8");
    }

    public MimeType(String jsonString) {
        this(new JSONObject(jsonString));
    }

    public MimeType(JSONObject json) {
        this();
        if(json.has(NAME)) {
            setFullName(json.getString(NAME));
        } else if(json.has(TYPE)) {
            setType(json.getString(TYPE));
        }
        if(json.has(MIME)) {
            setMime(json.getString(MIME));
            if(getMime().startsWith("text")) {
                setText(true);
                setGzip(true);
            }
        }
        if(json.has(TEXT)) {
            setText(json.getBoolean(TEXT));
            setGzip(true);
        }
        if(json.has(GZIP)) {
            setGzip(json.getBoolean(GZIP));
        }
    }

    public String fetchContentType() {
        return getMime() + (isText() ? "; charset=" + getCharset() : "");
    }

    public String getType() {
        return type;
    }

    public MimeType setType(String type) {
        this.type = type.toLowerCase();
        return this;
    }

    public String getMime() {
        return mime;
    }

    public MimeType setMime(String mime) {
        this.mime = mime.toLowerCase();
        return this;
    }

    public boolean isText() {
        return text;
    }

    public MimeType setText(boolean text) {
        this.text = text;
        return this;
    }

    public boolean isGzip() {
        return gzip;
    }

    public MimeType setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public MimeType setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public static boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public static void setGzipEnabled(boolean gzipEnabled) {
        MimeType.gzipEnabled = gzipEnabled;
    }

    public String getFullName() {
        return fullName;
    }

    public MimeType setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    @Override
    public String toString() {
        return "MimeType{" +
                "fullName='" + fullName + '\'' +
                ", type='" + type + '\'' +
                ", mime='" + mime + '\'' +
                ", charset='" + charset + '\'' +
                ", text=" + text +
                ", gzip=" + gzip +
                '}';
    }
}
