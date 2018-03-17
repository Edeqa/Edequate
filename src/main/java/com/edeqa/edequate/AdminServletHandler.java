package com.edeqa.edequate;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.Locales;
import com.edeqa.edequate.rest.Nothing;
import com.edeqa.edequate.rest.Resource;
import com.edeqa.edequate.rest.Version;
import com.edeqa.edequate.rest.admin.Page;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable1;

import java.io.File;
import java.io.FilenameFilter;
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
        registerRest();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        registerRest();
        registerActionsPool();
    }

    private void registerRest() {
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(((Arguments) getSystemBus().getHolder(Arguments.TYPE)).getWebRootDirectory()).setChildDirectory("js/admin").setActionName("/rest/admin"));
        registerAction(new Page());
    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {

        Arguments arguments = (Arguments) getSystemBus().getHolder(Arguments.TYPE);

        if(requestWrapper.getRequestURI().getPath().startsWith("/admin/rest/")) {
            super.perform(requestWrapper);
        } else if(requestWrapper.getRequestURI().getPath().startsWith("/admin/")
                || requestWrapper.getRequestURI().getPath().startsWith("/admin")) {
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
        } else {
            URI uri = requestWrapper.getRequestURI();
            String host = requestWrapper.getRequestedHost();
            String redirectLink = "https://" + host + arguments.getWrappedHttpsPort() + uri.getPath();
            requestWrapper.sendRedirect(redirectLink);
        }
    }
}
