package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Mime;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class MimeTypesTest {

    MimeTypes mimeTypes;

    @Before
    public void setUp() throws Exception {
        mimeTypes = new MimeTypes();
        mimeTypes.useDefault();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void add() {
        assertEquals(13, mimeTypes.getTypes().size());
        mimeTypes.add(new MimeType().setType("test"));
        assertEquals(14, mimeTypes.getTypes().size());
    }

    @Test
    public void fetchMimeFor() {
        assertEquals(Mime.TEXT_HTML, mimeTypes.fetchMimeFor("index.html").getMime());
        assertEquals(Mime.APPLICATION_OCTET_STREAM, mimeTypes.fetchMimeFor("test").getMime());
        assertEquals(Mime.APPLICATION_X_WEB_APP_MANIFEST_JSON, mimeTypes.fetchMimeFor("manifest.json").getMime());
    }

    @Test
    public void getDefaultMime() {
        assertEquals(Mime.APPLICATION_OCTET_STREAM, mimeTypes.getDefaultMime().getMime());
    }

    @Test
    public void setDefaultMime() {
        assertEquals(Mime.APPLICATION_OCTET_STREAM, mimeTypes.getDefaultMime().getMime());
        mimeTypes.setDefaultMime(new MimeType().setType("test"));
        assertEquals(Mime.APPLICATION_UNKNOWN, mimeTypes.getDefaultMime().getMime());
        assertEquals("test", mimeTypes.getDefaultMime().getType());
    }

    @Test
    public void parse() {
        JSONObject json = new JSONObject();
        json.put("type", "test");
        json.put("mime", "Test");

        JSONArray array = new JSONArray();
        array.put(json);

        mimeTypes.parse(array);
        assertEquals("test", mimeTypes.fetchMimeFor("file.test").getMime());

    }

    @Test
    public void useDefault() {
        mimeTypes.useDefault();
        assertEquals(13, mimeTypes.getTypes().size());
    }
}