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
package functionalTests.activeobject.acontinuation;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;


public class AOAContinuation implements java.io.Serializable {

    /**
     *
     */
    boolean isFuture = true;

    private AOAContinuation deleguate;

    Id id;

    Id idSent;

    public AOAContinuation() {
    }

    public AOAContinuation(String name) {
        this.id = new Id(name);
    }

    public void initFirstDeleguate() throws Exception {
        this.deleguate = PAActiveObject.newActive(AOAContinuation.class, new Object[] { "deleguate1" });
        deleguate.initSecondDeleguate();
    }

    public void initSecondDeleguate() throws Exception {
        this.deleguate = PAActiveObject.newActive(AOAContinuation.class, new Object[] { "deleguate2" });
    }

    public Id getId(String name) {
        if (id.getName().equals(name)) {
            return id;
        } else {
            return deleguate.getInternalId(name);
        }
    }

    public Id getInternalId(String name) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return deleguate.getId(name);
    }

    public Id getIdforFuture() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }

    public AOAContinuation getA(AOAContinuation a) {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a;
    }

    public AOAContinuation delegatedGetA(AOAContinuation a) {
        return this.deleguate.getA(a);
    }

    public void forwardID(Id id) {
        if (deleguate != null) {
            deleguate.forwardID(id);
        }

        isFuture = PAFuture.isAwaited(id);
        idSent = id;
    }

    public boolean isSuccessful() {
        if (!isFuture) {
            return false;
        }

        if (deleguate != null) {
            return deleguate.isSuccessful();
        }

        return isFuture;
    }

    public String getFinalResult() {
        if (deleguate != null) {
            return deleguate.getFinalResult();
        } else {
            return idSent.getName();
        }
    }

    public String getIdName() {
        return id.getName();
    }
}
