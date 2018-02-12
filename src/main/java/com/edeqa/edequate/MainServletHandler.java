package com.edeqa.edequate;


import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.Replacements;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.MimeTypes;
import com.edeqa.helpers.Misc;
import com.google.common.net.HttpHeaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.edeqa.helpers.HtmlGenerator.A;
import static com.edeqa.helpers.HtmlGenerator.CLASS;
import static com.edeqa.helpers.HtmlGenerator.DIV;
import static com.edeqa.helpers.HtmlGenerator.HEIGHT;
import static com.edeqa.helpers.HtmlGenerator.HREF;
import static com.edeqa.helpers.HtmlGenerator.IMG;
import static com.edeqa.helpers.HtmlGenerator.LINK;
import static com.edeqa.helpers.HtmlGenerator.NOSCRIPT;
import static com.edeqa.helpers.HtmlGenerator.REL;
import static com.edeqa.helpers.HtmlGenerator.SPAN;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.STYLESHEET;
import static com.edeqa.helpers.HtmlGenerator.TYPE;
import static com.edeqa.helpers.HtmlGenerator.WIDTH;

public class MainServletHandler extends AbstractServletHandler {

    private MimeTypes mimeTypes;
    private Replacements replacements;

    public MainServletHandler() {
        setMimeTypes(new MimeTypes().useDefault());
        setReplacements(new Replacements());
    }

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {

        URI uri = requestWrapper.getRequestURI();

        if ("/_ah/start".equals(uri.getPath())) {
            requestWrapper.sendResponseHeaders(200,0);
            requestWrapper.getOutputStream().close();
            return;
        } else if("/_ah/stop".equals(uri.getPath())) {
            requestWrapper.sendResponseHeaders(200,0);
            requestWrapper.getOutputStream().close();
            return;
        }

        WebPath webPath = new WebPath(getWebDirectory(), uri.getPath());
        if(!webPath.path().exists()) {
            String beginWeb = webPath.web(1);
            if("/main".startsWith(beginWeb) || "/rest".startsWith(beginWeb) || "/admin".startsWith(beginWeb)) {
                webPath = new WebPath(getWebDirectory(), "index.html");
                if (!webPath.path().exists()) {
                    requestWrapper.sendResult(200, Mime.TEXT_HTML, fetchDefaultIndex().build().getBytes());
                    return;
                }
            } else {
                requestWrapper.sendError(404, "File not found");
                return;
            }
        }

        String ipRemote = requestWrapper.getRemoteAddress().getAddress().getHostAddress();

        String host, referer = null;
        try {
            host = requestWrapper.getRequestHeaders().get(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
            referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
        } catch(Exception e){
            host = InetAddress.getLocalHost().getHostAddress();
        }
        try {
            referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
            if(referer.contains(host)) referer = null;
        } catch(Exception e){
        }

        String etag = "W/1976-" + ("" + webPath.path().lastModified()).hashCode();

        File root = new File(getWebDirectory());
//        String path = uri.getPath().toLowerCase();
        int resultCode = 200;
        if (!webPath.path().getCanonicalPath().startsWith(root.getCanonicalPath())) {
            // Suspected path traversal attack: reject with 403 error.
            Misc.log("Main", "[" + ipRemote + "]", uri.getPath(), "[403 - suspected path traversal attack]", (referer != null ? "referer: " + referer : ""));
            resultCode = 403;
            webPath = new WebPath(getWebDirectory(), "403.html");
//                Utils.sendResult.onEvent(exchange, 403, Constants.MIME.TEXT_PLAIN, "403 Forbidden\n".getBytes());
        } else if (webPath.path().isDirectory()) {
            webPath = webPath.webPath("index.html");
            Misc.log("Main", "[" + ipRemote + "]", "->", webPath.web(), "[" + (webPath.path().exists() ? webPath.path().length() + " byte(s)" : "not found") + "]", (referer != null ? "referer: " + referer : ""));
//            } else if (etag.equals(ifModifiedSince)) {
//                resultCode = 304;
//                file = new File(root + "/304.html");
//                Utils.sendResult.onEvent(exchange, 304, null, "304 Not Modified\n".getBytes());
//        } else if (!uri.getPath().endsWith("/") && !webPath.path().exists()) {
//            Misc.log("Main", "-> " + uri.getPath() + "/" + (referer != null ? ", referer: " + referer : ""));
//            requestWrapper.sendRedirect(uri.getPath() + "/");
//            return;
        } else if (!webPath.path().isFile() || uri.getPath().startsWith("/WEB-INF") || uri.getPath().startsWith("/META-INF") || uri.getPath().startsWith("/.")) {
            // Object does not exist or is not a file: reject with 404 error.

            boolean found = false;
//            String[] parts = path.split("/");
//            if(parts.length > 1) {
//                for (int i = 0; i < OPTIONS.getPages().length(); i++) {
//                    if(parts[1].equals(OPTIONS.getPages().get(i))) {
//                        resultCode = 200;
//                        Misc.log("Main", uri.getPath(), "[200 - page found]" + (referer != null ? ", referer: " + referer : ""));
//                        file = new File(OPTIONS.getWebRootDirectory() + "/index.html");
//                        found = true;
//                        break;
//                    }
//                }
//            }
            if(!found) {
                resultCode = 404;
                Misc.log("Main", "[" + ipRemote + "]", uri.getPath(), "[404 - not found]", (referer != null ? "referer: " + referer : ""));
                webPath = new WebPath(getWebDirectory(),"404.html");
            }
        } else {
            Misc.log("Main", "[" + ipRemote + "]", uri.getPath(), "[" + webPath.path().length() + " byte(s)]",(referer != null ? "referer: " + referer : ""));
        }

        {
            // Object exists and it is a file: accept with response code 200.
            MimeType.setGzipEnabled(false);
            for (String s : requestWrapper.getRequestHeader(HttpHeaders.ACCEPT_ENCODING)) {
                if(s.toLowerCase().contains("gzip")) {
                    MimeType.setGzipEnabled(true);
                    break;
                }
            }

            MimeType mimeType = getMimeTypes().fetchMimeFor(webPath.path().getName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            String lastModified = dateFormat.format(webPath.path().lastModified());

            requestWrapper.setHeader(HttpHeaders.LAST_MODIFIED, lastModified);
//            requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, OPTIONS.isDebugMode() ? "max-age=10" : "max-age=60");
            requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=10");
            requestWrapper.setHeader(HttpHeaders.ETAG, etag);
            requestWrapper.setHeader(HttpHeaders.SERVER, "Edequate/" + Version.getVersion());
            requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

            // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
            requestWrapper.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
            requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "frame-ancestors 'self'");
            requestWrapper.setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
            requestWrapper.setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
            requestWrapper.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=63072000; includeSubDomains; preload");
//                requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'unsafe-inline' 'unsafe-eval' https: 'nonce-waytous' 'strict-dynamic' report-uri /violation");
//        requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.gstatic.com https://www.googletagmanager.com https://cdnjs.cloudflare.com https://www.google-analytics.com https://connect.facebook.net https://platform.twitter.com https://maps.googleapis.com https://apis.google.com");

            // FIXME http://nibbler.silktide.com/en_US/reports/waytous.net
            // FIXME https://gtmetrix.com/reports/waytous.net/6i4B5kR2
            requestWrapper.setHeader(HttpHeaders.VARY, "Accept-Encoding");

            requestWrapper.setGzip(mimeType.isGzip());

            if (mimeType.isText()) {
//                if(!mimeType.getMime().matches(";\\s*charset\\s*=")) {
//                    type += "; charset=UTF-8";
//                }

                FileReader reader = new FileReader(webPath.path());
                int c;
                StringBuilder fileContent = new StringBuilder();
                while ((c = reader.read()) != -1) {
                    fileContent.append((char) c);
                }
                reader.close();

                byte[] bytes = fileContent.toString().getBytes(); //Files.readAllBytes(file.toPath());
                Charset charset = StandardCharsets.ISO_8859_1;
                if(bytes[0] == -1 && bytes[1] == -2) charset = StandardCharsets.UTF_16;
                else if(bytes[0] == -2 && bytes[1] == -1) charset = StandardCharsets.UTF_16;


                String string = new String(bytes, charset);
                string = getReplacements().process(string, mimeType);

                requestWrapper.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, mimeType.fetchContentType());
                if(!mimeType.isGzip()) requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(string.length()));
                requestWrapper.sendResponseHeaders(resultCode, 0);

                try {
                    OutputStream os = requestWrapper.getResponseBody();
                    os.write(string.getBytes(charset));
                    os.close();
                } catch(Exception e){
                    System.out.println("C:"+requestWrapper.getRequestURI());
                    e.printStackTrace();
                }
            } else {
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, mimeType.fetchContentType());
                if(!mimeType.isGzip()) requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(webPath.path().length()));
                requestWrapper.sendResponseHeaders(resultCode, 0);
                OutputStream os = requestWrapper.getResponseBody();

                FileInputStream fs = new FileInputStream(webPath.path());
                final byte[] buffer = new byte[0x10000];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
            }

        }

    }

    private HtmlGenerator fetchDefaultIndex() {
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

        HtmlGenerator.Tag noscript = html.getBody().add(NOSCRIPT);
        noscript.add(LINK).with(TYPE, Mime.TEXT_CSS).with(REL, STYLESHEET).with(HREF, "/css/noscript.css");

        HtmlGenerator.Tag header = noscript.add(DIV).with(CLASS, "header");
        header.add(IMG).with(SRC, "/images/edeqa-logo.svg").with(WIDTH, 24).with(HEIGHT, 24);
        header.with("Edequate Example");

        noscript.add(DIV).with(CLASS, "text").with("This site requires to allow Javascript. Please enable Javascript in your browser and try again or use other browser that supports Javascript.");

        HtmlGenerator.Tag copyright = noscript.add(DIV).with(CLASS, "copyright");
        copyright.add(A).with("Edequate").with(CLASS, "link").with(HREF, "http://www.edeqa.com/edequate");
        copyright.add(SPAN).with(" &copy;2017-18 ");
        copyright.add(A).with("Edeqa").with(CLASS, "link").with(HREF, "http://www.edeqa.com");

        return html;
    }

    public MimeTypes getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public Replacements getReplacements() {
        return replacements;
    }

    public void setReplacements(Replacements replacements) {
        this.replacements = replacements;
    }
}