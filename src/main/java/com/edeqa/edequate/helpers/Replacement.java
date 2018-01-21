package com.edeqa.edequate.helpers;

import javax.activation.MimeType;

public class Replacement {

    private String pattern;
    private String replace;

    public String getPattern() {
        return pattern;
    }

    public Replacement setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getReplace() {
        return replace;
    }

    public Replacement setReplace(String replace) {
        this.replace = replace;
        return this;
    }

    @Override
    public String toString() {
        return "Replacement{" +
                "pattern='" + pattern + '\'' +
                ", replace='" + replace + '\'' +
                '}';
    }
}
