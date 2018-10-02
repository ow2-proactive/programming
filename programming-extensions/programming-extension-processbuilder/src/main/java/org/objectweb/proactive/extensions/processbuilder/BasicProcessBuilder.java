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
package org.objectweb.proactive.extensions.processbuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
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

    public CoreBindingDescriptor getAvailableCoresDescriptor() {
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

    public Process start() throws IOException, OSUserException, CoreBindingException, FatalProcessBuilderException {
        return new BasicProcess(this.pb.start());

    }

    public final class BasicProcess extends Process {
        final private Process process;

        public BasicProcess(Process p) {
            this.process = p;
        }

        @Override
        public void destroy() {
            process.destroy();
        }

        @Override
        public OutputStream getOutputStream() {
            return this.process.getOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return this.process.getInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return this.process.getErrorStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return this.process.waitFor();
        }

        @Override
        public int exitValue() {
            return this.process.exitValue();
        }
    }
}
