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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Windows.<br>
 * It relies on a parunas.exe native tool that creates a process under a specific user.
 * <p>
 * This builder does not accept OSUser with a private key, only username and password
 * authentication is possible.
 * 
 * @since ProActive 4.4.0
 */
public final class WindowsProcessBuilder extends OSProcessBuilder {

    // path to tool
    private final String SCRIPTS_LOCATION;
    private static final String PARUNAS = "parunas.exe";

    /**
     * Creates a new instance of this class.
     */
    protected WindowsProcessBuilder(final OSUser user, final CoreBindingDescriptor cores, final String paHome) {
        super(user, cores);
        SCRIPTS_LOCATION = paHome + "\\dist\\scripts\\processbuilder\\win\\";
    }

    @Override
    public Boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        if (user.hasPrivateKey()) {
            // remove this when SSH support has been added to the product ;)
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
        if (!user.hasPassword()) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean isCoreBindingSupported() {
        return false;
    }

    @Override
    protected String[] wrapCommand() {
        // Wrap the user command into a call to parunas.exe    	
        List<String> wrappedCommand = new ArrayList<String>();
        wrappedCommand.add(SCRIPTS_LOCATION + PARUNAS);//"C:\\vbodnart\\workcosmos\\parunas\\Release\\parunas.exe");
        wrappedCommand.add("/u:" + super.user().getUserName());
        wrappedCommand.add("/d:.");
        // Inherit the working dir from the original process builder
        final File wdir = super.delegatedPB.directory();
        if (wdir != null) {
            try {
                wrappedCommand.add("/w:" + wdir.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wrappedCommand.addAll(super.delegatedPB.command());
        return wrappedCommand.toArray(new String[] {});
    }

    @Override
    protected void prepareEnvironment() throws FatalProcessBuilderException {
    }

    @Override
    protected void additionalCleanup() {
    }

    /**
     * The client process is created by the parunas.exe tool
     */
    @Override
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {

        final List<String> original = this.delegatedPB.command();
        final File wdir = this.delegatedPB.directory();

        // wrap user code in pretty wrapper scripts
        this.delegatedPB.command(wrapCommand());
        this.delegatedPB.directory(null);
        Process p;
        try {
            p = this.delegatedPB.start();
        } catch (IOException e) {
            this.delegatedPB.command(original);
            this.delegatedPB.directory(wdir);
            throw new FatalProcessBuilderException("Cannot launch because parunas.exe tool is missing!", e);
        }
        // set the command back as we found it :)
        this.delegatedPB.command(original);
        this.delegatedPB.directory(wdir);

        // Feed the stdin with the password                      
        final OutputStream stdin = p.getOutputStream();
        stdin.write((super.user().getPassword() + "\n").getBytes());
        stdin.flush();

        return p;
    }
}