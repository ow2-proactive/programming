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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * <p>
 * A class implementing this interface is a factory of request objects.
 * It is able to create Request tailored for a particular purpose.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/12/23
 * @since   ProActive 0.91
 *
 */
public interface RequestFactory {

    /**
     * Creates a request object based on the given parameter
     * @return the newly created Request object.
     */
    public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID,
            MessageTags tags);
}
