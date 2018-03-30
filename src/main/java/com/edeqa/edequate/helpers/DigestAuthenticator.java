package com.edeqa.edequate.helpers;


import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.admin.Admins;
import com.edeqa.edequate.rest.admin.Splash;
import com.edeqa.eventbus.EventBus;
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

import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;

/*
 * Created 5/16/2017.
 */
@SuppressWarnings("restriction")
public class DigestAuthenticator extends Authenticator {

    public static final byte COL = ':';

    private static final Set<String> givenNonces = new HashSet<>();
    private static final SecureRandom random = new SecureRandom();
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

            Admins admins = (Admins) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Admins.TYPE);
            if(!admins.exists()) {
                admins.generate("admin");
                return fetchSplash(httpExchange, false, "Admin account have been generated. Please use login <code><b>admin</b></code> and empty password first.");
            }

            if (authorization == null) {
//                Misc.log("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "Login request", "[" + httpExchange.getRequestURI() + "]");
                return fetchSplash(httpExchange, true, null);
            }
            if (authorization.startsWith("Basic ")) {
                return fetchSplash(httpExchange, true, null);
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
                return fetchSplash(httpExchange, true, null);
            }
            if(timestamps.getTimeout() > 0 && !timestamps.containsKey(principal.getName())) {
                timestamps.put(principal.getName());
                return fetchSplash(httpExchange, true, null);
            }
            if(timestamps.expired(principal.getName())) {
                timestamps.put(principal.getName());
//                httpExchange.setAttribute("digest-context", null);
                return fetchSplash(httpExchange, true, null);
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
//            return fetchSplash(httpExchange);

        } catch (Exception e) {
            Misc.err("DigestAuthenticator", "[" + httpExchange.getRemoteAddress().getAddress().getHostAddress() + "]", "got error", e);
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
            return new Authenticator.Retry(401);
        }
    }

    private Result fetchSplash(HttpExchange httpExchange, boolean requireAuthentication, String info) throws IOException {
        if(requireAuthentication) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
        }

        Splash splash = (Splash) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Splash.TYPE);
        String content = splash.setInfo(info).setButtons(true).fetchSplash().build();
        httpExchange.sendResponseHeaders(401, content.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(content.getBytes());
        }
        return null;
    }

    private HttpPrincipal validateUser(HttpExchange httpExchange, Map<String, String> challengeParameters) {
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

    public static String createNonce() {
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