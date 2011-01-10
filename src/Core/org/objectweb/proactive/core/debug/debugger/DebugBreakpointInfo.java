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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.debugger;

import java.io.Serializable;


public class DebugBreakpointInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
    private BreakpointType breakpointType;
    private String threadName;
    private String methodName;
    private long breakpointId;

    public DebugBreakpointInfo(BreakpointInfo breakpointInfo) {
        breakpointType = breakpointInfo.getType();
        threadName = breakpointInfo.getThread().getName();
        if (breakpointInfo.getRequest() != null) {
            methodName = breakpointInfo.getRequest().getMethodName();
        }
        breakpointId = breakpointInfo.getBreakpointId();
    }

    public BreakpointType getBreakpointType() {
        return breakpointType;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isImmediate() {
        return breakpointType.isImmediate();
    }

    public long getBreakpointId() {
        return breakpointId;
    }

}