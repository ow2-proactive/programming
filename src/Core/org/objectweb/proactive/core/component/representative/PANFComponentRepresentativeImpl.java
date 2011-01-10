/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.component.representative;

import org.objectweb.fractal.api.type.ComponentType;


/**
 * An object of type <code> Component  </code> which is a remote reference on a
 * component. <br>
 * When creating an active object of type <code> A  </code>, you get a reference
 * on the active object through a dynamically generated stub of type
 * <code> A  </code>. Similarly, when creating a component, you get a reference
 * on an object of type <code> Component  </code>, in other words an instance of
 * this class. Ini this case, this class represents a non functional component. It is marked by the ProActiveNFComponentRepresentative interface.
 * <p>
 * During the construction of an instance of this class, references to
 * interfaces of the component are also dynamically generated : references to
 * functional interfaces corresponding to the server interfaces of the
 * component, and references to control interfaces. The idea is to save remote
 * invocations : when requesting a controller or an interface, the generated
 * corresponding interface is directly returned. Then, invocations on this
 * interface are reified and transferred to the actual component. <br>
 *
 * @author The ProActive Team
 */
public class PANFComponentRepresentativeImpl extends PAComponentRepresentativeImpl implements
        PANFComponentRepresentative {
    /**
     * 
     */
    private static final long serialVersionUID = 500L;

    public PANFComponentRepresentativeImpl(ComponentType componentType, String hierarchicalType,
            String controllersConfigFileLocation) {
        super(componentType, hierarchicalType, controllersConfigFileLocation);
    }
}
