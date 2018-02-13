package com.edeqa.edequate.rest;

import com.edeqa.edequate.abstracts.FileRestAction;
import com.edeqa.edequate.helpers.Replacements;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Content extends FileRestAction {

    private WebPath webPath;
    private String content;
    private MimeType mimeType;
    private Replacements replacements;
    private int resultCode;

    public Content() {
        super();
    }

    @Override
    public void call(JSONObject json, RequestWrapper requestWrapper) {
        try {
            MimeType.setGzipEnabled(false);
            for (String s : requestWrapper.getRequestHeader(HttpHeaders.ACCEPT_ENCODING)) {
                if(s.toLowerCase().contains("gzip")) {
                    MimeType.setGzipEnabled(true);
                    break;
                }
            }

            if(getMimeType() == null) {
                setMimeType(new MimeType().setMime(Mime.APPLICATION_UNKNOWN));
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

            if(getWebPath() != null) {
                String lastModified = dateFormat.format(getWebPath().path().lastModified());
                requestWrapper.setHeader(HttpHeaders.LAST_MODIFIED, lastModified);
            }
//            requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, OPTIONS.isDebugMode() ? "max-age=10" : "max-age=60");
            requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=10");

            if(getWebPath() != null) {
                String etag = "W/1976-" + Math.abs(("" + getWebPath().path().lastModified()).hashCode());
                requestWrapper.setHeader(HttpHeaders.ETAG, etag);
            }
            requestWrapper.setHeader(HttpHeaders.SERVER, "Edequate/" + Version.getVersion());
            requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

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

            requestWrapper.setGzip(getMimeType().isGzip());

            if (getMimeType().isText()) {
//                if(!mimeType.getMime().matches(";\\s*charset\\s*=")) {
//                    type += "; charset=UTF-8";
//                }

                String string;
                Charset charset = StandardCharsets.ISO_8859_1;
                if(getWebPath() != null) {
                    FileReader reader = new FileReader(getWebPath().path());
                    int c;
                    StringBuilder fileContent = new StringBuilder();
                    while ((c = reader.read()) != -1) {
                        fileContent.append((char) c);
                    }
                    reader.close();

                    byte[] bytes = fileContent.toString().getBytes(); //Files.readAllBytes(file.toPath());
                    if (bytes[0] == -1 && bytes[1] == -2) charset = StandardCharsets.UTF_16;
                    else if (bytes[0] == -2 && bytes[1] == -1) charset = StandardCharsets.UTF_16;
                    string = new String(bytes, charset);
                } else {
                    string = getContent();
                }

                if(getReplacements() != null) string = getReplacements().process(string, getMimeType());
                requestWrapper.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, getMimeType().fetchContentType());
                if (!getMimeType().isGzip())
                    requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(string.length()));
                requestWrapper.sendResponseHeaders(getResultCode(), 0);

                OutputStream os = requestWrapper.getResponseBody();
                os.write(string.getBytes(charset));
                os.close();
            } else {
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, getMimeType().fetchContentType());
                if (!getMimeType().isGzip())
                    requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(webPath.path().length()));
                requestWrapper.sendResponseHeaders(getResultCode(), 0);
                OutputStream os = requestWrapper.getResponseBody();

                if(getWebPath() != null) {
                    FileInputStream fs = new FileInputStream(getWebPath().path());
                    final byte[] buffer = new byte[0x10000];
                    int count;
                    while ((count = fs.read(buffer)) >= 0) {
                        os.write(buffer, 0, count);
                    }
                    fs.close();
                    os.close();
                } else {
                    InputStream is = new ByteArrayInputStream(getContent().getBytes());
                    final byte[] buffer = new byte[0x10000];
                    int count;
                    while ((count = is.read(buffer)) >= 0) {
                        os.write(buffer, 0, count);
                    }
                    is.close();
                    os.close();
                }
            }
        } catch(Exception e) {
            Misc.err("Content", "failed for", webPath, requestWrapper.getRequestURI(), e);
        }
        clear();
    }

    public void clear() {
        setWebPath(null);
        setMimeType(null);
        setResultCode(0);
        setContent(null);
    }

    private WebPath getWebPath() {
        return webPath;
    }

    public Content setWebPath(WebPath webPath) {
        this.webPath = webPath;
        return this;
    }

    private MimeType getMimeType() {
        return mimeType;
    }

    public Content setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public Content setReplacements(Replacements replacements) {
        this.replacements = replacements;
        return this;
    }

    private Replacements getReplacements() {
        return replacements;
    }

    public Content setResultCode(int resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    private int getResultCode() {
        return resultCode;
    }

    public String getContent() {
        return content;
    }

    public Content setContent(String content) {
        this.content = content;
        return this;
    }
}


