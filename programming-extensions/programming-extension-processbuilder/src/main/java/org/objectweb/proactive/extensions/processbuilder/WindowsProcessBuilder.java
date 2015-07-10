/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Windows.<br>
 * It relies on an internal implementation of the java.lang.Process interface used to
 * create process under a specific user.
 * <p>
 * This builder does not accept OSUser with a private key, only username and password
 * authentication is possible.
 * 
 * @since ProActive 5.0.0
 */
public final class WindowsProcessBuilder implements OSProcessBuilder {

    // the underlying ProcessBuilder to whom all work will be delegated
    // if no specified user
    protected final ProcessBuilder delegatedPB;

    // user - this should be a valid OS user entity (username and maybe a
    // password). The launched process will be run under this user's environment and rights.
    private final OSUser user;

    // descriptor of the core-binding (subset of cores on which the user's
    // process can execute)
    private final CoreBindingDescriptor cores;

    /**
     * Creates a new instance of this class.
     */
    protected WindowsProcessBuilder(final OSUser user, final CoreBindingDescriptor cores, final String paHome) {
        this.delegatedPB = new ProcessBuilder();
        // clean the env, the sub-process must not inherit the environment of the current process
        // it can still be modified
        this.delegatedPB.environment().clear();
        this.user = user;
        this.cores = cores;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#isCoreBindingSupported()
     */
    public boolean isCoreBindingSupported() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#start()
     */
    public Process start() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        Process p = null;

        if (this.user != null || this.cores != null) {
            // user or core binding is specified - do the fancy stuff
            p = setupAndStart();

        } else {
            // no extra service needed, just fall through to the delegated pb
            delegatedPB.environment().putAll(environment());
            p = delegatedPB.start();
        }

        return p;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#command()
     */
    public List<String> command() {
        return this.delegatedPB.command();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#command(java.lang.String[])
     */
    public OSProcessBuilder command(String... command) {
        this.delegatedPB.command(command);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#user()
     */
    public OSUser user() {
        return this.user;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#cores()
     */
    public CoreBindingDescriptor cores() {
        return this.cores;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#getAvaliableCoresDescriptor()
     */
    public CoreBindingDescriptor getAvailableCoresDescriptor() {
        return this.cores;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#directory()
     */
    public File directory() {
        return this.delegatedPB.directory();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#directory(java.io.File)
     */
    public OSProcessBuilder directory(File directory) {
        this.delegatedPB.directory(directory);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#environment()
     */
    public Map<String, String> environment() {
        //        if (this.user != null) {
        //            throw new NotImplementedException(
        //                "The environment modification of a user process is not implemented");
        //        }
        return this.delegatedPB.environment();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#redirectErrorStream()
     */
    public boolean redirectErrorStream() {
        return this.delegatedPB.redirectErrorStream();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#redirectErrorStream(boolean)
     */
    public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.delegatedPB.redirectErrorStream(redirectErrorStream);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder#canExecuteAsUser(org.objectweb.proactive.extensions.processbuilder.OSUser)
     */
    public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        if (user.hasPrivateKey()) {
            // remove this when SSH support has been added to the product ;)
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
        if (!user.hasPassword()) {
            return false;
        }
        return true;
    }

    /**
     * Create a native representation of a process that will run in background
     * that means no interaction with the desktop 
     */
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {

        // If the user doesn't have any domain use the local one
        String domain = ".";

        if (this.user().hasDomain()) {
            domain = this.user().getDomain();
        }

        // Create the windows process 
        final WindowsProcess p = new WindowsProcess(domain, this.user().getUserName(), this.user()
                .getPassword());

        // Inherit environment
        final Map<String, String> env = this.delegatedPB.environment();
        // Inherit the working dir from the original process builder
        final File wdir = this.delegatedPB.directory();
        final String path = (wdir == null ? null : wdir.getCanonicalPath());
        // Inherit the command from the original process builder
        final List<String> cmdList = this.delegatedPB.command();
        final String[] cmdArray = cmdList.toArray(new String[cmdList.size()]);

        // Start the sub-process and return it as a java.lang.Process implementation
        try {
            p.start(cmdArray, env, path);
        } catch (OSUserException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new FatalProcessBuilderException("Unexpected exception", e);
        }
        return p;
    }

    //    public static void main(String[] args) {
    //        WindowsProcessBuilder b = new WindowsProcessBuilder(new OSUser("tutu", "tutu"), null, null);
    //        String s = "C:\\Program Files\\Java\\jdk1.6.0_21\\\\jre\\bin\\java -Dproactive.scheduler.logs.maxsize=0 -Dproactive.configuration=file:C:\\vbodnart\\workspace12\\scheduling\\config\\scheduler\\forkedJavaTask\\forkedTask-paconf.xml -Djava.security.policy=file:C:\\vbodnart\\workspace12\\scheduling\\bin\\windows\\..\\../config/security.java.policy-client -Dlog4j.configuration=file:///C:\\vbodnart\\workspace12\\scheduling\\config\\scheduler\\forkedJavaTask\\forkedTask-log4j -D32 -cp .;.;C:\\vbodnart\\workspace12\\scheduling\\classes\\common;C:\\vbodnart\\workspace12\\scheduling\\classes\\resource-manager;C:\\vbodnart\\workspace12\\scheduling\\classes\\scheduler;;C:\\vbodnart\\workspace12\\scheduling\\lib\\ProActive\\ProActive.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\c-java-mysql-enterprise-plugin-1.0.0.42.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\commons-codec-1.3.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\commons-collections-3.2.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\derby.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\derbytools.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\isorelax.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\msv.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\mysql-connector-java-5.1.12-bin.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\mysql-connector-java-5.1.7-bin.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\relaxngDatatype.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\rngpack-1.1a.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\wstx-lgpl-3.9.2.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\xsdlib.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\jruby-engine.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\jruby.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\js.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\jsch-0.1.38.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\jython-engine.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\jython.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\script-api.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\common\\script\\script-js.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\annotation\\ejb3-persistence.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\annotation\\hibernate-annotations.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\annotation\\hibernate-commons-annotations.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\antlr-2.7.6.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\dom4j-1.6.1.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\geronimo-spec-jta-1.0.1B-rc4.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\hibernate-core.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\slf4j-api-1.5.6.jar;C:\\vbodnart\\workspace12\\scheduling\\lib\\hibernate\\core\\slf4j-log4j12-1.5.6.jar;C:\\vbodnart\\workspace12\\scheduling\\addons org.objectweb.proactive.core.runtime.StartPARuntime -p rmi://optimus.activeeon.com:1100/PA_JVM1151458586 -c 1 -d 679109";
    //        b.command(s);//"cmd.exe /c notepad.exe");
    //        try {
    //            Process p = b.start();
    //            Thread.sleep(35000);
    //
    //            p.destroy();
    //
    //            System.out.println("enclosing_type.enclosing_method() ----> exitValue " + p.exitValue());
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
}