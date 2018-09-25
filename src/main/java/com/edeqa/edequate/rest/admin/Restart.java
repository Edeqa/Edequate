package com.edeqa.edequate.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;


@SuppressWarnings("unused")
public class Restart extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/restart";

    private static final String RESTART = "restart";
    private static final String STOP = "stop";

    private String info;
    private String script;
    private boolean buttons;
    private JSONObject options;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {

        setOptions(request.fetchOptions());

        if(getOptions().has(RESTART)) {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_STRING);
            json.put(MESSAGE, "Restarting");

            try {
                restartApplication(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(getOptions().has(STOP)) {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_STRING);
            json.put(MESSAGE, "Stopping");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }).start();

        } else {
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_METHOD_NOT_ALLOWED);
            json.put(MESSAGE, "Not enough arguments");
            Misc.err("Api", "failed because of not enough arguments");
        }
    }

    public static void restartApplication(Runnable runBeforeRestart) throws IOException {
        try {
// java binary
            String java = System.getProperty("java.home") + "/bin/java";
// vm arguments
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            StringBuilder vmArgsOneLine = new StringBuilder();
            for (String arg : vmArguments) {
                if (!arg.contains("-agentlib") && !arg.contains("-javaagent")) {
                    vmArgsOneLine.append("\"").append(arg).append("\" ");
                }
            }
            if(java.contains(" ")) java = "\"" + java + "\"";
            final StringBuilder cmd = new StringBuilder(java + " " + vmArgsOneLine);

            String[] mainCommand = System.getProperty("sun.java.command").split(" ");
            if (mainCommand[0].endsWith(".jar")) {
                cmd.append("-jar ").append(new File(mainCommand[0]).getPath());
            } else {
                cmd.append("-cp \"").append(System.getProperty("java.class.path")).append("\" ").append(mainCommand[0]);
            }
            for (int i = 1; i < mainCommand.length; i++) {
                cmd.append(" ");
                cmd.append(mainCommand[i]);
            }

            if(!cmd.toString().matches("\\.log")) {
                String logFileName = ((Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE)).getLogFile();
                cmd.append(" &> \"").append(new WebPath(logFileName).path().getCanonicalPath()).append("\"");
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println(cmd);
                    Runtime.getRuntime().exec(cmd.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            // execute some custom code before restarting
            if (runBeforeRestart!= null) {
                runBeforeRestart.run();
            }
            System.exit(0);
        } catch (Exception e) {
            throw new IOException("Error while trying to restart the application", e);
        }
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public JSONObject getOptions() {
        return options;
    }
}
