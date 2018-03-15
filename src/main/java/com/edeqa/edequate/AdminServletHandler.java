package com.edeqa.edequate;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.admin.Page;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.google.common.net.HttpHeaders;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;


/**
 * Created 3/14/18.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends RestServletHandler {

    public AdminServletHandler(){
        super();

//        registerAction(new InitialData());
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
            new Content().setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true)).setWebPath(new WebPath(arguments.getWebRootDirectory(), "index-admin.html")).setResultCode(200).call(null, requestWrapper);
        } else {
            URI uri = requestWrapper.getRequestURI();
            String host;
            try {
                host = requestWrapper.getRequestHeaders().get(HttpHeaders.HOST).get(0);
                host = host.split(":")[0];
            } catch(Exception e){
                e.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }
            String redirectLink = "https://" + host + arguments.getWrappedHttpsPort() + uri.getPath();
            requestWrapper.sendRedirect(redirectLink);
        }
    }
}
