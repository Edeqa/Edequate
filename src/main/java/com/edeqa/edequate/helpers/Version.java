package com.edeqa.edequate.helpers;

public class Version {

    private static final int VERSION_CODE = 6;
    private static final String VERSION_NAME = "2.0";

    public static int getVersionCode() {
        return VERSION_CODE;
    }

    public static String getVersionName() {
        return VERSION_NAME;
    }

    public static String getVersion() {
        return VERSION_NAME + "." + VERSION_CODE;
    }

}
