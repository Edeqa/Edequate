package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.PagesStructure;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.edeqa.edequate.helpers.PagesStructure.CATEGORY;
import static com.edeqa.edequate.helpers.PagesStructure.INITIAL;
import static com.edeqa.edequate.helpers.PagesStructure.SECTION;
import static com.edeqa.edequate.helpers.PagesStructure.TITLE;

public class Pages extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

    private static final int CONTENT_MAXIMUM_LENGTH = 1024 * 1024;

    private File directory;

    private static final String REMOVE = "remove";
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

//        Misc.err("Pages", options);

        //noinspection unchecked
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        directory = new File(arguments.getWebRootDirectory());

        if(options.has(UPDATE)) {
            updatePage(json, options);
        } else if(options.has(REMOVE)) {
            removePage(json, options);
        } else if(options.has(CATEGORY)) {
            updateCategory(json, options);
        } else if(options.has(SECTION)) {
            updateSection(json, options);
        }
    }

    private void updatePage(JSONObject json, JSONObject options) throws IOException {

        PagesStructure.Page initial = null;
        PagesStructure.Page update = new PagesStructure.Page().parse(options.getJSONObject(UPDATE));

        WebPath pagesFile = update.fetchPagesFile();
        PagesStructure pagesStructure = new PagesStructure().read(pagesFile);

        if (options.has(INITIAL)) {
            initial = new PagesStructure.Page().parse(options.getJSONObject(INITIAL));
        }

        if (!validate(json, initial, update)) return;

        if(initial != null) {
            pagesStructure.replace(update, initial);
        } else {
            pagesStructure.put(update);
        }
        pagesFile.save(pagesStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated page:", update.toJSON(), "[" + update.fetchLocale() + "]");

//        WebPath updateWebPath = update.fetchContentFileName()
//        File updateFile = update.getOptionsFile();
//        PagesStructure sectionStructure;
//        if (updateFile.exists()) {
//            sectionStructure = new PagesStructure().read(updateFile);
//            if (initial != null) {
//                sectionStructure.remove(initial);
//            }
//        } else {
//            sectionStructure = new PagesStructure().parse("[]");
//        }
//        sectionStructure.put(update);
//
//        new WebPath(update.getResource()).save(update.content);
//        Misc.log("Pages", "file updated:", update.getResource(), "[" + update.content.length() + " byte(s)]");
//
//        new WebPath(updateFile).save(sectionStructure.toJSON().toString(2));
//        json.put(STATUS, STATUS_SUCCESS);
//        Misc.log("Pages", "has updated page:", update.toJSON(), "[" + update.locale + "]");
    }

    private void removePage(JSONObject json, JSONObject options) throws IOException {
        PagesStructure.Page initial = null, remove;
        remove = new PagesStructure.Page().parse(options.getJSONObject(REMOVE));

        if (!validate(json, initial, remove)) return;

        WebPath pagesFile = remove.fetchPagesFile();
        PagesStructure pagesStructure = new PagesStructure().read(pagesFile);
        pagesStructure.remove(remove);
        pagesFile.save(pagesStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has removed:", remove.toJSON());
    }

    private void updateCategory(JSONObject json, JSONObject options) throws IOException {
        PagesStructure.Category category = new PagesStructure.Category().parse(options.getJSONObject(CATEGORY));
        WebPath structureFile = category.fetchPagesFile();
        PagesStructure sectionStructure = new PagesStructure().read(structureFile);
        sectionStructure.put(category);
        structureFile.save(sectionStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated category:", category.toJSON());
    }

    private void updateSection(JSONObject json, JSONObject options) throws IOException {
        WebPath structureFile = new WebPath(directory, "data/pages-" + options.getJSONObject(SECTION).getString(SECTION) + ".json");
        PagesStructure sectionStructure = new PagesStructure().read(structureFile);

        sectionStructure.setTitle(options.getJSONObject(SECTION).getString(TITLE));
        structureFile.save(sectionStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated section:", options);
    }

    private boolean validate(JSONObject json, PagesStructure.Page initial, PagesStructure.Page update) throws IOException {
        if(initial != null) {
            if(!initial.fetchPagesFile().path().getAbsolutePath().equals(update.fetchPagesFile().path().getAbsolutePath())) {
                Misc.err("Pages", "found invalid section:", update.fetchContentFile().path().getAbsolutePath());
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_FOUND);
                json.put(MESSAGE, "Invalid data");
                return false;
            }
            if(!(new File(directory, "content").getCanonicalPath().equals(update.fetchContentFile().path().getParentFile().getParentFile().getCanonicalPath()))) {
                Misc.err("Pages", "found invalid resource file name:", update.fetchContentFile().path().getAbsolutePath());
                json.put(CODE, ERROR_FORBIDDEN);
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Invalid file name");
                return false;
            }
        }
        if(update.getContent() != null && update.getContent().length() > CONTENT_MAXIMUM_LENGTH) {
            Misc.err("Pages", "content is too large:", update);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_PAYLOAD_TOO_LARGE);
            json.put(MESSAGE, "Content is too large");
            return false;
        }
        if(update.toJSON() == null || !update.toJSON().has("type")) {
            Misc.err("Pages", "not found section or name:", update);
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_UNPROCESSABLE_ENTITY);
            json.put(MESSAGE, "Invalid data");
            return false;
        }
        return true;
    }

}
