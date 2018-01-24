package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Mime;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class MimeTypeTest {

    private MimeType mimeType;

    @Before
    public void setUp() throws Exception {
        mimeType = new MimeType();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void fetchContentType() {
        assertEquals("application/unknown", mimeType.fetchContentType());
        mimeType.setType("html");
        mimeType.setMime(Mime.TEXT_HTML);
        mimeType.setCharset("CP-1251");
        mimeType.setText(true);
        assertEquals("text/html; charset=CP-1251", mimeType.fetchContentType());
    }

    @Test
    public void getType() {
        assertEquals("", mimeType.getType());

    }

    @Test
    public void setType() {
        mimeType.setType("html");
        assertEquals("html", mimeType.getType());
    }

    @Test
    public void getMime() {
        assertEquals("application/unknown", mimeType.getMime());
    }

    @Test
    public void setMime() {
        mimeType.setMime(Mime.TEXT_HTML);
        assertEquals("text/html", mimeType.getMime());
    }

    @Test
    public void isText() {
        assertEquals(false, mimeType.isText());
    }

    @Test
    public void setText() {
        assertEquals(false, mimeType.isText());
        mimeType.setText(true);
        assertEquals(true, mimeType.isText());
    }

    @Test
    public void isGzip() {
        assertEquals(false, mimeType.isGzip());
    }

    @Test
    public void setGzip() {
        assertEquals(false, mimeType.isGzip());
        mimeType.setGzip(true);
        assertEquals(true, mimeType.isGzip());
    }

    @Test
    public void getCharset() {
        assertEquals("UTF-8", mimeType.getCharset());
    }

    @Test
    public void setCharset() {
        assertEquals("UTF-8", mimeType.getCharset());
        mimeType.setCharset("CP-1251");
        assertEquals("CP-1251", mimeType.getCharset());
    }

    @Test
    public void isGzipEnabled() {
        assertEquals(false, MimeType.isGzipEnabled());
    }

    @Test
    public void setGzipEnabled() {
        assertEquals(false, MimeType.isGzipEnabled());
        MimeType.setGzipEnabled(true);
        assertEquals(true, MimeType.isGzipEnabled());
    }

    @Test
    public void getFullName() {
        assertEquals(null, mimeType.getFullName());
    }

    @Test
    public void setFullName() {
        assertEquals(null, mimeType.getFullName());
        mimeType.setFullName("manifest.json");
        assertEquals("manifest.json", mimeType.getFullName());
    }

    @Test
    public void constructorWithJson() {
        JSONObject json = new JSONObject();
        json.put("type", "css");
        json.put("mime", Mime.TEXT_CSS);
        json.put("text", true);

        mimeType = new MimeType(json.toString());
        System.out.println(mimeType.toString());

        json.put("name", "manifest.json");
        json.put("gzip", true);

        mimeType = new MimeType(json);
        System.out.println(mimeType.toString());
    }

}