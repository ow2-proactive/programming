/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.scilab.monitor;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.examples.scilab.MSEngine;


/**
 * MSEngineInfo contains all methods to access to informations about a Scilab Engine
 */
public class MSEngineInfo {
    private String idEngine;
    private String idCurrentTask;
    private MSEngine mSEngine;
    private BooleanWrapper isActivate; //a future to test if the Scilab engine is activated

    public MSEngineInfo(String idEngine, MSEngine mSEngine, BooleanWrapper isActivate) {
        this.idEngine = idEngine;
        this.mSEngine = mSEngine;
        this.isActivate = isActivate;
    }

    public String getIdEngine() {
        return idEngine;
    }

    public MSEngine getMSEngine() {
        return mSEngine;
    }

    public String getSciEngineUrl() {
        return PAActiveObject.getActiveObjectNodeUrl(this.mSEngine);
    }

    public BooleanWrapper getIsActivate() {
        return isActivate;
    }

    public void setIsActivate(BooleanWrapper isActivate) {
        this.isActivate = isActivate;
    }

    public String getIdCurrentTask() {
        return idCurrentTask;
    }

    public void setIdCurrentTask(String idCurrentTask) {
        this.idCurrentTask = idCurrentTask;
    }
}
