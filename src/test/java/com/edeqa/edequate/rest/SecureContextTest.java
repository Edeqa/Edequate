package com.edeqa.edequate.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;


@FixMethodOrder(MethodSorters.JVM)
public class SecureContextTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getType() {
        assertEquals("/rest/secure/context", new SecureContext().getType());
    }

    @Test
    public void call() {
    }

    @Test
    public void getHttpsConfigurator() {
    }

    @Test
    public void setSslContext() {
    }

    @Test
    public void getSslContext() {
    }
}