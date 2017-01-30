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
