package com.edeqa.edequate.rest;

import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.edeqa.edequate.abstracts.AbstractAction.CODE;
import static com.edeqa.edequate.abstracts.AbstractAction.EXTRA;
import static com.edeqa.edequate.abstracts.AbstractAction.MESSAGE;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS_SUCCESS;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.JVM)
public class VersionTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getType() {
        assertEquals("/rest/version", new Version().getType());
    }

    @Test
    public void call() {
        JSONObject json = new JSONObject();
        new Version().call(json, new RequestWrapper());
        assertEquals(STATUS_SUCCESS, json.get(STATUS));
        assertEquals(6, json.get(CODE));
        assertEquals("2.0", json.get(MESSAGE));
        assertEquals("2.0.6", json.get(EXTRA));
    }
}