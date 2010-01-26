/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.debugger;

import java.io.Serializable;


public enum BreakpointType implements Serializable {

    /** set a breakpoint at the beginning of a new service */
    NewService("New Service", false),

    /** set a breakpoint before execution a method which is an immediate service */
    NewImmediateService("New Immediate Service", true),

    /** set a breakpoint at the end of a service */
    EndService("End Service", false),

    /** set a breakpoint at the end of an immediate service */
    EndImmediateService("End Immediate Service", true),

    /** set a breakpoint before sending the request to the target active object */
    SendRequest("Send Request");

    private String name;
    private boolean immediate;

    private BreakpointType(String name) {
        this(name, false);
    }

    private BreakpointType(String name, boolean isImmediate) {
        this.name = name;
        this.immediate = isImmediate;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public String toString() {
        return name;
    }
}
