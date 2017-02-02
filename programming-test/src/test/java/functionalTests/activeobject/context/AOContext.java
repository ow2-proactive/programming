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
                                  (r.getSourceBodyID().equals(caller)) &&
                                  (r.getMethodName().equals("standardService")));
    }

    public BooleanWrapper immediateService(UniqueID caller) {
        Context current = PAActiveObject.getContext();
        Request r = current.getCurrentRequest();
        return new BooleanWrapper((r != null) && (current.getBody().getID().equals(myID)) &&
                                  (r.getSourceBodyID().equals(caller)) &&
                                  (r.getMethodName().equals("immediateService")));
    }

    public BooleanWrapper test(AOContext a) {
        return new BooleanWrapper((a.standardService(this.myID).getBooleanValue()) &&
                                  (a.immediateService(this.myID)).getBooleanValue());
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
