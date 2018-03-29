package com.edeqa.edequate.helpers;


import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.admin.Admins;
import com.edeqa.edequate.rest.admin.RestorePassword;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;
import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;
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
import static com.edeqa.helpers.HtmlGenerator.TYPE;
import static com.edeqa.helpers.HtmlGenerator.WIDTH;

/*
 * Created 5/16/2017.
 */
@SuppressWarnings("restriction")
public class DigestAuthenticator extends Authenticator {

    public static final byte COL = ':';

    private final Set<String> givenNonces = new HashSet<>();
    private final SecureRandom random = new SecureRandom();
    private final String realm;
    private final ExpiringHashMap<String,Object> timestamps = new ExpiringHashMap<String,Object>();//.setTimeout(1000 * 60 * 60L);

    public DigestAuthenticator(String realm) {
        this.realm = realm;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        try {
            DigestContext context = getOrCreateContext(httpExchange);
            String authorization = httpExchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorization == null) {
//                Misc.log("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "Login request", "[" + httpExchange.getRequestURI() + "]");
                return fetchHeaders(httpExchange);
            }
            if (authorization.startsWith("Basic ")) {
                return fetchHeaders(httpExchange);
            }

            if (!authorization.startsWith("Digest ")) {
                throw new RuntimeException("Invalid 'Authorization' header.");
            }
            if("Digest logout".equals(authorization)) {
                if(context.getPrincipal() == null) {
                    return new Authenticator.Retry(401);
                }
                Misc.log("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "Logout/" + context.getPrincipal());

                httpExchange.setAttribute("digest-context", null);
                Headers responseHeaders = httpExchange.getResponseHeaders();
                responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));

                return new Authenticator.Retry(401);
            }
            String challenge = authorization.substring(7);
            Map<String, String> challengeParameters = parseDigestChallenge(challenge);

            HttpPrincipal principal = validateUser(httpExchange, challengeParameters);

            if (principal == null) {
                return fetchHeaders(httpExchange);
            }
            if(timestamps.getTimeout() > 0 && !timestamps.containsKey(principal.getName())) {
                timestamps.put(principal.getName());
                return fetchHeaders(httpExchange);
            }
            if(timestamps.expired(principal.getName())) {
                timestamps.put(principal.getName());
//                httpExchange.setAttribute("digest-context", null);
                return fetchHeaders(httpExchange);
            }

            if (!context.isAuthenticated()) {
                Misc.log("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "Logged in/" + principal);
            }

            if (useNonce(challengeParameters.get("nonce"))) {
                timestamps.put(principal.getName());
                context.principal = principal;
                return new Authenticator.Success(principal);
            }
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(true));
            return new Authenticator.Retry(401);
//            return fetchHeaders(httpExchange);

        } catch (Exception e) {
            Misc.err("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "got error", e);
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
            return new Authenticator.Retry(401);
        }
    }

    private Result fetchHeaders(HttpExchange httpExchange) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));

        String content = fetchSplash(true).build();
        httpExchange.sendResponseHeaders(401, content.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(content.getBytes());
        }
        return null;
    }

    private HttpPrincipal validateUser(HttpExchange httpExchange, Map<String, String> challengeParameters) throws AuthenticationException {
        String realm = challengeParameters.get("realm");
        String username = challengeParameters.get("username");

        if (realm == null || realm.length() == 0 || username == null || username.length() == 0) {
            return null;
        }

        Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);
        if(!admins.exists(username)) {
            Misc.err("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "not found user [" + username + "]");
            return null;
        }

        Admins.Admin currentAdmin = admins.get(username);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            md5.update(username.getBytes());
//            md5.update(COL);
//            md5.update(realm.getBytes());
//            md5.update(COL);
//            md5.update("".getBytes());
//
//            byte[] ha1 = toHexBytes(md5.digest());
//            try {
//                System.out.println("Username:"+username+", realm:"+realm+", digest:"+new String(ha1, "UTF-8"));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            byte[] ha1 = currentAdmin.getDigest().getBytes();

            md5.update(httpExchange.getRequestMethod().getBytes());
            md5.update(COL);
            md5.update(challengeParameters.get("uri").getBytes());

            byte[] ha2 = toHexBytes(md5.digest());

            md5.update(ha1);
            md5.update(COL);
            md5.update(challengeParameters.get("nonce").getBytes());
            md5.update(COL);
            md5.update(challengeParameters.get("nc").getBytes());
            md5.update(COL);
            md5.update(challengeParameters.get("cnonce").getBytes());
            md5.update(COL);
            md5.update("auth".getBytes());
            md5.update(COL);
            md5.update(ha2);

            byte[] expectedResponse = toHexBytes(md5.digest());
            byte[] actualResponse = challengeParameters.get("response").getBytes();

            if (MessageDigest.isEqual(expectedResponse, actualResponse)) {
                return new HttpPrincipal(username, realm);
            } else {
                Misc.err("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "incorrect password for", "[" + username + "]");
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException("No MD5? Should not be possible", e);
        }
    }


    private String getChallenge(boolean stale) {
        StringBuilder buf = new StringBuilder();
        buf.append("realm=\"").append(realm).append("\",");
        buf.append("qop=\"auth,auth-int\",");
        buf.append("nonce=\"").append(createNonce()).append("\"");
        if (stale) {
            buf.append(",stale=true");
        }
        return buf.toString();
    }

    private Map<String, String> parseDigestChallenge(String challenge) {
        Map<String, String> ret = new HashMap<>();
        HeaderParser parser = new HeaderParser(challenge);
        while (parser.hasNext()) {
            HeaderParser.Parameter next = parser.next();
            ret.put(next.key, next.value);
        }
        return ret;
    }

    private class HeaderParser {

        private static final char DELIM = ',';
        private static final char EQ = '=';
        private static final char ESC = '\\';
        private static final char Q = '"';

        private final String header;
        private final int length;
        private int pos = 0;
        private boolean seenNext;

        HeaderParser(String message) {
            this.header = message;
            this.length = message.length();
        }

        boolean hasNext() {
            if (seenNext) {
                return true;
            }
            if (pos >= length) {
                return false;
            }
            int nextEquals = header.indexOf(EQ, pos);
            if (nextEquals < 0 || nextEquals >= length - 1) {
                return false;
            }
            seenNext = true;
            return true;
        }

        Parameter next() {
            if (!seenNext && !hasNext()) {
                return null;
            }
            Parameter ret = new Parameter();
            int equalsPos = header.indexOf(EQ, pos);
            ret.key = header.substring(pos, equalsPos).trim();
            pos = equalsPos + 1;
            int nextDelim = header.indexOf(DELIM, pos);
            int nextQ = header.indexOf(Q, pos);
            boolean quoted = false;
            if (nextQ > 0 && (nextDelim < 0 || nextQ < nextDelim)) {
                quoted = true;
            }

            if (quoted) {
                String invalid = header.substring(pos, nextQ).trim();
                if (!"".equals(invalid)) {
                    throw new IllegalArgumentException("Invalid header content '" + invalid + "' for " + ret.key);
                }
                pos = nextQ;
                int endQ = -1;
                while (endQ < 0) {
                    nextQ = header.indexOf(Q, nextQ + 1);
                    if (nextQ < 0) {
                        throw new IllegalArgumentException("No matching quote for " + ret.key);
                    }
                    if (header.charAt(nextQ - 1) != ESC) {
                        endQ = nextQ;
                    }
                }
                ret.value = header.substring(pos + 1, endQ);
                int nextDelim2 = header.indexOf(DELIM, pos);
                if (nextDelim2 > 0) {
                    pos = nextDelim2 + 1;
                }
            } else {
                int nextDelim2 = header.indexOf(DELIM, pos);
                if (nextDelim2 > 0) {
                    ret.value = header.substring(pos, nextDelim2).trim();
                    pos = nextDelim2 + 1;
                } else {
                    ret.value = header.substring(pos, length - 1).trim();
                    pos = length + 1;
                }

            }
            seenNext = false;
            return ret;
        }
        class Parameter {
            String key;
            String value;
        }
    }

    public String createNonce() {
        byte[] ret = new byte[16];
        random.nextBytes(ret);
        String retStr = toHexString(ret);
        synchronized (givenNonces) {
            givenNonces.add(retStr);
        }
        return retStr;
    }

    public boolean useNonce(String nonceToUse) {
        synchronized (givenNonces) {
            return givenNonces.remove(nonceToUse);
        }

    }
    private DigestContext getOrCreateContext(HttpExchange httpExchange) {
        DigestContext ret = (DigestContext)httpExchange.getAttribute("digest-context");
        if (ret == null) {
            ret = new DigestContext();
            httpExchange.setAttribute("digest-context", ret);
        }
        return ret;
    }

    private static class DigestContext {
        private HttpPrincipal principal = null;

        boolean isAuthenticated() {
            return principal != null;
        }

        HttpPrincipal getPrincipal() {
            return principal;
        }
    }

    static final byte[] HEX_BYTES = new byte[]
                                            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte aDigest : digest) {
            sb.append(Integer.toHexString((aDigest & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static byte[] toHexBytes(byte[] toBeConverted) {
        if (toBeConverted == null) {
            throw new NullPointerException("Parameter to be converted can not be null");
        }
        byte[] converted = new byte[toBeConverted.length * 2];
        for (int i = 0; i < toBeConverted.length; i++) {
            byte b = toBeConverted[i];
            converted[i * 2] = HEX_BYTES[b >> 4 & 0x0F];
            converted[i * 2 + 1] = HEX_BYTES[b & 0x0F];
        }
        return converted;
    }

    private HtmlGenerator fetchSplash(boolean withButtons) {

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);

        HtmlGenerator html = new HtmlGenerator();

        html.getHead().add(HtmlGenerator.TITLE).with("Edeqa");
        html.getHead().add(HtmlGenerator.LINK).with(HtmlGenerator.REL, "icon").with(HtmlGenerator.HREF, "/icons/favicon.ico");
        html.getHead().add(HtmlGenerator.STYLE).with("@import url('/css/edequate.css');@import url('/css/edequate-admin.css');");
        html.getHead().add(HtmlGenerator.META).with(HtmlGenerator.NAME, "viewport").with(HtmlGenerator.CONTENT, "width=device-width, initial-scale=1, maximum-scale=5, user-scalable=no");
        html.getHead().add(HtmlGenerator.SCRIPT).with(HtmlGenerator.ASYNC, true).with(HtmlGenerator.SRC, "/js/Edequate.js").with("data-variable", "u");

        HtmlGenerator.Tag body = html.getBody().add(DIV).with(ID, "loading-dialog").with(CLASS, "admin-splash-layout");
        body.add(IMG).with(CLASS, "admin-splash-logo").with(SRC, "/images/logo.svg");
        body.add(DIV).with(CLASS, "admin-splash-title").with(arguments.getAppName() + " " + arguments.getVersion());

        body.add(DIV).with(CLASS, "admin-splash-subtitle").with("Admin");

        if(withButtons) {
            HtmlGenerator.Tag buttons = body.add(DIV).with(CLASS, "admin-splash-buttons");

            buttons.add(BUTTON).with("Home").with(CLASS, "dialog-button").with(ONCLICK, "window.location = '/home';");
            buttons.add(BUTTON).with("Login").with(CLASS, "dialog-button").with(ONCLICK, "u.clear(this.parentNode);window.location.reload();");
            buttons.add(BUTTON).with("Forgot password").with(CLASS, "dialog-button").with(ONCLICK, "window.location = '" + RestorePassword.TYPE + "';");
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

        return html;
    }


    public class ExpiringHashMap<K,V> extends HashMap<K,V> {
        private HashMap<K,Long> timestamp;
        private Long timeout = 0L;
        private int max = 100;

        public ExpiringHashMap() {
            timestamp = new HashMap<>();
        }

        @Override
        public V get(Object key) {
            if(expired(key)) {
                return null;
            }
            return super.get(key);
        }

        public boolean expired(Object key) {
            if(containsKey(key)) {
                Long last = timestamp.get(key);
                long now = Calendar.getInstance().getTimeInMillis();
                if(timeout > 0 && now - last > timeout) {
                    return true;
                }
            }
            return false;
        }

        public boolean valid(Object key) {
            if(containsKey(key) && !expired(key)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public V put(K key, V value) {
            checkMax();
            long now = Calendar.getInstance().getTimeInMillis();
            timestamp.put(key, now);
            return super.put(key, value);
        }

        private void checkMax() {
            if(size() > max) {
                long now = Calendar.getInstance().getTimeInMillis();
                Iterator<Entry<K, Long>> iter = timestamp.entrySet().iterator();
                while(iter.hasNext()) {
                    Entry<K, Long> entry = iter.next();
                    if(timeout > 0 && now - entry.getValue() > timeout) {
                        super.remove(entry.getKey());
                        iter.remove();
                    }
                }
            }
        }

        public V put(K key) {
            long now = Calendar.getInstance().getTimeInMillis();
            checkMax();
            timestamp.put(key, now);
            return super.put(key, null);
        }

        @Override
        public V remove(Object key) {
            timestamp.remove(key);
            return super.remove(key);
        }

        public ExpiringHashMap<K, V> setTimeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Long getTimeout() {
            return timeout;
        }

        public Long expiredInMillis(K key) {
            if(containsKey(key)) {
                long now = Calendar.getInstance().getTimeInMillis();
                return now - timestamp.get(key);
            } else {
                return 0L;
            }
        }

        @Override
        public String toString() {
            return "ExpiringHashMap{" +
               "timestamp=" + timestamp +
               '}' + super.toString();
        }
    }

}