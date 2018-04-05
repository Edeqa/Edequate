package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Pages extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

    private File directory;

    private static final String CATEGORY = "category";
    private static final String CONTENT = "content";
    private static final String EXPLICIT = "explicit";
    public static final String ICON = "icon";
    public static final String INITIAL = "initial";
    private static final String LOCALE = "locale";
    public static final String MENU = "menu";
    public static final String NAME = "name";
    private static final String PATH = "path";
    private static final String PRIORITY = "priority";
    private static final String RESOURCE = "resource";
    private static final String REMOVE = "remove";
    private static final String SECTION = "section";
    private static final String TITLE = "title";
    public static final String UPDATE = "update";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws IOException {
        JSONObject options = request.fetchOptions();

        if(Misc.isEmpty(options)) {
            Misc.err("Pages", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        //noinspection unchecked
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        directory = new File(arguments.getWebRootDirectory());

        if(options.has(UPDATE)) {
            updatePage(json, options);
        } else if(options.has(REMOVE)) {
            removePage(json, options);
        } else if(options.has(SECTION)) {
            updateSection(json, options);
        }
    }

    private void updatePage(JSONObject json, JSONObject options) throws IOException {
        Resource initial = null, update;
        if (options.has(INITIAL)) {
            initial = new Resource().parse(options.getJSONObject(INITIAL));
        }
        update = new Resource().parse(options.getJSONObject(UPDATE));

        if (!validate(json, initial, update)) return;

        File updateFile = update.getOptionsFile();
        PagesStructure pagesStructure;
        if (updateFile.exists()) {
            pagesStructure = new PagesStructure().read(updateFile);
            if (initial != null) {
                pagesStructure.remove(initial);
            }
        } else {
            pagesStructure = new PagesStructure().parse("[]");
        }
        pagesStructure.put(update);

        new WebPath(update.getResource()).save(update.content);
        Misc.log("Pages", "file updated:", update.getResource(), "[" + update.content.length() + " byte(s)]");

        new WebPath(updateFile).save(pagesStructure.toJSON().toString(2));
        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated page:", update.toJSON(), "[" + update.locale + "]");
    }

    private void removePage(JSONObject json, JSONObject options) throws IOException {
        Resource initial = null, remove;
        remove = new Resource().parse(options.getJSONObject(REMOVE));

        if (!validate(json, initial, remove)) return;

        File removeFile = remove.getOptionsFile();
        PagesStructure pagesStructure;
        if (removeFile.exists()) {
            pagesStructure = new PagesStructure().read(removeFile);
            pagesStructure.remove(remove);

            new WebPath(removeFile).save(pagesStructure.toJSON().toString(2));
            json.put(STATUS, STATUS_SUCCESS);
            Misc.log("Pages", "has removed:", remove.toJSON());
        } else {
            Misc.err("Pages", "not found", remove);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_FOUND);
            json.put(MESSAGE, "Page not found");
        }
    }

    private void updateSection(JSONObject json, JSONObject options) throws IOException {
        Category category = new Category().parse(options.getJSONObject(SECTION));
        WebPath structureFile = new WebPath(category.getOptionsFile());
        PagesStructure pagesStructure;
        if (structureFile.path().exists()) {
            pagesStructure = new PagesStructure().read(structureFile.path());
        } else {
            pagesStructure = new PagesStructure().parse("[]");
        }
        pagesStructure.put(category);
        structureFile.save(pagesStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated category:", category.toJSON());
    }

    private boolean validate(JSONObject json, Resource initial, Resource update) throws IOException {
        if(initial != null) {
            File updateFile = update.getOptionsFile();
            boolean found = false;
            for(File file: initial.getOptionsMulti()) {
                if(file.getCanonicalPath().equals(updateFile.getCanonicalPath())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                Misc.err("Pages", "found invalid section:", update.getResource().getAbsolutePath());
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_FOUND);
                json.put(MESSAGE, "Invalid data");
                return false;
            }
            if(!(new File(directory, "content").getCanonicalPath().equals(update.getResource().getParentFile().getParentFile().getCanonicalPath()))) {
                Misc.err("Pages", "found invalid resource file name:", update.getResource().getAbsolutePath());
                json.put(CODE, ERROR_FORBIDDEN);
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Invalid file name");
                return false;
            }
        }
        if(update.section == null || update.name == null) {
            Misc.err("Pages", "not found section or name:", update);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "Invalid data");
            return false;
        }
        return true;
    }

    class Resource {
        private JSONObject json;
        private boolean initial;
        private int priority;
        private String resource;
        private String icon;
        private Object category;
        private String menu;
        private Object section;
        private String title;
        private String name;
        private String locale;
        private String content;

        Resource parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = path[1];
                if(path.length > 2) resource = path[2];
            }
            if(json.has(CATEGORY)) category = json.get(CATEGORY);
            if(category == null) category = 10;

            if(json.has(CONTENT)) content = json.getString(CONTENT);
            if(json.has(INITIAL)) initial = json.getBoolean(INITIAL);
            if(json.has(LOCALE)) locale = json.getString(LOCALE);
            if(json.has(RESOURCE)) resource = json.getString(RESOURCE);
            if(json.has(SECTION)) section = json.get(SECTION);

            if(json.has("type")) name = json.getString("type");
            else if(json.has(NAME)) name = json.getString(NAME);

            return this;
        }

        public JSONObject toJSON() {
            if(json == null) json = new JSONObject();
            JSONObject newJson = new JSONObject(json.toString());
            newJson.put(CATEGORY, category);
            newJson.put("type", name);
            if(resource != null) {
                newJson.put(RESOURCE, resource);
            } else {
                newJson.put(RESOURCE, getResource().getName());
            }
            if(newJson.has(CONTENT)) newJson.remove(CONTENT);
            if(newJson.has(INITIAL)) newJson.remove(INITIAL);
            if(newJson.has(LOCALE)) newJson.remove(LOCALE);
            if(newJson.has(NAME)) newJson.remove(NAME);
            if(newJson.has(SECTION)) newJson.remove(SECTION);

            return newJson;
        }

        String getLocale() {
            return locale != null ? locale : "en";
        }

        File getOptionsFile() {
            return new File(directory, "data/pages-" + section + ".json");
        }

        ArrayList<File> getOptionsMulti() {
            ArrayList<File> list = new ArrayList<>();
            List<Object> names;
            if(section instanceof JSONArray) {
                names = ((JSONArray) section).toList();
            } else {
                names = new ArrayList<>();
                names.add(section);
            }
            for(Object name:names) {
                list.add(new File(directory, "data/pages-" + name.toString() + ".json"));
            }
            return list;
        }

        File getResource() {
            if(resource != null) return new File(directory, "content/" + getLocale() + "/" + resource);
            return new File(directory, "content/" + getLocale() + "/" + section + "-" + name + ".html");
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
                           "}";
        }
    }

    class Category {
        private JSONObject json;
        private Object section;
        private boolean explicit;
        private Object category;
        private String title;

        Category() {
            explicit = true;
        }

        Category parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = path[1];
            }
            if(json.has(CATEGORY)) category = json.get(CATEGORY);
            if(category == null) category = 10;

            if(json.has(EXPLICIT)) explicit = json.getBoolean(EXPLICIT);
            if(json.has(SECTION)) section = json.get(SECTION);

            if(json.has(TITLE)) title = json.getString(TITLE);

            return this;
        }

        public Integer fetchCategory() {
            return Integer.valueOf("" + category);
        }

        public JSONObject toJSON() {
            if(json == null) json = new JSONObject();
            JSONObject newJson = new JSONObject(json.toString());
            newJson.put(CATEGORY, fetchCategory());
            newJson.put(EXPLICIT, explicit);
            newJson.put(SECTION, section);
            newJson.put(TITLE, title);
            if(newJson.has(PATH)) newJson.remove(PATH);
            return newJson;
        }

        File getOptionsFile() {
            return new File(directory, "data/pages-" + section + ".json");
        }

        @Override
        public String toString() {
            return "Category{" +
                           "json=" + json +
                           ", section=" + section +
                           ", explicit=" + explicit +
                           ", category=" + category +
                           ", title='" + title + '\'' +
                           '}';
        }
    }

    class PagesStructure {
        private List<JSONArray> categories;
        private JSONArray json;
        private Map<Integer,Category> categoriesOptions;

        PagesStructure parse(String string) {
            json = new JSONArray(string);
            categories = new ArrayList<>();
            categoriesOptions = new HashMap<>();
            for(int i = 0; i < 11; i++) {
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
                    Misc.err("Pages", "normalizing, ignore:", array.get(i));
                }
            }
        }

        private void parse(JSONObject object) {
            if(object.has(RESOURCE)) {
                put(new Resource().parse(object));
            } else if(object.has(SECTION)) {
                put(new Category().parse(object));
            }
        }

        public void put(Resource resource) {
            remove(resource);
            categories.get(Integer.valueOf(resource.category.toString())).put(resource.toJSON());
        }

        public void put(Category category) {
            if(categoriesOptions.containsKey(category.fetchCategory())) {
                Category existing = categoriesOptions.get(category.fetchCategory());
                if(category.json.has(TITLE)) {
                    existing.title = category.title;
                }
                if(category.json.has(EXPLICIT)) {
                    existing.explicit = category.explicit;
                }
            } else {
                categoriesOptions.put(category.fetchCategory(), category);
            }
        }

        public void remove(Resource resource) {
            Iterator<Object> iter = categories.get(Integer.valueOf(resource.category.toString())).iterator();
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
            for (Map.Entry<Integer,Category> entry : categoriesOptions.entrySet()) {
                json.put(entry.getValue().toJSON());
            }
            return json;
        }

        public PagesStructure read(File updateFile) throws IOException {
            StringBuilder string = new StringBuilder();
            try (FileReader reader = new FileReader(updateFile)) {
                int c;
                while ((c = reader.read()) != -1) {
                    string.append((char) c);
                }
            }
            parse(string.toString());
            return this;
        }
    }
}
