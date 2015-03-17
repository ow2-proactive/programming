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
package org.objectweb.proactive.core.process;

import java.io.Serializable;

import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * This class has the same functionalities than JVMProcess, except that the class associated with this process
 * ie the class that this process will start when the <code>startProcess()</code> is called, is set automatically to
 * <code>org.objectweb.proactive.core.runtime.startRuntime</code>.This class is mainly used with XML deployment descriptor.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class JVMNodeProcess extends JVMProcessImpl implements Serializable {

    private static final long serialVersionUID = 61L;

    /**
     * Creates a new instance of JVMNodeProcess.
     */
    public JVMNodeProcess() {
        this(new StandardOutputMessageLogger());
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }

    /**
     * Creates a new instance of JVMNodeProcess
     * @param messageLogger The logger that handles input and error stream of this process
     */
    public JVMNodeProcess(RemoteProcessMessageLogger messageLogger) {
        super(messageLogger);
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }

    /**
     * Creates a new instance of JVMNodeProcess
     * @param inputMessageLogger The logger that handles input stream of this process
     * @param errorMessageLogger The logger that handles error stream of this process
     */
    public JVMNodeProcess(RemoteProcessMessageLogger inputMessageLogger,
            RemoteProcessMessageLogger errorMessageLogger) {
        super(inputMessageLogger, errorMessageLogger);
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }
}
