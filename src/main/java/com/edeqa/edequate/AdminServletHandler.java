package com.edeqa.edequate;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.admin.Admins;
import com.edeqa.edequate.rest.admin.LogsClear;
import com.edeqa.edequate.rest.admin.LogsLog;
import com.edeqa.edequate.rest.admin.Pages;
import com.edeqa.edequate.rest.admin.RestorePassword;
import com.edeqa.edequate.rest.admin.Splash;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;


/**
 * Created 3/14/18.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends RestServletHandler {

    public AdminServletHandler(){
        super();
        useDefault();
        registerActionsPool();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        useDefault();
    }

    @Override
    public void useDefault() {
        super.useDefault();
        registerAction(new Files().setFilenameFilter((dir, name) -> name.contains("Holder")).setWebDirectory(((Arguments) getSystemBus().getHolder(Arguments.TYPE)).getWebRootDirectory()).setChildDirectory("js/admin").setActionName("/rest/admin"));
        registerAction(new Pages());

        registerAction(new LogsClear());
        registerAction(new LogsLog());
        registerAction(new Admins().read());
        registerAction(new RestorePassword());
        registerAction(new Splash());
    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {

        Arguments arguments = (Arguments) getSystemBus().getHolder(Arguments.TYPE);

        URI uri = requestWrapper.getRequestURI();
        if(uri.getPath().startsWith("/admin/rest/") || uri.getPath().startsWith("/admin/restore/")) {
            super.perform(requestWrapper);
        } else if(uri.getPath().startsWith("/admin")) {
            WebPath webPath = new WebPath(arguments.getWebRootDirectory(), "index-admin.html");
            if(webPath.path().exists()) {
                new Content()
                        .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true))
                        .setWebPath(webPath)
                        .setResultCode(200)
                        .call(null, requestWrapper);
            } else {
                Misc.err("Admin", "doesn't found index-admin.html, redirecting to the main page");
                String host = requestWrapper.getRequestedHost();
                String redirectLink = "https://" + host + arguments.getWrappedHttpsPort() + "/404.html";
                requestWrapper.sendRedirect(redirectLink);
            }
        } else if(uri.getPath().equals("/home")) {
            String host = requestWrapper.getRequestedHost();
            String redirectLink = "https://" + host + arguments.getWrappedHttpsPort();
            requestWrapper.sendRedirect(redirectLink);
        } else {
            String host = requestWrapper.getRequestedHost();
            String redirectLink = "https://" + host + arguments.getWrappedHttpsPort() + uri.getPath();
            requestWrapper.sendRedirect(redirectLink);
        }
    }
}
