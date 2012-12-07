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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Factory class for {@link OSProcessBuilder} which will produce a process
 * builder that is compatible with the underlying operating system.
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
@PublicAPI
public interface OSProcessBuilderFactory {
    /**
     * Creates a new instance of {@link OSProcessBuilder}
     * @return the os specific process builder
     */
    public OSProcessBuilder getBuilder();

    /**
     * Creates a new instance of {@link OSProcessBuilder} for
     * processes that runs under the specified user.
     * @param user the process user
     * @return the os specific process builder 
     */
    public OSProcessBuilder getBuilder(OSUser user);

    /**
     * Creates a new instance of {@link OSProcessBuilder} for
     * processes that runs with the specified cores mapping.
     * @param cores the cores mapping
     * @return the os specific process builder 
     */
    public OSProcessBuilder getBuilder(CoreBindingDescriptor cores);

    /**
     * Creates a new instance of {@link OSProcessBuilder} for
     * processes that runs under the specified user and cores mapping.
     * @param user the process user
     * @param cores the cores mapping
     * @return the os specific process builder 
     */
    public OSProcessBuilder getBuilder(OSUser user, CoreBindingDescriptor cores);

}
