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
