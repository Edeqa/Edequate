package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;

import org.json.JSONObject;

import static com.edeqa.helpers.HtmlGenerator.A;
import static com.edeqa.helpers.HtmlGenerator.BUTTON;
import static com.edeqa.helpers.HtmlGenerator.CLASS;
import static com.edeqa.helpers.HtmlGenerator.DIV;
import static com.edeqa.helpers.HtmlGenerator.HEIGHT;
import static com.edeqa.helpers.HtmlGenerator.HREF;
import static com.edeqa.helpers.HtmlGenerator.ID;
import static com.edeqa.helpers.HtmlGenerator.IMG;
import static com.edeqa.helpers.HtmlGenerator.LINK;
import static com.edeqa.helpers.HtmlGenerator.NOSCRIPT;
import static com.edeqa.helpers.HtmlGenerator.ONCLICK;
import static com.edeqa.helpers.HtmlGenerator.REL;
import static com.edeqa.helpers.HtmlGenerator.SPAN;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.STYLESHEET;
import static com.edeqa.helpers.HtmlGenerator.WIDTH;


@SuppressWarnings("unused")
public class Splash extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/splash";
    private String info;
    private String script;
    private boolean buttons;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_HTML);
        json.put(MESSAGE, fetchSplash().build());
    }

    public HtmlGenerator fetchSplash() {

        //noinspection unchecked
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);

        HtmlGenerator html = new HtmlGenerator();

        html.getHead().add(HtmlGenerator.TITLE).with(arguments.getAppName() + " Admin");
        html.getHead().add(HtmlGenerator.LINK).with(HtmlGenerator.REL, "icon").with(HtmlGenerator.HREF, "/icons/favicon.ico");
        html.getHead().add(HtmlGenerator.STYLE).with("@import url('/css/edequate.css');@import url('/css/edequate-admin.css');");
        html.getHead().add(HtmlGenerator.META).with(HtmlGenerator.NAME, "viewport").with(HtmlGenerator.CONTENT, "width=device-width, initial-scale=1, maximum-scale=5, user-scalable=no");
        if(getScript() != null) {
            html.getHead().add(HtmlGenerator.SCRIPT).with(HtmlGenerator.ASYNC, true).with(HtmlGenerator.SRC, "/js/Edequate.js").with("data-variable", "u").with("data-callback", "u.require('" + getScript() + "', u).then(function(script){script.start()})").with("data-export-constants", true);
        } else {
            html.getHead().add(HtmlGenerator.SCRIPT).with(HtmlGenerator.ASYNC, true).with(HtmlGenerator.SRC, "/js/Edequate.js").with("data-variable", "u");
        }

        HtmlGenerator.Tag body = html.getBody().add(DIV).with(ID, "loading-dialog").with(CLASS, "admin-splash-layout");
        body.add(IMG).with(CLASS, "admin-splash-logo").with(SRC, "/images/logo.svg");
        body.add(DIV).with(CLASS, "admin-splash-title").with(arguments.getAppName() + " " + arguments.getVersion());

        body.add(DIV).with(CLASS, "admin-splash-subtitle").with("Admin");
        HtmlGenerator.Tag info = body.add(DIV).with(CLASS, "admin-splash-info");
        if(getInfo() != null) {
            info.with(getInfo());
        }

        HtmlGenerator.Tag buttons = body.add(DIV);

        buttons.add(BUTTON).with("Home").with(CLASS, "dialog-button").with(ONCLICK, "window.location = '/home';");
        buttons.add(BUTTON).with("Login").with(CLASS, "dialog-button").with(ONCLICK, "u.clear(this.parentNode);window.location.reload();");
        buttons.add(BUTTON).with("Forgot password").with(CLASS, "dialog-button").with(ONCLICK, "window.location = '" + RestorePassword.TYPE + "';");

        if(isButtons()) {
            buttons.with(CLASS, "admin-splash-buttons");
        } else {
            buttons.with(CLASS, "admin-splash-buttons hidden");
        }

        HtmlGenerator.Tag based = body.add(DIV).with(CLASS, "admin-splash-copyright");
        based.add(SPAN).with("Based on ");
        based.add(A).with("Edequate " + Version.getVersion()).with(CLASS, "link").with(HREF, "http://www.edeqa.com/edequate");
        based.add(SPAN).with(" &copy;2017-18 ");
        based.add(A).with("Edeqa").with(CLASS, "link").with(HREF, "http://www.edeqa.com");


        HtmlGenerator.Tag noscript = html.getBody().add(NOSCRIPT);
        noscript.add(LINK).with(TYPE, Mime.TEXT_CSS).with(REL, STYLESHEET).with(HREF, "/css/noscript.css");

        HtmlGenerator.Tag header = noscript.add(DIV).with(CLASS, "header");
        header.add(IMG).with(SRC, "/images/edeqa-logo.svg").with(WIDTH, 24).with(HEIGHT, 24);
        header.with(arguments.getAppName());

        noscript.add(DIV).with(CLASS, "text").with("This site requires to allow Javascript. Please enable Javascript in your browser and try again or use other browser that supports Javascript.");

        HtmlGenerator.Tag copyright = noscript.add(DIV).with(CLASS, "copyright");
        copyright.add(A).with("Edequate").with(CLASS, "link").with(HREF, "http://www.edeqa.com/edequate");
        copyright.add(SPAN).with(" &copy;2017-18 ");
        copyright.add(A).with("Edeqa").with(CLASS, "link").with(HREF, "http://www.edeqa.com");

        setInfo(null);
        setScript(null);
        setButtons(false);

        return html;
    }

    public Splash setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public String getScript() {
        return script;
    }

    public Splash setScript(String script) {
        this.script = script;
        return this;
    }

    public boolean isButtons() {
        return buttons;
    }

    public Splash setButtons(boolean buttons) {
        this.buttons = buttons;
        return this;
    }
}
