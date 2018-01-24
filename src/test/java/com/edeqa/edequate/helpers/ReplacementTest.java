package com.edeqa.edequate.helpers;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class ReplacementTest {

    private Replacement replacement;

    @Before
    public void setUp() throws Exception {
        replacement = new Replacement();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getPattern() {
        assertEquals(null, replacement.getPattern());
        System.out.println(replacement.toString());
    }

    @Test
    public void setPattern() {
        replacement.setPattern("test");
        assertEquals("test", replacement.getPattern());
    }

    @Test
    public void getReplace() {
        assertEquals(null, replacement.getReplace());
    }

    @Test
    public void setReplace() {
        replacement.setReplace("test2");
        assertEquals("test2", replacement.getReplace());
    }
}