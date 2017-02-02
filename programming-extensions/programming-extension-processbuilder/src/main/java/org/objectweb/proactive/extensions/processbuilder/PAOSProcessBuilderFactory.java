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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * An {@link OSProcessBuilderFactory} integrated with ProActive.
 * 
 * @since ProActive 5.0.0
 */
final public class PAOSProcessBuilderFactory implements OSProcessBuilderFactory {
    final private OperatingSystem os;

    final private String nativeScriptPath;

    public PAOSProcessBuilderFactory() throws ProActiveException {
        this.os = OperatingSystem.getOperatingSystem();
        this.nativeScriptPath = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
    }

    public PAOSProcessBuilderFactory(String nativeScriptPath) {
        this.os = OperatingSystem.getOperatingSystem();
        this.nativeScriptPath = nativeScriptPath;
    }

    public OSProcessBuilder getBuilder() {
        return this.getBuilder(null, null);
    }

    public OSProcessBuilder getBuilder(final OSUser user) {
        return this.getBuilder(user, null);
    }

    public OSProcessBuilder getBuilder(final CoreBindingDescriptor cores) {
        return this.getBuilder(null, cores);
    }

    public OSProcessBuilder getBuilder(final OSUser user, final CoreBindingDescriptor cores) {
        if (user == null) {
            return new BasicProcessBuilder();
        }
        switch (os) {
            case unix:
                return new LinuxProcessBuilder(user, cores, this.nativeScriptPath);
            case windows:
                return new WindowsProcessBuilder(user, cores, this.nativeScriptPath);
            default:
                throw new NotImplementedException("The process builder is not yet implemented on " + os);
        }
    }
}
