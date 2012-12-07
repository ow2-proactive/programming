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
package org.objectweb.proactive.extensions.processbuilder.exception;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This exception is used to signal an internal error encountered by the OSProcessBuilder while
 * preparing for the command to run. By internal we mean a problem related to the scripts and
 * other resources used by the process builder.
 * <p>
 * This error comes in several flavors:
 * <ul>
 *  <li>Lack of output from scripts - This error can have different causes on different 
 *  operating systems, however it will usually be related to stream redirection, or piping
 *  issues.
 *  </li>
 *  <li>Corruption of scripts - Which means that the scripts are not respecting the messaging
 *  interface. Typos and version mismatches between scripts and class files can cause this failure.
 *  </li>
 *  <li>Inability to launch - Other reasons</li>
 * </ul>
 * </p>
 * @author The ProActive Team
 * 
 * @since ProActive 5.0.0
 */
@PublicAPI
public class FatalProcessBuilderException extends Exception implements Serializable {

    public FatalProcessBuilderException(String descr) {
        super(descr);
    }

    public FatalProcessBuilderException(String descr, Throwable cause) {
        super(descr, cause);
    }

}
