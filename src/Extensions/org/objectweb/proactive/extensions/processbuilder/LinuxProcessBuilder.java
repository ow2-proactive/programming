/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.processbuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Linux.<br>
 * It relies on scritps for custom launching of the user command.
 * 
 * @author Zsolt Istvan
 * 
 */
public class LinuxProcessBuilder extends OSProcessBuilder {

    // path to scripts
    private final String SCRIPTS_LOCATION;
    private static final String CHECK_SUDO = "check_sudo.sh";
    private static final String LAUNCH_SCRIPT = "launch.sh";
    //other constants
    private static final String ENV_VAR_USER_PASSWORD = "PA_OSPB_USER_PASSWORD";
    private static final String ENV_VAR_USER_KEY_CONTENT = "PA_OSPB_USER_KEY_CONTENT";

    public LinuxProcessBuilder(String paHome) {
        SCRIPTS_LOCATION = paHome + "/dist/scripts/processbuilder/linux/";
    }

    @Override
    public Boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        try {
            String[] args = { SCRIPTS_LOCATION + CHECK_SUDO, user.getUserName(),
                    (user.hasPassword()) ? SCRIPTS_LOCATION : "" };
            Process p;

            try {
                // running a script that sudo-s into user and runs whoami
                String[] environment = {
                        ENV_VAR_USER_PASSWORD + "=" + ((user.hasPassword()) ? user.getPassword() : ""),
                        ENV_VAR_USER_KEY_CONTENT + "=" +
                            ((user.hasPrivateKey()) ? String.copyValueOf(user.getPrivateKey()) : "") };
                p = Runtime.getRuntime().exec(args, environment);
            } catch (IOException e) {
                additionalCleanup();
                throw new FatalProcessBuilderException("Cannot launch because scripts are missing!", e);
            }

            additionalCleanup();
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
                } else {
                    try {
                        createAndThrowException(line);
                    } catch (FatalProcessBuilderException fpbe) {
                        throw fpbe;
                    } catch (Exception e) {
                        throw new FatalProcessBuilderException("Cannot launch!", e);
                    }
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
        String uname = (user() == null) ? "" : user().getUserName();
        String cpart = (cores() == null) ? "" : cores().toString();
        String wpath = (directory() == null) ? "" : directory().getAbsolutePath();
        ArrayList<String> icmd = new ArrayList<String>();
        // under linux the launcher needs:
        // [script folder] [ABS_PATH_TO_WORKDIR] [USERNAME] [CORES] [COMMAND...
        icmd.add(SCRIPTS_LOCATION + LAUNCH_SCRIPT);
        icmd.add(SCRIPTS_LOCATION);
        icmd.add(wpath);
        icmd.add(uname);
        icmd.add(cpart);
        icmd.addAll(command());

        if (user() != null && (user().hasPassword() || user().hasPrivateKey())) {
            for (int i = 1; i < 5; i++)
                if (icmd.get(i).contains(" ")) {
                    icmd.set(i, "'" + icmd.get(i).replace("'", "'\"'\"'") + "'");
                }
        }

        return icmd.toArray(new String[0]);
    }

    @Override
    protected void prepareEnvironment() throws FatalProcessBuilderException {
        /*
         * In case we attempt to launch a command under an other user, and we want to use a
         * password, it has to be passed to the scripts through the environment variable {@value
         * #ENV_VAR_USER_PASSWORD}. <br> So before doing the actual launching, we set the env.
         * variable; and after the process has started, we unset the variable, so no one can read it
         * through the inner process builder's environment() method.
         */
        if (user() != null) {
            if (user().hasPrivateKey()) {
                delegatedPB.environment().put(ENV_VAR_USER_KEY_CONTENT,
                        String.copyValueOf(user().getPrivateKey()));
            }

            if (user().hasPassword()) {
                delegatedPB.environment().put(ENV_VAR_USER_PASSWORD, user().getPassword());
            }
        }

    }

    @Override
    protected void additionalCleanup() {
        delegatedPB.environment().remove(ENV_VAR_USER_PASSWORD);
        delegatedPB.environment().remove(ENV_VAR_USER_KEY_CONTENT);
    }

}
