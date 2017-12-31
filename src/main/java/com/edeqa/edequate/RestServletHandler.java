package com.edeqa.edequate;


import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.edequate.rest.Nothing;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import static com.edeqa.edequate.interfaces.RestAction.CALLBACK;
import static com.edeqa.edequate.interfaces.RestAction.CODE;
import static com.edeqa.edequate.interfaces.RestAction.CODE_REDIRECT;
import static com.edeqa.edequate.interfaces.RestAction.FALLBACK;
import static com.edeqa.edequate.interfaces.RestAction.MESSAGE;
import static com.edeqa.edequate.interfaces.RestAction.REQUEST;
import static com.edeqa.edequate.interfaces.RestAction.STATUS;
import static com.edeqa.edequate.interfaces.RestAction.STATUS_ERROR;

public class RestServletHandler extends AbstractServletHandler {

    private Map<String, RestAction> actions;

    @Override
    public void init() throws ServletException {
        super.init();
        setActions(new LinkedHashMap<String, RestAction>());
        populateRestActions("com.edeqa.edequate.rest");
    }

    protected void populateRestActions(String packageName) {

        Misc.log("Rest", "init actions within", packageName);
        List<Class> classes = getAllClasses( packageName);

        for (Class item : classes) {
            try {
                Object instance = item.newInstance();
                if (instance instanceof RestAction) {
                    String apiVersion = (String) instance.getClass().getField("apiVersion").get(instance);
                    String actionName = (String) instance.getClass().getField("actionName").get(instance);
                    RestAction action = (RestAction) instance;
                    getActions().put("/rest/" + apiVersion + "/" + actionName, action);

                    Misc.log("Rest", "register:", action.getClass().getSimpleName(), "for:", "/rest/" + apiVersion + "/" + actionName);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public void perform(RequestWrapper requestWrapper) throws IOException {

        String path = requestWrapper.getRequestURI().toString().replaceFirst("/$", "");

        Map<String, String[]> arguments = requestWrapper.getParameterMap();

        JSONObject json = new JSONObject();
        json.put(REQUEST, path);

        if (getActions().containsKey(path)) {
            Misc.log("Rest", "performing:", getActions().get(path).getClass().getSimpleName());
            getActions().get(path).call(json, requestWrapper);
        }

        if (!json.has(STATUS)) {
            Misc.log("Rest", "performing:", Nothing.class.getSimpleName());
            new Nothing().call(json, requestWrapper);
        }

        if(json.has(CODE) && json.getInt(CODE) == CODE_REDIRECT && json.has(MESSAGE)) {
            Misc.log("Rest", "redirect:", json.getString(MESSAGE));
            requestWrapper.sendRedirect(json.getString(MESSAGE));
            return;
        }

        String callback = null;
        String fallback = null;
        if (arguments.containsKey(CALLBACK)) {
            callback = arguments.get(CALLBACK)[0];
        }
        if (arguments.containsKey(FALLBACK)) {
            fallback = arguments.get(FALLBACK)[0];
        }

        if (json.getString(STATUS).equals(STATUS_ERROR) && !Misc.isEmpty(fallback)) {
            callback = fallback;
        }

        if (json.getString(STATUS).equals(STATUS_ERROR) && !Misc.isEmpty(callback) ) {
            requestWrapper.sendError(json.getInt(CODE),callback + "(" + json.toString() + ");");
        } else if (json.getString(STATUS).equals(STATUS_ERROR)) {
            requestWrapper.sendError(json.getInt(CODE), json);
        } else if (!Misc.isEmpty(callback) ) {
            requestWrapper.sendResult(callback + "(" + json.toString() + ");");
        } else {
            requestWrapper.sendResult(json);
        }
    }

    private List<Class> getAllClasses(String pckgname) {
        try {
            ArrayList<Class> classes = new ArrayList<>();

            /*final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            System.out.println("LOADER:"+loader);
            System.out.println("LOADER:"+ClassPath.from(loader).getTopLevelClasses());
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            System.out.println("CLASSINFO:"+info);
                if (info.getName().startsWith(pckgname)) {
                    final Class<?> clazz = info.load();
                    classes.add(clazz);
                    // do something with your clazz
                }
            }*/

            // Get a File object for the package
            File directory = null;
            try {
                directory = new File(Thread.currentThread().getContextClassLoader().getResource(pckgname.replace('.', '/')).getFile().replace("%20", " "));
            } catch (NullPointerException e) {
                Misc.err("Rest", e);
            }
            if (directory != null && directory.exists()) {
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".class") && !file.contains("$")) {
                            classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                        }
                    }
                }
            } else {
                Misc.err("Rest", "Directory does not exist");
            }
            return classes;
        } catch (Exception e) {
            Misc.err("Rest", e);
        }
        return Collections.emptyList();
    }

    private Map<String, RestAction> getActions() {
        return actions;
    }

    private void setActions(Map<String, RestAction> actions) {
        this.actions = actions;
    }
}