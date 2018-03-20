package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.AbstractAction;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@SuppressWarnings("unused")
public class Arguments extends AbstractAction<String[]> {

    public static final String TYPE = "/rest/arguments";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, String[] args) {
//        json.put(STATUS, STATUS_SUCCESS);
    }

    public String getLogin() {
        return "";
    }

    public String getPasswordHash() {
        return "";
    }

    public String getWebRootDirectory() {
        try {
            return URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "UTF-8").split("/WEB-INF/classes/")[0];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getSSLCertificatePassword() {
        return "";
    }

    public String getKeystoreFilename() {
        return "";
    }

    public boolean isDebugMode() {
        return false;
    }

    public int getHttpPort() {
        return 80;
    }

    public int getHttpsPort() {
        return 443;
    }

    public int getHttpsAdminPort() {
        return 8989;
    }

    public int getHttpPortMasked(){
        return 80;
    }

    public int getHttpsPortMasked() {
        return 443;
    }

    public String getWrappedHttpPort() {
        return getHttpPortMasked() == 80 ? "" : ":" + getHttpPortMasked();
    }

    public String getWrappedHttpsPort() {
        return getHttpsPortMasked() == 443 ? "" : ":" + getHttpsPortMasked();
    }

    public String getLogFile() {
        return "";
    }
}
