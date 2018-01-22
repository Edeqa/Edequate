package com.edeqa.edequate.helpers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Replacements {
    private Map<String, Replacement> mapDefault;
    private Map<String, Map<String, Replacement>> mapMime;
    private Map<String, MimeType> disabled;

    public Replacements() {
        mapDefault = new LinkedHashMap<>();
        mapMime = new LinkedHashMap<>();
        disabled = new HashMap<>();
    }

    public void add(Replacement replacement) {
        mapDefault.put(replacement.getPattern(), replacement);
    }

    public void add(String mimeType, Replacement replacement) {
        Map<String, Replacement> map = new LinkedHashMap<>();
        if(mapMime.containsKey(mimeType)) {
            map = mapMime.get(mimeType);
        } else {
            mapMime.put(mimeType, map);
        }
        map.put(replacement.getPattern(), replacement);
    }

    public String process(String string, MimeType mimeType) {
        if(!mimeType.isText() || disabled.containsKey(mimeType.getMime())) {
            return string;
        }
        Map<String, Replacement> map = mapDefault;
        if(mapMime.containsKey(mimeType.getMime())) {
            map = mapMime.get(mimeType.getMime());
        }
        for(Map.Entry<String,Replacement> x: map.entrySet()) {
            string = string.replaceAll(x.getValue().getPattern(), x.getValue().getReplace());
        }
        return string;
    }

    public void disableFor(MimeType mimeType) {
        disabled.put(mimeType.getMime(), mimeType);
    }

    public void disableFor(String mimeType) {
        disabled.put(mimeType, null);
    }
}
