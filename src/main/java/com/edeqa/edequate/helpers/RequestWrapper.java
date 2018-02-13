package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created 6/9/2017.
 */

@SuppressWarnings("unused")
public class RequestWrapper {

    protected final static int MODE_SERVLET = 0;
    protected final static int MODE_EXCHANGE = 1;

    protected final static long MAX_BODY_LENGTH = 1024 * 1024;

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private String charset;

    private HttpExchange httpExchange;

    private int mode;
    private boolean gzip;

    @Override
    public String toString() {
        return "RequestWrapper{}";
    }

    public RequestWrapper() {
        this.gzip = false;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        setMode(MODE_SERVLET);
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
        setMode(MODE_SERVLET);
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        setMode(MODE_EXCHANGE);
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public URI getRequestURI() {
        if(mode == MODE_SERVLET) {
            try {
                return new URI(httpServletRequest.getRequestURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestURI();
        }
        return null;
    }

    public void setHeader(String name, String value) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.setHeader(name, value);
        } else if(mode == MODE_EXCHANGE) {
            httpExchange.getResponseHeaders().set(name, value);
        }
    }

    public void addHeader(String name, String value) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.addHeader(name, value);
        } else if(mode == MODE_EXCHANGE) {
            httpExchange.getResponseHeaders().add(name, value);
        }
    }

    public void sendResponseHeaders(int code, int arg1) {
        if(mode == MODE_SERVLET) {
            if(charset != null) {
                String contentType = httpServletResponse.getHeader(HttpHeaders.CONTENT_TYPE);
                if (!contentType.toLowerCase().contains("; charset=")) {
                    contentType = contentType + "; charset=" + charset;
                    httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
                }
            }
            httpServletResponse.setStatus(code);
        } else if(mode == MODE_EXCHANGE) {
            if(isGzip()) {
                setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            }

            if(charset != null) {
                List<String> contentTypes = httpExchange.getResponseHeaders().get(HttpHeaders.CONTENT_TYPE);
                for (String contentType : contentTypes) {
                    if (!contentType.toLowerCase().contains("; charset=")) {
                        contentType = contentType + "; charset=" + charset;
                        httpExchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, contentType);
                    }
                }
            }

            try {
                httpExchange.sendResponseHeaders(code, arg1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRedirect(String redirectLink) throws IOException {
        if(mode == MODE_SERVLET) {
            setHeader(HttpHeaders.SERVER, "Edequate/" + Version.getVersion());
            httpServletResponse.sendRedirect(redirectLink);
        } else if(mode == MODE_EXCHANGE) {
            setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            setHeader(HttpHeaders.DATE, new Date().toString());
            setHeader(HttpHeaders.LOCATION, redirectLink);
            setHeader(HttpHeaders.SERVER, "Edequate/" + Version.getVersion());
            httpExchange.sendResponseHeaders(302, 0);
            httpExchange.close();
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletResponse.getOutputStream();
        } else if(mode == MODE_EXCHANGE) {
            if(isGzip()) {
                return new BufferedOutputStream(new GZIPOutputStream(httpExchange.getResponseBody()));
            } else {
                return httpExchange.getResponseBody();
            }
        }
        return null;
    }

    public OutputStream getResponseBody() throws IOException {
        return getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletRequest.getInputStream();
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestBody();
        }
        return null;
    }

    public InputStream getRequestBody() throws IOException {
        return getInputStream();
    }

    public void setCharacterEncoding(String charset) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.setCharacterEncoding(charset);
        } else if(mode == MODE_EXCHANGE) {
            this.charset = charset;
        }
    }

    public PrintWriter getPrintWriter() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletResponse.getWriter();
        } else if(mode == MODE_EXCHANGE) {
            return new PrintWriter(httpExchange.getResponseBody());
        }
        return null;
    }

    public InetSocketAddress getRemoteAddress() {
        if(mode == MODE_SERVLET) {
            return new InetSocketAddress(httpServletRequest.getRemoteAddr(), httpServletRequest.getRemotePort());
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRemoteAddress();
        }
        return null;
    }

    public Map<String, List<String>> getRequestHeaders() {
        if(mode == MODE_SERVLET) {
//            Headers implements Map<String, List<String>> {
            Map<String, List<String>> headers = new HashMap<>();
            String x;
            Enumeration<String> names = httpServletRequest.getHeaderNames();
            while(names.hasMoreElements()) {
                x = names.nextElement();
                Enumeration<String> h = httpServletRequest.getHeaders(x);
                headers.put(x, Collections.list(h) );
            }
            return headers;
        } else if(mode == MODE_EXCHANGE) {
            Map<String, List<String>> headers = new HashMap<>();
            Map.Entry<String, List<String>> entry;

            Iterator<Map.Entry<String, List<String>>> iter = httpExchange.getRequestHeaders().entrySet().iterator();
            while(iter.hasNext()) {
                entry = iter.next();
                headers.put(entry.getKey(), entry.getValue() );
            }
            return headers;
        }
        return null;
    }

    public List<String> getRequestHeader(String name) {
        if(mode == MODE_SERVLET) {
            return Collections.list(httpServletRequest.getHeaders(name));
        } else if(mode == MODE_EXCHANGE) {
            Headers headers = httpExchange.getRequestHeaders();
            if(headers.containsKey(name)) {
                return httpExchange.getRequestHeaders().get(name);
            } else {
                return Collections.emptyList();
            }
        }
        return null;
    }

    public String getRequestedHost() {
        String host = null;
        if(mode == MODE_SERVLET) {
            Thread.dumpStack();
            return null;
        } else if(mode == MODE_EXCHANGE) {
            host = httpExchange.getRequestHeaders().getFirst(HttpHeaders.HOST);
            if(host == null) {
                host = httpExchange.getLocalAddress().getHostName();
            }
            if(host == null) {
                try {
                    host = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            if(host != null) {
                host = host.split(":")[0];
            }
        }
        return host;
    }

    public String getRequestMethod() {
        if(mode == MODE_SERVLET) {
            return httpServletRequest.getMethod();
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestMethod();
        }
        return null;
    }

    public void sendResult(JSONObject json) {
        sendResult(200, Mime.APPLICATION_JSON, json.toString().getBytes());
    }

    public void sendResult(String string) {
        sendResult(200, Mime.TEXT_PLAIN, string.getBytes());
    }

    public void sendError(Integer code, JSONObject json) {
        sendResult(code, Mime.APPLICATION_JSON, json.toString().getBytes());
    }

    public void sendError(Integer code, String string) {
        sendResult(code, Mime.TEXT_PLAIN, string.getBytes());
    }

    public void sendResult(Integer code, String contentType, byte[] bytes) {
        try {
            setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

            // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
            setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
            setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "frame-ancestors 'self'");
            setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
            setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
            setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=63072000; includeSubDomains; preload");
            setHeader(HttpHeaders.VARY, "Accept-Encoding");

            addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            if(contentType != null) setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            setHeader(HttpHeaders.SERVER, "Edequate/" + Version.getVersion());
            setHeader(HttpHeaders.DATE, new Date().toString());

            sendResponseHeaders(code, bytes.length);

            OutputStream os = getResponseBody();
            os.write(bytes);
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getMethod() {
        return getRequestMethod();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public String getBody() {
        String body = null;
        try {
            InputStreamReader isr = new InputStreamReader(getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            body = br.readLine();
            br.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return body;
    }

    public void processBody(Runnable1<StringBuilder> callback, Runnable1<Exception> fallback) {

        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = this.getRequestBody();
            int b;
            long count = 0;
            while ((b = is.read()) != -1) {
                if(count++ > MAX_BODY_LENGTH) {
                    fallback.call(new IllegalArgumentException("Body size is bigger than " + MAX_BODY_LENGTH + " byte(s)."));
                    return;
                }
                buf.append((char) b);
            }
            is.close();

            if(buf.length() > 0) {
                callback.call(buf);
            } else {
                fallback.call(new IllegalArgumentException("Empty body"));
            }

        } catch(Exception e) {
            e.printStackTrace();
            if(fallback != null) fallback.call(e);
        }

    }

    public Map<String,List<String>> getParameterMap() {
        try {
            if (mode == MODE_SERVLET) {
                HashMap<String, List<String>> map = new HashMap<>();
                for (Map.Entry<String, String[]> x : httpServletRequest.getParameterMap().entrySet()) {
                    map.put(x.getKey(), Arrays.asList(x.getValue()));
                }
                return map;
            } else if (mode == MODE_EXCHANGE) {
                HashMap<String, List<String>> map = new HashMap<>();

                List<String> list;
                String query = httpExchange.getRequestURI().getQuery();
                if (!Misc.isEmpty(query)) {
                    String[] queryParts = httpExchange.getRequestURI().getQuery().split("&");
                    for (String x : queryParts) {
                        String[] arguments = x.split("=");
                        if (map.containsKey(arguments[0])) {
                            list = map.get(arguments[0]);
                        } else {
                            list = new ArrayList<>();
                            map.put(arguments[0], list);
                        }
                        if (arguments.length > 1) list.add(arguments[1]);
                    }
                }
                return map;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}
