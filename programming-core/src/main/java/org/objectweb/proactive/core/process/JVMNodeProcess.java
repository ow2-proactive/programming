/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
