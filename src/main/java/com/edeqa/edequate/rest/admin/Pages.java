package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Pages extends FileRestAction {

    public static final String TYPE = "/admin/rest/page";

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

        //noinspection unchecked
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        directory = new File(arguments.getWebRootDirectory());

//        if(options.has(UPDATE)) {
//            updatePage(json, options);
//        } else if(options.has(REMOVE)) {
//            removePage(json, options);
//        } else if(options.has(SECTION)) {
//            updateSection(json, options);
//        }
    }

    /*private void updatePage(JSONObject json, JSONObject options) throws IOException {

        String section = options.getJSONObject(UPDATE).getString(SECTION);
        File file = new File(getWebDirectory(), "data/pages-" + section + ".json");
        SectionStructure sectionStructure = new SectionStructure().read(file);


        SectionStructure.Resource initial = null, update;
        if (options.has(INITIAL)) {
            initial = new SectionStructure.Resource().parse(options.getJSONObject(INITIAL));
        }
        update = new SectionStructure.Resource().parse(options.getJSONObject(UPDATE));

        if (!validate(json, initial, update)) return;

        File updateFile = update.getOptionsFile();
        SectionStructure sectionStructure;
        if (updateFile.exists()) {
            sectionStructure = new SectionStructure().read(updateFile);
            if (initial != null) {
                sectionStructure.remove(initial);
            }
        } else {
            sectionStructure = new SectionStructure().parse("[]");
        }
        sectionStructure.put(update);

        new WebPath(update.getResource()).save(update.content);
        Misc.log("Pages", "file updated:", update.getResource(), "[" + update.content.length() + " byte(s)]");

        new WebPath(updateFile).save(sectionStructure.toJSON().toString(2));
        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated page:", update.toJSON(), "[" + update.locale + "]");
    }

    private void removePage(JSONObject json, JSONObject options) throws IOException {
        SectionStructure.Resource initial = null, remove;
        remove = new SectionStructure.Resource().parse(options.getJSONObject(REMOVE));

        if (!validate(json, initial, remove)) return;

        File removeFile = remove.getOptionsFile();
        SectionStructure sectionStructure;
        if (removeFile.exists()) {
            sectionStructure = new SectionStructure().read(removeFile);
            sectionStructure.remove(remove);

            new WebPath(removeFile).save(sectionStructure.toJSON().toString(2));
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
        SectionStructure.Category category = new SectionStructure.Category().parse(options.getJSONObject(SECTION));
        WebPath structureFile = new WebPath(category.getOptionsFile());
        SectionStructure sectionStructure;
        if (structureFile.path().exists()) {
            sectionStructure = new SectionStructure().read(structureFile.path());
        } else {
            sectionStructure = new SectionStructure().parse("[]");
        }
        sectionStructure.put(category);
        structureFile.save(sectionStructure.toJSON().toString(2));

        json.put(STATUS, STATUS_SUCCESS);
        Misc.log("Pages", "has updated category:", category.toJSON());
    }

    private boolean validate(JSONObject json, SectionStructure.Resource initial, SectionStructure.Resource update) throws IOException {
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
*/

}
