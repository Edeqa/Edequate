package com.edeqa.edequate;


import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.Locales;
import com.edeqa.edequate.rest.Nothing;
import com.edeqa.edequate.rest.Resource;
import com.edeqa.edequate.rest.Uptime;
import com.edeqa.edequate.rest.Version;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.edequate.rest.system.OneTime;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.edeqa.edequate.abstracts.AbstractAction.CODE;
import static com.edeqa.edequate.abstracts.AbstractAction.FALLBACK;
import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS_ERROR;
import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;

@SuppressWarnings("unchecked")
public class RestServletHandler extends AbstractServletHandler {

    private final EventBus<AbstractAction> restBus;
    private final EventBus<AbstractAction> systemBus;

    private final Map<String, AbstractAction> pool;

    public RestServletHandler() {
        EventBus.setMainRunner(EventBus.RUNNER_SINGLE_THREAD);
        restBus = (EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS);
        systemBus = (EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS);
        pool = new LinkedHashMap<>();
        getSystemBus().registerIfAbsent(new Arguments());
    }

    public void useDefault() {
        Arguments arguments = (Arguments) getSystemBus().getHolder(Arguments.TYPE);

        getSystemBus().registerIfAbsent(new OneTime());

        registerAction(new Files().setFilenameFilter((dir, name) -> name.contains("Holder")).setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("js/main").setActionName("/rest/main"));
        registerAction(new Files().setFilenameFilter((dir, name) -> name.contains("pages-")).setFilenameProcess(name -> name.replaceAll("pages-(.*?)\\.json", "$1")).setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("data").setActionName("/rest/data/types"));
        registerAction(new Locales().setWebDirectory(arguments.getWebRootDirectory()));
        registerAction(new Resource().setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("content").setActionName("/rest/content"));
        registerAction(new Resource().setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("data").setActionName("/rest/data"));
        registerAction(new Resource().setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("resources").setActionName("/rest/resources"));
        registerAction(new Uptime());
        registerAction(new Version());

//        registerAction(new Nothing());
    }

    protected AbstractAction<RequestWrapper> registerAction(AbstractAction<RequestWrapper> actionHolder) {
        pool.put(actionHolder.getType(), actionHolder);
        return actionHolder;
    }

    @SuppressWarnings("unused")
    protected void populateRestActions(String packageName) {
        Misc.log("Rest", "automatic register actions within", packageName);
        List<Class> classes = getAllClasses( packageName);

        for (Class<?> item : classes) {
            try {
                Object instance = item.newInstance();
                if (instance instanceof AbstractAction) {
                    registerAction((AbstractAction<RequestWrapper>) instance);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void perform(RequestWrapper requestWrapper) throws IOException {
        String path = requestWrapper.getRequestURI().getPath().replaceFirst("/$", "");

        Map<String, List<String>> arguments = requestWrapper.getParameterMap();

        JSONObject json = new JSONObject();
        json.put(AbstractAction.REQUEST, path);

        String ipRemote = requestWrapper.getRemoteAddress().getAddress().getHostAddress();
        try {
            if (restBus.getHolder(path) != null) {
                Misc.log("Rest", "[" + ipRemote + "]", "perform:", restBus.getHolder(path).getClass().getSimpleName(), "[" + path + "]");
                restBus.getHolder(path).call(json, requestWrapper);
            }
        } catch(Exception e) {
            Misc.err("Rest", "failed:", e);
            if (!json.has(STATUS)) {
                new Nothing().setThrowable(e).call(json, requestWrapper);
            }
        }

        if (!json.has(STATUS)) {
            Misc.log("Rest", "[" + ipRemote + "]", "perform:", Nothing.class.getSimpleName(), "[" + path + "]");
            new Nothing().call(json, requestWrapper);
        }
        if (AbstractAction.STATUS_DELAYED.equals(json.getString(STATUS))) {
            return;
        }

        if(json.has(CODE)) {
            if (json.getInt(CODE) == AbstractAction.CODE_MOVED_TEMPORARILY && json.has(AbstractAction.MESSAGE)) {
                Misc.log("Rest", "redirect:", json.getString(AbstractAction.MESSAGE));
                requestWrapper.sendRedirect(json.getString(AbstractAction.MESSAGE));
                return;
            }
        }

        String callback = null;
        String fallback = null;
        if (arguments.containsKey(AbstractAction.CALLBACK)) {
            callback = arguments.get(AbstractAction.CALLBACK).get(0);
        }
        if (arguments.containsKey(FALLBACK)) {
            fallback = arguments.get(FALLBACK).get(0);
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

    protected void registerActionsPool() {
        if (!pool.isEmpty()) {
            for (Map.Entry<String, AbstractAction> entry : pool.entrySet()) {
                if (restBus.getHolder(entry.getKey()) != null) {
                    Misc.log("Rest", "override:", entry.getValue().getClass().getName(), "[" + entry.getKey() + "]");
                } else {
                    Misc.log("Rest", "register:", entry.getValue().getClass().getName(), "[" + entry.getKey() + "]");
                }
                restBus.registerOrUpdate(entry.getValue());
            }
            pool.clear();
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
                if (info.getType().startsWith(pckgname)) {
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
                Misc.err("Rest", "Directory does not exist:", directory);
            }
            return classes;
        } catch (Exception e) {
            Misc.err("Rest", e);
        }
        return Collections.emptyList();
    }

    protected EventBus<AbstractAction> getSystemBus() {
        return systemBus;
    }
}