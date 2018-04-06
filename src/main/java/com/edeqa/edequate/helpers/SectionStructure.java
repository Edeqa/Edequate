package com.edeqa.edequate.helpers;

public class SectionStructure {

    private static final String CATEGORIES = "categories";
    private static final String CATEGORY = "category";
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
    private static final String TITLE = "title";

/*
    private List<Category> categories;
    private JSONObject json;

    public SectionStructure read(File updateFile) throws IOException {
        StringBuilder string = new StringBuilder();
        try (FileReader reader = new FileReader(updateFile)) {
            int c;
            while ((c = reader.read()) != -1) {
                string.append((char) c);
            }
        }
        json = new JSONObject(string.toString());
        categories = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            categories.add(new Category());
        }
        if(json.has(CATEGORIES)) {
            for (int i = 0; i < json.getJSONArray(CATEGORIES).length(); i++) {
                Category category = new Category().parse(json.getJSONArray(CATEGORIES).getJSONObject(i));
                if(category != null) {
                    categories.add(category.category, category);
                }
            }
        }
        return this;
    }

    public void put(Resource resource) {
        remove(resource);
        categories.get(resource.category).put(resource);
    }

    public void put(Category category) {
        categories.set(category.category, category);
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
        for (Category category : categories) {
            if (category.length() > 0) {
                json.put(category);
            }
        }
        for (Map.Entry<Integer,Category> entry : categoriesOptions.entrySet()) {
            json.put(entry.getValue().toJSON());
        }
        return json;
    }


    public static class Resource {
        private JSONObject json;
        private boolean initial;
        private String resource;
        private Integer category;
        private Object section;
        private String name;
        private String locale;
        private String content;

        Resource parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = Integer.valueOf(path[1]);
                if(path.length > 2) resource = path[2];
            }
            if(json.has(CATEGORY)) category = Integer.valueOf(String.valueOf(json.get(CATEGORY)));
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
                           ", resource='" + resource + '\'' +
                           ", category=" + category +
                           ", section='" + section + '\'' +
                           ", name='" + name + '\'' +
                           ", locale='" + locale + '\'' +
                           (content != null ? ", content=" + content.length() : "") +
                           "}";
        }
    }

    public class Category {
        private JSONObject json;
        private Object section;
        private boolean explicit;
        private Integer category;
        private String title;

        Category() {
            explicit = true;
        }

        Category parse(JSONObject json) {
            this.json = json;
            if(json.has(PATH)) {
                String[] path = json.getString(PATH).split(":");
                section = path[0];
                if(path.length > 1) category = Integer.valueOf(path[1]);
            }
            if(json.has(CATEGORY)) category = Integer.valueOf(String.valueOf(json.get(CATEGORY)));
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
*/

}
