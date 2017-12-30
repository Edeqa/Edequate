package com.edeqa.edequate;


import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;

import java.io.IOException;

import static com.edeqa.helpers.HtmlGenerator.A;
import static com.edeqa.helpers.HtmlGenerator.CLASS;
import static com.edeqa.helpers.HtmlGenerator.DIV;
import static com.edeqa.helpers.HtmlGenerator.HEIGHT;
import static com.edeqa.helpers.HtmlGenerator.HREF;
import static com.edeqa.helpers.HtmlGenerator.IMG;
import static com.edeqa.helpers.HtmlGenerator.LINK;
import static com.edeqa.helpers.HtmlGenerator.NOSCRIPT;
import static com.edeqa.helpers.HtmlGenerator.REL;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.STYLESHEET;
import static com.edeqa.helpers.HtmlGenerator.TYPE;
import static com.edeqa.helpers.HtmlGenerator.WIDTH;

public class MainServletHandler extends AbstractServletHandler {

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {

        Misc.log("Main", requestWrapper.getRequestURI());

        HtmlGenerator html = new HtmlGenerator();

        html.getHead().add(HtmlGenerator.TITLE).with("Edeqa");
        html.getHead().add(HtmlGenerator.LINK).with(HtmlGenerator.REL, "icon").with(HtmlGenerator.HREF, "/icons/favicon.ico");
        html.getHead().add(HtmlGenerator.STYLE).with("@import url('/css/edequate.css');@import url('/css/edequate-horizontal.css');@import url('/css/edeqa-colors.css');");
        html.getHead().add(HtmlGenerator.META).with(HtmlGenerator.NAME, "viewport").with(HtmlGenerator.CONTENT, "width=device-width, initial-scale=1, maximum-scale=5, user-scalable=no");
        html.getHead().add(HtmlGenerator.SCRIPT).with(HtmlGenerator.ASYNC, true).with(HtmlGenerator.SRC, "/js/Edequate.js").with("variable", "u").with("callback", "u.require('/js/main/Main', u).then(function(main){main.start()})").with("exportConstants","true");

        HtmlGenerator.Tag a = html.getBody().add(HtmlGenerator.DIV).with(HtmlGenerator.ID, "loading-dialog").with(HtmlGenerator.CLASS, "modal shadow progress-dialog").with(HtmlGenerator.TABINDEX, -1)
                .add(HtmlGenerator.DIV).with(HtmlGenerator.CLASS, "dialog-items");
        a.add(HtmlGenerator.DIV).with(HtmlGenerator.CLASS, "dialog-item progress-dialog-circle");
        a.add(HtmlGenerator.DIV).with(HtmlGenerator.CLASS, "dialog-item progress-dialog-title").with("Loading...");
        a.add(HtmlGenerator.DIV).with(HtmlGenerator.ID, "loading-dialog-progress").with(HtmlGenerator.CLASS, "dialog-item progress-dialog-title");

        html.getBody().add(HtmlGenerator.DIV);
        html.getBody().add(HtmlGenerator.DIV);
        html.getBody().add(HtmlGenerator.DIV);
        html.getBody().add(HtmlGenerator.NOSCRIPT);

        HtmlGenerator.Tag noscript = html.getBody().add(NOSCRIPT);
        noscript.add(LINK).with(TYPE, Mime.TEXT_CSS).with(REL, STYLESHEET).with(HREF, "/css/noscript.css");

        HtmlGenerator.Tag header = noscript.add(DIV).with(CLASS, "header").with("Edeqa");
        header.add(IMG).with(SRC, "/images/edeqa-logo.svg").with(WIDTH, 24).with(HEIGHT, 24);
        header.with(" Edeqa");

        noscript.add(DIV).with(CLASS, "text").with("This service requires to allow Javascript. Please enable Javascript in your browser or use other browser that supports Javascript and try again.");
        noscript.add(DIV).with(CLASS, "copyright").with("Edequate &copy;2017-18 ").add(A).with(CLASS, "link").with(HREF, "http://edequate.edeqa.com").with("Edeqa");

        requestWrapper.sendResult(200, Mime.TEXT_HTML, html.build().getBytes());

    }
}
// [END example]