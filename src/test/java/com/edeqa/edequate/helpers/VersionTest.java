package com.edeqa.edequate.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionTest {

    private Version version = new Version();

    @Test
    public void getVersionCode() {
        assertEquals(7, Version.getVersionCode());
    }

    @Test
    public void getVersionName() {
        assertEquals("2.1", Version.getVersionName());
    }

    @Test
    public void getVersion() {
        assertEquals("2.1.7", Version.getVersion());
    }
}