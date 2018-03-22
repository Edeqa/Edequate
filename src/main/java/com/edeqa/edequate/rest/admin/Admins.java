package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Iterator;

import static com.edeqa.edequate.helpers.DigestAuthenticator.COL;
import static com.edeqa.edequate.helpers.DigestAuthenticator.toHexBytes;

public class Admins extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/data/admins";

    private final static String ADD = "add";
    private final static String ADMIN = "admin";
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
    private final static String PASSWORD_HASH = "password_hash";
    private final static String REALM = "realm";
    private final static String ROLES = "roles";
    private final static String SECURITY = "security";
    private final static String SECURITY_EXPIRED = "expired";
    private final static String SECURITY_EXPIRING_SOON = "expiring soon";
    private final static String SECURITY_MEDIUM = "medium";
    private final static String SECURITY_MISSING = "missing";
    private final static String SECURITY_STRONG = "strong";
    private final static String SECURITY_WEAK = "weak";

    private JSONObject users;

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

        if(users == null) {
            read();
        }
        JSONObject options = new JSONObject(body);

        Object result = null;
        switch(options.getString(MODE)) {
            case MODE_CURRENT:
                Misc.log("Admins", "requested for current", "[" + request.getUserName() + "]");
                result = fetchAdmin(request.getUserName());
                break;
            case MODE_LIST:
                Misc.log("Admins", "requested list");
                result = fetchList();
                break;
            case MODE_SAVE:
                Misc.log("Admins", "save", options);
                saveAdmin(options.getJSONObject(ADMIN), options.getJSONObject(INITIAL));
//                result = fetchAdmin(options.getString(LOGIN));
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_EXTENDED);
                json.put(MESSAGE, "Arguments not enough.");
                return;
            case MODE_SELECT:
                Misc.log("Admins", "requested for", "[" + options.getString(LOGIN) + "]");
                result = fetchAdmin(options.getString(LOGIN));
                break;
            default:
                json.put(STATUS, STATUS_ERROR);
                json.put(CODE, ERROR_NOT_EXTENDED);
                json.put(MESSAGE, "Arguments not enough.");
                return;
        }

        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_JSON);
        json.put(MESSAGE, result);
    }

    private void saveAdmin(JSONObject json, JSONObject initial) throws Exception {
        read();
        System.out.println("Before:"+users);


        String login = null, password = null;
        if(json.has(LOGIN)) login = json.getString(LOGIN);

        boolean add = false;
        if(json.has(ADD)) {
            add = json.getBoolean(ADD);
        }
        if(add && users.has(login)) {
            throw new Exception("Admin already exists: " + login);
        }
        if(!add && !users.has(login)) {
            throw new Exception("Admin not exists: " + login);
        }

        JSONObject admin = new JSONObject();
        if(!add) {
            admin = users.getJSONObject(login);
        }
        if(json.has(PASSWORD)) password = json.getString(PASSWORD);

        admin.remove(NAME);
        if(json.has(NAME) && json.getString(NAME).length() > 0) admin.put(NAME, json.getString(NAME));

        admin.remove(EMAIL);
        if(json.has(EMAIL) && json.getString(EMAIL).length() > 0) admin.put(EMAIL, json.getString(EMAIL));

        admin.remove(EXPIRATION);
        if(json.has(EXPIRATION) && json.getLong(EXPIRATION) > 0) admin.put(EXPIRATION, json.getLong(EXPIRATION));
        if(json.has(ROLES)) admin.put(ROLES, json.getString(ROLES));

        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        String realm = arguments.getRealm();

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(login.getBytes());
        md5.update(COL);
        md5.update(realm.getBytes());
        md5.update(COL);
        md5.update(password.getBytes());

        byte[] ha1 = toHexBytes(md5.digest());

        admin.put(PASSWORD_HASH, new String(ha1, "UTF-8"));

        System.out.println("Username:"+login+", realm:"+realm+", digest:"+new String(ha1, "UTF-8"));

        System.out.println("After:"+users);

        WebPath usersWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.admins.json");
        try (FileWriter writer = new FileWriter(usersWebPath.path())) {
            writer.write(users.toString(2));
            Misc.log("Admins", "saved admin:", admin.toString());
            json.put(STATUS, STATUS_SUCCESS);
        }
        read();
    }

    private JSONObject fetchAdmin(String login) {
        JSONObject user = new JSONObject();
        if(login == null || !exists(login)) return user;
        user.put(LOGIN, login);
        user.put(EMAIL, getEmail(login));
        user.put(EXPIRATION, getExpiration(login));
        user.put(NAME, getName(login));
        user.put(SECURITY, fetchPower(login));
        user.put(REALM, getRealm(login));
        user.put(ROLES, getRoles(login));
        return user;
    }

    private JSONArray fetchList() {
        JSONArray usersObject = new JSONArray();
        read();
        Iterator<String> keys = users.keys();
        while(keys.hasNext()) {
            String login = keys.next();
            usersObject.put(fetchAdmin(login));
        }
        return usersObject;
    }

    public Admins read() {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        WebPath usersWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.admins.json");
        try {
            setUsers(new JSONObject(usersWebPath.content()));
        } catch (IOException e) {
            Misc.err("Admins", "got an error", e);
        }
        return this;
    }

    public boolean exists(String username) {
        return users.has(username);
    }

    public String getEmail(String username) {
        return (String) getValue(username, EMAIL);
    }

    public String getName(String username) {
        return (String) getValue(username, NAME);
    }

    public String getPasswordHash(String username) {
        return (String) getValue(username, PASSWORD_HASH);
    }

    public long getExpiration(String username) {
        Object value = getValue(username, EXPIRATION);
        if(value != null) {
            return (long) value;
        } else {
            return 0L;
        }
    }

    public String fetchPower(String username) {
        String power = (String) getValue(username, SECURITY);
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
        long expiration = getExpiration(username);
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

    public String getRealm(String username) {
        return (String) getValue(username, REALM);
    }

    public String getRoles(String username) {
        return (String) getValue(username, ROLES);
    }

    private Serializable getValue(String username, String key) {
        if(getUsers().has(username)) {
            if(getUsers().get(username) instanceof JSONObject) {
                JSONObject json = getUsers().getJSONObject(username);
                if(json.has(key)) {
                    return (Serializable) json.get(key);
                } else {
                    return null;
                }
            } else {
                return "User data is corrupted.";
            }
        } else {
            return "User not found.";
        }
    }

    private JSONObject getUsers() {
        return users;
    }

    private void setUsers(JSONObject users) {
        this.users = users;
    }


}
