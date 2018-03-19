package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Page extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

    private File directory;

    private static final String CATEGORY = "category";
    private static final String CONTENT = "content";
    public static final String ICON = "icon";
    public static final String INITIAL = "initial";
    private static final String LOCALE = "locale";
    public static final String MENU = "menu";
    public static final String NAME = "name";
    private static final String PRIORITY = "priority";
    private static final String RESOURCE = "resource";
    private static final String SECTION = "section";
    private static final String TITLE = "title";
    public static final String UPDATE = "update";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        String body = request.getBody();
        if(Misc.isEmpty(body)) {
            Misc.err("Page", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        JSONObject options = new JSONObject(body);
        try {
            Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
            directory = new File(arguments.getWebRootDirectory());

            Resource initial = null, update;
            if(options.has(INITIAL)) {
                initial = new Resource().parse(options.getJSONObject(INITIAL));
            }
            update = new Resource().parse(options.getJSONObject(UPDATE));

            if(!validate(json, initial, update)) return;

            File updateFile = update.getOptions();
            PagesStructure pagesStructure;
            if(updateFile.exists()) {
                StringBuilder string = new StringBuilder();
                try(FileReader reader = new FileReader(updateFile)) {
                    int c;
                    while((c=reader.read())!=-1){
                        string.append((char)c);
                    }
                }
                pagesStructure = new PagesStructure().parse(string.toString());
                if(initial != null) {
                    pagesStructure.remove(initial);
                }
            } else {
                pagesStructure = new PagesStructure().parse("[]");
            }
            pagesStructure.put(update);

            try (FileWriter writer = new FileWriter(update.getResource())) {
                writer.write(update.content);
                Misc.log("Page", "file updated:", update.getResource(), "with content length:", update.content.length());
            } catch (Exception e) {
                Misc.err("Page", "saving failed for", update.getResource(), e);
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_RUNTIME);
                json.put(MESSAGE, e.getMessage());
            }
            try (FileWriter writer = new FileWriter(updateFile)) {
                writer.write(pagesStructure.toJSON().toString(2));
                Misc.log("Page", "has updated:", update.toJSON(), "[" + update.locale + "]");
                json.put(STATUS, STATUS_SUCCESS);
            } catch (Exception e) {
                Misc.err("Page", "saving failed for", updateFile, e);
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_RUNTIME);
                json.put(MESSAGE, e.getMessage());
            }
        } catch (Exception e) {
            Misc.err("Page", e);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_RUNTIME);
            json.put(MESSAGE, e.getMessage());
        }
    }

    private boolean validate(JSONObject json, Resource initial, Resource update) throws IOException {
        File updateFile = update.getOptions();
        if(initial != null) {
            File initialFile = initial.getOptions();
            if(!initialFile.getCanonicalPath().equals(updateFile.getCanonicalPath())) {
                Misc.err("Page", "found invalid section file name:", update.getResource().getAbsolutePath());
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_FORBIDDEN);
                json.put(MESSAGE, "Invalid section");
                return false;
            }
            if(!(new File(directory, "content").getCanonicalPath().equals(update.getResource().getParentFile().getParentFile().getCanonicalPath()))) {
                Misc.err("Page", "found invalid resource file name:", update.getResource().getAbsolutePath());
                json.put(CODE, ERROR_FORBIDDEN);
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Invalid file name");
                return false;
            }
        }
        if(update.section == null || update.name == null) {
            Misc.err("Page", "not found section or name:", update);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "Invalid data");
            return false;
        }
        return true;
    }

    class Resource {
        private boolean initial;
        private int priority;
        private String resource;
        private String icon;
        private int category;
        private String menu;
        private String section;
        private String title;
        private String name;
        private String locale;
        private String content;

        Resource parse(JSONObject json) {
            if(json.has(CATEGORY)) category = json.getInt(CATEGORY);
            if(json.has(CONTENT)) content = json.getString(CONTENT);
            if(json.has(ICON)) icon = json.getString(ICON);
            if(json.has(INITIAL)) initial = json.getBoolean(INITIAL);
            if(json.has(LOCALE)) locale = json.getString(LOCALE);
            if(json.has(MENU)) menu = json.getString(MENU);
            if(json.has(PRIORITY)) priority = json.getInt(PRIORITY);
            if(json.has(RESOURCE)) resource = json.getString(RESOURCE);
            if(json.has(SECTION)) section = json.getString(SECTION);
            if(json.has(TITLE)) title = json.getString(TITLE);

            if(json.has("type")) name = json.getString("type");
            else if(json.has(NAME)) name = json.getString(NAME);

            return this;
        }

        String getLocale() {
            return locale != null ? locale : "en";
        }

        File getOptions() {
            return new File(directory, "data/pages-" + section + ".json");
        }

        File getResource() {
            if(resource != null) return new File(directory, "content/" + getLocale() + "/" + resource);
            return new File(directory, "content/" + getLocale() + "/" + section + "-" + name + ".html");
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();

            json.put(CATEGORY, category);
            if(icon != null) json.put(ICON, icon);
            if(menu != null) json.put(MENU, menu);
            json.put("type", name);
            if(priority != 0) json.put(PRIORITY, priority);
            if(resource != null) json.put(RESOURCE, resource);
            else {
                json.put(RESOURCE, getResource().getName());
            }
            if(title != null) json.put(TITLE, title);

            return json;
        }

        @Override
        public String toString() {
            return "Resource{" +
                    "initial=" + initial +
                    ", priority=" + priority +
                    ", resource='" + resource + '\'' +
                    ", icon='" + icon + '\'' +
                    ", category=" + category +
                    ", menu='" + menu + '\'' +
                    ", section='" + section + '\'' +
                    ", title='" + title + '\'' +
                    ", name='" + name + '\'' +
                    ", locale='" + locale + '\'' +
                    (content != null ? ", content=" + content.length() : "") +
                    '}';
        }
    }

    class PagesStructure {
        private List<JSONArray> categories;
        private JSONArray json;

        PagesStructure parse(String string) {
            json = new JSONArray(string);
            categories = new ArrayList<>();
            for(int i = 0; i < 10; i++) {
                categories.add(new JSONArray());
            }
            parse(json);

            return this;
        }

        private void parse(JSONArray array) {
            for(int i = 0; i < array.length(); i++) {
                if(array.get(i) instanceof JSONArray) {
                    parse(array.getJSONArray(i));
                } else if(array.get(i) instanceof JSONObject) {
                    parse(array.getJSONObject(i));
                } else {
                    Misc.err("Page", "normalizing, ignore:", array.get(i));
                }
            }
        }

        private void parse(JSONObject object) {
            Resource resource = new Resource().parse(object);
            put(resource);
        }

        public void put(Resource resource) {
            remove(resource);
            categories.get(resource.category).put(resource.toJSON());
        }

        public void remove(Resource resource) {
            Iterator<Object> iter = categories.get(resource.category).iterator();
            while (iter.hasNext()) {
                JSONObject page = (JSONObject) iter.next();
                if(page.has("type") && page.getString("type").equals(resource.name)) {
                    iter.remove();
                    break;
                }
            }
        }

        JSONArray toJSON() {
            JSONArray json = new JSONArray();
            for (JSONArray category : categories) {
                if (category.length() > 0) {
                    json.put(category);
                }
            }
            return json;
        }
    }
}
