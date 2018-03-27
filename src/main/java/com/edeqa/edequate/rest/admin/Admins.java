package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.edeqa.edequate.helpers.DigestAuthenticator.COL;
import static com.edeqa.edequate.helpers.DigestAuthenticator.toHexBytes;

public class Admins extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/data/admins";

    private final static String ADD = "add";
    private final static String ADMIN = "admin";
    private final static String DIGEST = "digest";
    private final static String EMAIL = "email";
    private final static String EXPIRATION = "expiration";
    private final static String LOGIN = "login";
    private final static String INITIAL = "initial";
    private final static String MODE = "mode";
    private final static String MODE_CURRENT = "current";
    private final static String MODE_LIST = "list";
    private final static String MODE_SAVE = "save";
    private final static String MODE_SELECT = "select";
    private final static String NAME = "name";
    private final static String PASSWORD = "password";
    private final static String REALM = "realm";
    private final static String ROLES = "roles";
    private final static String SECURITY = "security";
    private final static String SECURITY_EXPIRED = "expired";
    private final static String SECURITY_EXPIRING_SOON = "expiring soon";
    private final static String SECURITY_MEDIUM = "medium";
    private final static String SECURITY_MISSING = "missing";
    private final static String SECURITY_STRONG = "strong";
    private final static String SECURITY_WEAK = "weak";

    private Map<String, Admin> admins;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) throws Exception {
        String body = request.getBody();
        if(Misc.isEmpty(body)) {
            Misc.err("Admins", "not performed, arguments not defined");
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_NOT_EXTENDED);
            json.put(MESSAGE, "Arguments not defined.");
            return;
        }

        if(admins == null) {
            read();
        }
        JSONObject options = new JSONObject(body);

        Object result;
        switch(options.getString(MODE)) {
            case MODE_CURRENT:
                Misc.log("Admins", "requested for current", "[" + request.getUserName() + "]");
                result = current(request).toJSON();
                break;
            case MODE_LIST:
                Misc.log("Admins", "requested list");
                result = fetchList();
                break;
            case MODE_SAVE:
                Misc.log("Admins", "save admin");
                saveAdmin(options.getJSONObject(ADMIN), options.getJSONObject(INITIAL));
                result = null;
                break;
            case MODE_SELECT:
                Misc.log("Admins", "requested for", "[" + options.getString(LOGIN) + "]");
                result = get(options.getString(LOGIN)).toJSON();
                break;
            default:
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_EXTENDED);
                json.put(MESSAGE, "Not enough arguments.");
                return;
        }

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_JSON);
        json.put(MESSAGE, result);
    }

    private void saveAdmin(JSONObject json, JSONObject initial) throws Exception {
        read();
        try {
            String login = null, password = null;
            if (json.has(LOGIN)) login = json.getString(LOGIN);

            boolean add = false;
            if (json.has(ADD)) {
                add = json.getBoolean(ADD);
            }
            if (add && exists(login)) {
                throw new Exception("Admin already exists: " + login);
            }
            if (!add && !exists(login)) {
                throw new Exception("Admin not exists: " + login);
            }

            Misc.err("Admins", "is saving for", "[" + login + "]");

            Admin admin = new Admin(login, json);
            if (!add) {
                admin = admins.get(login);
            }
            if (json.has(PASSWORD)) admin.storePassword(json.getString(PASSWORD));
            admin.fetchFrom(json);

            JSONObject adminsOut = new JSONObject();
            for (Map.Entry<String, Admin> entry : admins.entrySet()) {
                adminsOut.put(entry.getKey(), entry.getValue().getJSON());
            }

            Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
            WebPath usersWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.admins.json.new");
            try (FileWriter writer = new FileWriter(usersWebPath.path())) {
                writer.write(adminsOut.toString(2));
                json.put(STATUS, STATUS_SUCCESS);
                writer.close();
                usersWebPath.rename(".admins.json");
            }
        } catch(Exception e) {
            read();
            throw e;
        }
        read();
    }

    private JSONArray fetchList() {
        JSONArray usersObject = new JSONArray();
        read();
        for(Map.Entry<String,Admin> entry: admins.entrySet()) {
            usersObject.put(entry.getValue().toJSON());
        }
        return usersObject;
    }

    public Admins read() {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        WebPath usersWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.admins.json");
        try {
            JSONObject json = new JSONObject(usersWebPath.content());
            admins = new HashMap<>();
            Iterator<String> iter = json.keys();
            while(iter.hasNext()) {
                String login = iter.next();
                admins.put(login, new Admin(login, json.getJSONObject(login)));
            }
        } catch (IOException e) {
            Misc.err("Admins", "got an error", e);
        }
        return this;
    }

    public boolean exists(String username) {
        return admins.containsKey(username);
    }

    public class Admin {
        private String login;
        private JSONObject json;
        public Admin(String login, JSONObject json) {
            this.login = login;
            this.json = json;
        }

        public String fetchPower() {
            String power = (String) getValue(SECURITY);
            if(power == null) {
                power = SECURITY_MISSING;
            }
            switch(power) {
                case SECURITY_MEDIUM:
                case SECURITY_STRONG:
                case SECURITY_WEAK:
                    break;
                default:
                    power = SECURITY_MISSING;
            }
            long expiration = getExpiration();
            if(expiration > 0) {
                Calendar cal = Calendar.getInstance();
                long now = cal.getTime().getTime();
                if(expiration - now <= 0) {
                    power = SECURITY_EXPIRED;
                } else if(expiration - now < 30*24*60*60*1000L) {
                    power = SECURITY_EXPIRING_SOON;
                }
            }
            return power;
        }

        private JSONObject toJSON() {
            JSONObject user = new JSONObject();
            user.put(LOGIN, login);
            user.put(EMAIL, getEmail());
            user.put(EXPIRATION, getExpiration());
            user.put(NAME, getName());
            user.put(SECURITY, fetchPower());
            user.put(REALM, getRealm());
            user.put(ROLES, getRoles());
            return user;
        }

        public void fetchFrom(JSONObject json) {
            this.json.remove(NAME);
            if(json.has(NAME) && json.getString(NAME).length() > 0) this.json.put(NAME, json.getString(NAME));

            this.json.remove(EMAIL);
            if(json.has(EMAIL) && json.getString(EMAIL).length() > 0) this.json.put(EMAIL, json.getString(EMAIL));

            this.json.remove(EXPIRATION);
            if(json.has(EXPIRATION) && json.getLong(EXPIRATION) > 0) this.json.put(EXPIRATION, json.getLong(EXPIRATION));
            if(json.has(ROLES)) this.json.put(ROLES, json.getString(ROLES));
        }

        public JSONObject getJSON() {
            return json;
        }

        public String getEmail() {
            return (String) getValue(EMAIL);
        }

        public String getName() {
            return (String) getValue(NAME);
        }

        public String getDigest() {
            return (String) getValue(DIGEST);
        }

        public long getExpiration() {
            Object value = getValue(EXPIRATION);
            if(value != null) {
                return (long) value;
            } else {
                return 0L;
            }
        }

        public String getRealm() {
            return (String) getValue(REALM);
        }

        public String getRoles() {
            return (String) getValue(ROLES);
        }

        private Serializable getValue(String key) {
            if(json.has(key)) {
                return (Serializable) json.get(key);
            } else {
                return null;
            }
        }

        public void storePassword(String password) throws Exception {

            if(password == null || password.length() == 0) return;

            Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
            String realm = arguments.getRealm();

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(login.getBytes());
            md5.update(COL);
            md5.update(realm.getBytes());
            md5.update(COL);
            md5.update(password.getBytes());

            byte[] ha1 = toHexBytes(md5.digest());
            Misc.log("Admins", "store login:", login, ", realm:"+realm+", digest:"+new String(ha1, "UTF-8"));

            this.json.put(DIGEST, new String(ha1, "UTF-8"));

            if(password.length() < 9) {
                this.json.put(SECURITY, SECURITY_WEAK);
            } else if(password.length() < 13) {
                this.json.put(SECURITY, SECURITY_MEDIUM);
            } else {
                this.json.put(SECURITY, SECURITY_STRONG);
            }
        }

        @Override
        public String toString() {
            return "Admin{" +
               "login='" + login + '\'' +
               ", json=" + json +
               '}';
        }
    }

    public Admin get(String login) {
        return admins.get(login);
    }

    public Admin current(RequestWrapper request) {
        try {
            String name = request.getUserName();
            return admins.get(name);
        } catch (Exception e) {

        }
        return null;
    }

    public Admin current(HttpExchange httpExchange) {
        try {
            String name = httpExchange.getPrincipal().getUsername();
            return admins.get(name);
        } catch(Exception e) {

        }
        return null;
    }


}
