/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.componentcontroller;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Base class for all controller components.
 *
 * @author The ProActive Team
 *
 */
public abstract class AbstractProActiveComponentController implements Serializable, HostComponentSetter {
    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    private boolean isInternal = true;
    private InterfaceType interfaceType;
    protected static Logger controllerLogger = ProActiveLogger.getLogger(Loggers.COMPONENTS_CONTROLLERS);
    ProActiveComponent hostComponent;

    /**
     * Constructor for AbstractProActiveComponentController.
     *
     */
    public AbstractProActiveComponentController() {
    }

    public void setHostComponent(Component hostComponent) {
        this.hostComponent = (ProActiveComponent) hostComponent;
    }
}
