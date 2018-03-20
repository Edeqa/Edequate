package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.IOException;

public class Users extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/users";

    private final static String PASSWORD_HASH = "password_hash";
    private final static String ROLES = "roles";
    private JSONObject users;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
//        String body = request.getBody();
//        if(Misc.isEmpty(body)) {
//            Misc.err("Page", "not performed, arguments not defined");
//            json.put(STATUS, STATUS_ERROR);
//            json.put(CODE, ERROR_NOT_EXTENDED);
//            json.put(MESSAGE, "Arguments not defined.");
//            return;
//        }

        if(users == null) {
            read();
        }
    }

    public Users read() {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);
        WebPath usersWebPath = new WebPath(arguments.getWebRootDirectory(), "data/users.json");
        try {
            setUsers(new JSONObject(usersWebPath.content()));
        } catch (IOException e) {
            Misc.err("Users", "got an error", e);
        }
        return this;
    }

    public String getPasswordHash(String username) {
        return getValue(username, PASSWORD_HASH);
    }

    public boolean exists(String username) {
        return users.has(username);
    }

    public String getRoles(String username) {
        return getValue(username, ROLES);
    }

    private String getValue(String username, String key) {
        if(getUsers().has(username)) {
            if(getUsers().get(username) instanceof JSONObject) {
                JSONObject json = getUsers().getJSONObject(username);
                if(json.has(key)) {
                    return json.getString(key);
                } else {
                    return "User data is not found: " + key;
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
