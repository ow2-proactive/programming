package org.objectweb.proactive.extensions.processbuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Linux.<br>
 * It relies on scritps for custom launching of the user command.
 * 
 * @author Zsolt Istvan
 * 
 */
public class WindowsProcessBuilder extends OSProcessBuilder {

    private Map<String, String> environmentMap = new HashMap<String, String>();

    // path to scripts
    private final String SCRIPTS_LOCATION;
    private static final String CHECK_RUNAS = "check_runas.bat";
    private static final String LAUNCH_SCRIPT = "launch.bat";
    private static final String LAUNCH_EXE = "PipeBridge.exe";
    // other constants
    private static final String ENV_VAR_USER_PASSWORD = "PA_OSPB_USER_PASSWORD";

    private String envFile = "_";

    public WindowsProcessBuilder(String paHome) {
        SCRIPTS_LOCATION = paHome + "\\dist\\scripts\\processbuilder\\win\\";
    }

    @Override
    public Map<String, String> environment() {
        // TODO Auto-generated method stub
        return this.environmentMap;
    }

    @Override
    public Boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        if (user.hasPrivateKey()) {
            //remove this when SSH support has been added to the product ;)
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
        try {
            String[] args = { SCRIPTS_LOCATION + CHECK_RUNAS, user.getUserName(),
                    (user.hasPassword()) ? user.getPassword() : "",
                    (user.hasPassword()) ? SCRIPTS_LOCATION : "" };
            Process p;

            try {
                // running a script that sudo-s into user and runs whoami
                p = Runtime.getRuntime().exec(args);
            } catch (IOException e) {
                throw new FatalProcessBuilderException("Cannot launch because scripts are missing!", e);
            }

            InputStream inputstream = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(isr);
            String line;

            while ((line = bufferedreader.readLine()) != null) {
                // if whoami returns the same user name as user(), we are ok to
                // go
                if (line.equals(user.getUserName())) {
                    bufferedreader.close();
                    return true;
                }
            }

            bufferedreader.close();
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean isCoreBindingSupported() {
        return false;
    }

    @Override
    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return null;
    }

    @Override
    protected String[] wrapCommand() {
        String uname = (user() == null) ? "_" : user().getUserName();
        String passwSwitch = (user() != null && user().hasPassword()) ? "p" : "_";
        String cpart = (cores() == null) ? "_" : cores().toString();
        String wpath = (directory() == null) ? "_" : directory().getAbsolutePath();
        ArrayList<String> icmd = new ArrayList<String>();
        // under windows the launcher needs:
        // [exe_to_read_pipes] [launch_script] [pipename_for_script (added by
        // pipereader exe)]
        // [script_loc] [workspace] [username] [cores] [command...
        icmd.add(SCRIPTS_LOCATION + LAUNCH_EXE);
        icmd.add(SCRIPTS_LOCATION + LAUNCH_SCRIPT);
        icmd.add(SCRIPTS_LOCATION);
        icmd.add(wpath);
        icmd.add(envFile);
        icmd.add(cpart);
        icmd.add(uname);
        icmd.add(passwSwitch);

        // do this god-awful hack to work around the limitations of batch
        // scripts...
        // when using the paths be sure to switch back to space.
        int size = icmd.size();
        for (int i = 2; i < size; i++) {
            icmd.set(i, icmd.get(i).replace(" ", "?"));
        }

        return icmd.toArray(new String[0]);
    }

    /**
     * This method will create a file which will contain all variables from the
     * {@link #environment()}, and return the path to it. <br>
     * In case the environment is empty, it will return "_" which is the
     * "argument not set" value for the windows scripts. <br>
     * <br>
     * ATTENTION: "Environment" here means only variables explicitly set by the
     * user.
     * 
     * @return Path to a bat file or "_" in case there is no file.
     */
    protected String writeAndGetEnvironmentFile() throws FatalProcessBuilderException {
        if (environment().size() > 0 || command().size() > 0) {
            try {
                File temp = new File(SCRIPTS_LOCATION + "userenv" + UUID.randomUUID() + ".env");
                temp.createNewFile();
                temp.deleteOnExit();

                BufferedWriter out;

                out = new BufferedWriter(new FileWriter(temp));
                // this file will have the environment variables and their
                // values listed in the following way:
                // env_var1=value1
                // env_var2=value2
                // ...
                for (String i : environment().keySet()) {
                    out.write("" + i + "=" + environment().get(i) + "\n");
                }

                //if (user()!=null && user().hasPassword()) {
                out.write("cmd_coded=" + command().get(0).replace("%", "^%") + "\n");
                out.write("therest=");
                int limit = command().size();
                for (int i = 1; i < limit; i++) {
                    out.write(command().get(i).replace("%", "^%") + ((i < limit - 1) ? " " : ""));
                }
                out.write("\n");
                //}

                out.close();
                return temp.getCanonicalPath();
            } catch (IOException e) {
                throw new FatalProcessBuilderException("Could not write environment to temporary file!", e);
            }
        }
        return "_";
    }

    @Override
    protected void prepareEnvironment() throws FatalProcessBuilderException {
        /*
         * Before running the actual setupAndStart method, we write out the variables to a temp
         * file, and store its value in {@link #envFile}. Then we include this path in the wrapper
         * for the user's command.
         */
        envFile = writeAndGetEnvironmentFile();

        //do this AFTER writing the environment, because otherwise you dump the password to a file :D
        if (user() != null && user().hasPassword()) {
            delegatedPB.environment().put(ENV_VAR_USER_PASSWORD, user().getPassword());
        }

        if (user() != null && user().hasPrivateKey()) {
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
    }

    @Override
    protected void additionalCleanup() {
        delegatedPB.environment().remove(ENV_VAR_USER_PASSWORD);
    }

}
