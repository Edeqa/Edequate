package com.edeqa.edequate.helpers;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.Locales;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;
import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;

public class PagesStructure {

    private static final String CATEGORIES = "categories";
    public static final String CATEGORY = "category";
    private static final String COLLAPSIBLE = "collapsible";
    private static final String CONTENT = "content";
    private static final String EXPLICIT = "explicit";
    public static final String ICON = "icon";
    public static final String INITIAL = "initial";
    private static final String LOCALE = "locale";
    public static final String MENU = "menu";
    public static final String NAME = "name";
    private static final String PAGES = "pages";
    private static final String PATH = "path";
    private static final String PRIORITY = "priority";
    private static final String RESOURCE = "resource";
    public static final String SECTION = "section";
    public static final String TITLE = "title";
    private static final File directory;

    private List<Category> categories;
    private String section;
    private JSONObject json;

    static {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        directory = new File(arguments.getWebRootDirectory());
    }

    public PagesStructure read(WebPath updateWebPath) {
        try {
            json = new JSONObject(updateWebPath.content());
        } catch (Exception e){
            e.printStackTrace();
            json = new JSONObject();
        }
        section = "";
        if(json.has(SECTION)) section = json.getString(SECTION);
        categories = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            Category category = new Category();
            category.category = i;
            category.section = section;
            categories.add(category);
        }
        if(json.has(CATEGORIES)) {
            for (int i = 0; i < json.getJSONArray(CATEGORIES).length(); i++) {
                Category category = new Category();
                category.section = section;
                category.parse(json.getJSONArray(CATEGORIES).getJSONObject(i));
                categories.set(category.category, category);
            }
        }
        return this;
    }

    public void put(Page page) throws IOException {
        categories.get(page.category).pages.add(page);
        page.saveContent();
    }

    public void replace(Page newPage, Page oldPage) throws IOException {
        List<Page> pages = categories.get(newPage.category).pages;
        for (int i = 0; i < pages.size(); i++) {
            Page item = pages.get(i);
            if (item.name != null && item.name.equals(oldPage.name)) {
//                remove(oldPage);
                pages.set(i, newPage);
                newPage.saveContent();
                return;
            }
        }
        pages.add(newPage);
        newPage.saveContent();
    }

    public void put(Category category) {
        if(category == null) return;
        if(categories.size() > category.category) {
            Category oldCategory = categories.get(category.category);
            oldCategory.collapsible = category.collapsible;
            oldCategory.explicit = category.explicit;
            oldCategory.title = category.title;
        } else {
            categories.set(category.category, category);
        }
    }

    public void remove(Page resource) {
        Category category = categories.get(resource.category);
        for(Page page: category.pages) {
            if(page.name != null && page.name.equals(resource.name)) {
                category.pages.remove(page);
                for(WebPath file: page.fetchContentFiles()) {
                    if(file.path().exists()) {
                        file.path().delete();
                    }
                }
                break;
            }
        }
    }

    public JSONObject toJSON() {
        JSONArray array = new JSONArray();
        for (Category category : categories) {
            JSONObject jsonCategory = category.toJSON();
            if(jsonCategory != null) {
                array.put(jsonCategory);
            }
        }
        json.put(CATEGORIES, array);
        return json;
    }

    public void setTitle(String title) {
        if(title != null) {
            json.put(TITLE, title);
        } else if(json.has(TITLE)){
            json.remove(TITLE);
        }
    }

    public static class Page {
        private JSONObject json;
        private boolean initial;
        private String resource;
        private Integer category;
        private Integer priority;
        private Object section;
        private String name;
        private String locale;
        private String content;

        public Page parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = Integer.valueOf(path[1]);
                if(path.length > 2) name = path[2];
            }
            if(json.has(CATEGORY)) category = Integer.valueOf(String.valueOf(json.get(CATEGORY)));
            if(category == null) category = 10;

            if(json.has(PRIORITY)) priority = Integer.valueOf(String.valueOf(json.get(PRIORITY)));
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
                newJson.put(RESOURCE, fetchContentFileName());
            }
            if(priority != null && priority != 0) newJson.put(PRIORITY, priority);
            if(newJson.has(CONTENT)) newJson.remove(CONTENT);
            if(newJson.has(INITIAL)) newJson.remove(INITIAL);
            if(newJson.has(LOCALE)) newJson.remove(LOCALE);
            if(newJson.has(NAME)) newJson.remove(NAME);
            if(newJson.has(SECTION)) newJson.remove(SECTION);

            return newJson;
        }

        public String fetchLocale() {
            return locale != null ? locale : "en";
        }

        public WebPath fetchPagesFile() {
            return new WebPath(directory, "data/pages-" + section + ".json");
        }

        public String fetchContentFileName() {
            return section + "-" + name + ".html";
        }

        public WebPath fetchContentFile() {
            return new WebPath(directory, "content/" + fetchLocale() + "/" + fetchContentFileName());
        }

        public String getContent() {
            if(json.has(CONTENT)) return json.getString(CONTENT);
            return null;
        }

        public ArrayList<WebPath> fetchContentFiles() {
            Locales locales = (Locales) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder("/rest/locales");
            Map<String, Object> localesMap = locales.fetchLocales();
            ArrayList<WebPath> list = new ArrayList<>();
            for(Map.Entry<String,Object> entry: localesMap.entrySet()) {
                WebPath webPath = new WebPath(directory, "content/" + entry.getKey() + "/" + section + "-" + name + ".html");
                if(webPath.path().exists()) list.add(webPath);
            }
            return list;
        }

        @Override
        public String toString() {
            return "Page{" +
                           "json=" + json +
                           ", initial=" + initial +
                           ", resource='" + resource + '\'' +
                           ", category=" + category +
                           ", section=" + section +
                           ", name='" + name + '\'' +
                           ", locale='" + locale + '\'' +
                           (content != null ? ", content=[" + content.length() + " byte(s)]" : "") +
                           '}';
        }

        public void saveContent() throws IOException {
            fetchContentFile().save(content);
        }
    }

    public static class Category {
        private JSONObject json;
        private Object section;
        private boolean collapsible;
        private boolean explicit;
        private Integer category;
        private String title;
        private List<Page> pages;

        public Category() {
            pages = new ArrayList<>();
        }

        public Category parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = Integer.valueOf(path[1]);
                json.remove(PATH);
            }
            if(json.has(CATEGORY)) category = Integer.valueOf(String.valueOf(json.get(CATEGORY)));
            if(category == null) category = 10;

            if(json.has(EXPLICIT)) {
                explicit = json.getBoolean(EXPLICIT);
                json.remove(EXPLICIT);
            }
            if(json.has(COLLAPSIBLE)) {
                collapsible = json.getBoolean(COLLAPSIBLE);
                json.remove(COLLAPSIBLE);
            }
            if(json.has(SECTION)) section = json.remove(SECTION);
            if(json.has(TITLE)) title = json.getString(TITLE);

            pages = new ArrayList<>();
            if(json.has(PAGES)) {
                JSONArray pages = json.getJSONArray(PAGES);
                for(int i = 0; i < pages.length(); i++) {
                    Page page = new Page().parse(pages.getJSONObject(i));
                    page.category = i;
                    page.section = section;
                    this.pages.add(page);
                }
            }
            return this;
        }

        public Integer fetchCategory() {
            return Integer.valueOf("" + category);
        }

        public JSONObject toJSON() {
            boolean defined = false;
            if(json == null) {
                json = new JSONObject();
            } else {
                defined = true;
            }
            JSONObject newJson = new JSONObject(json.toString());
            newJson.put(CATEGORY, fetchCategory());
            if(newJson.length() == 1) defined = false;
            if(explicit) {
                newJson.put(EXPLICIT, true);
                defined = true;
            }
            if(collapsible) {
                newJson.put(COLLAPSIBLE, true);
                defined = true;
            }
            if(title != null) {
                newJson.put(TITLE, title);
                defined = true;
            }

            JSONArray pages = new JSONArray();
            for(Page page: this.pages) {
                JSONObject pageJson = page.toJSON();
                if(pageJson != null) {
                    pages.put(pageJson);
                }
            }
            if(newJson.has(PAGES)) newJson.remove(PAGES);
            if(pages.length() > 0) {
                newJson.put(PAGES, pages);
                defined = true;
            }
            if(!defined) return null;
            return newJson;
        }

        public WebPath fetchPagesFile() {
            return new WebPath(directory, "data/pages-" + section + ".json");
        }

        @Override
        public String toString() {
            return "Category{" +
                           "json=" + json +
                           ", section=" + section +
                           ", collapsible=" + collapsible +
                           ", explicit=" + explicit +
                           ", category=" + category +
                           ", title='" + title + '\'' +
                           ", pages=" + pages +
                           '}';
        }
    }

}
