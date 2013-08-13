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
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that wraps around {@link java.lang.ProcessBuilder}.<br>
 * It has no additional capabilities.
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
public class BasicProcessBuilder implements OSProcessBuilder {

    final private ProcessBuilder pb;

    public BasicProcessBuilder() {
        this.pb = new ProcessBuilder();
    }

    public List<String> command() {
        return this.pb.command();
    }

    public OSProcessBuilder command(String... command) {
        this.pb.command(command);
        return this;
    }

    public OSUser user() {
        return null;
    }

    public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        return false;
    }

    public CoreBindingDescriptor cores() {
        return null;
    }

    public boolean isCoreBindingSupported() {
        return false;
    }

    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return null;
    }

    public File directory() {
        return this.pb.directory();
    }

    public OSProcessBuilder directory(File directory) {
        this.pb.directory(directory);
        return this;
    }

    public Map<String, String> environment() {
        return this.pb.environment();
    }

    public boolean redirectErrorStream() {
        return this.pb.redirectErrorStream();
    }

    public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.pb.redirectErrorStream(redirectErrorStream);
        return this;
    }

    public Process start() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        return this.pb.start();
    }
}
