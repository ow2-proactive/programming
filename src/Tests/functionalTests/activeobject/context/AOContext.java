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
package functionalTests.activeobject.context;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.exceptions.HalfBodyException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class AOContext implements java.io.Serializable {
    private UniqueID myID;
    private StringWrapper name;
    private AOContext stubOnCaller;

    public boolean init(String name) {
        this.myID = PAActiveObject.getBodyOnThis().getID();
        PAActiveObject.setImmediateService("immediateService");
        this.name = new StringWrapper(name);
        return (this.myID != null);
    }

    public BooleanWrapper standardService(UniqueID caller) {
        Context current = PAActiveObject.getContext();
        Request r = current.getCurrentRequest();
        return new BooleanWrapper((r != null) && (current.getBody().getID().equals(myID)) &&
            (r.getSourceBodyID().equals(caller)) && (r.getMethodName().equals("standardService")));
    }

    public BooleanWrapper immediateService(UniqueID caller) {
        Context current = PAActiveObject.getContext();
        Request r = current.getCurrentRequest();
        return new BooleanWrapper((r != null) && (current.getBody().getID().equals(myID)) &&
            (r.getSourceBodyID().equals(caller)) && (r.getMethodName().equals("immediateService")));
    }

    public BooleanWrapper test(AOContext a) {
        return new BooleanWrapper((a.standardService(this.myID).booleanValue()) &&
            (a.immediateService(this.myID)).booleanValue());
    }

    // test stub on caller
    public int initTestStubOnCaller(AOContext a) {
        a.setStubOnCaller();
        return 0;
    }

    public void setStubOnCaller() {
        this.stubOnCaller = (AOContext) PAActiveObject.getContext().getStubOnCaller();
    }

    public StringWrapper getCallerName() {
        return this.stubOnCaller.getName();
    }

    public StringWrapper getName() {
        return this.name;
    }

    public BooleanWrapper testHalfBodyCaller() {
        Context c = PAActiveObject.getContext();
        try {
            // caller is a halfbody
            Object o = c.getStubOnCaller();
            o.toString();
        } catch (HalfBodyException e) {
            return new BooleanWrapper(true);
        }
        return new BooleanWrapper(false);
    }
}
