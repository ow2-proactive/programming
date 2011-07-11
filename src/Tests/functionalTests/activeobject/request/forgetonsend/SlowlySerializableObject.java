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
package functionalTests.activeobject.request.forgetonsend;

import java.io.Serializable;


/**
 * This class offers a way to simulate a heavy object through a sleep in serialization
 */
public class SlowlySerializableObject implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    private long tts;
    private String name;
    private int vmSerializer;

    public SlowlySerializableObject(String name, long timeToSerializeInMillis) {
        this.name = name;
        this.tts = timeToSerializeInMillis;
        this.vmSerializer = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVmSerializer() {
        return vmSerializer;
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION
    // -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            Thread.sleep(tts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.defaultWriteObject();
        out.writeInt(Runtime.getRuntime().hashCode());
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.vmSerializer = in.readInt();
    }
}
