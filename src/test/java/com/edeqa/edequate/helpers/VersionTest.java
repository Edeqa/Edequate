package com.edeqa.edequate.helpers;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionTest {

    private Version version = new Version();

    @Test
    public void getVersionCode() {
        assertEquals(5, Version.getVersionCode());
    }

    @Test
    public void getVersionName() {
        assertEquals("2.0", Version.getVersionName());
    }

    @Test
    public void getVersion() {
        assertEquals("2.0.5", Version.getVersion());
    }
}