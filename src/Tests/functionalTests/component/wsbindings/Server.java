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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.wsbindings;

public class Server implements Services, Service {
    public Server() {
    }

    public void doNothing() {
        System.out.println("doNothing called!");
    }

    public int incrementInt(int i) {
        return i + Services.INCREMENT_VALUE;
    }

    public double[] decrementArrayDouble(Double[] array) {
        double[] arrayResult = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            arrayResult[i] = array[i] - Services.DECREMENT_VALUE;
        }
        return arrayResult;
    }

    public String hello(String name) {
        return Services.HELLO_STRING + name;
    }

    public String[] splitString(String string) {
        return string.split(Services.SPLIT_REGEX);
    }

    public AnObject modifyObject(AnObject object) {
        object.setId(object.getId() + "Modified");
        object.setIntField(incrementInt(object.getIntField()));
        Double[] array = new Double[object.getArrayField().length];
        for (int i = 0; i < array.length; i++) {
            array[i] = object.getArrayField()[i];
        }
        object.setArrayField(decrementArrayDouble(array));
        AnObject object2 = new AnObject();
        object2.setId("Id" + ((int) (Math.random() * 100)));
        object.setObjectField(object2);
        return object;
    }

    public AnObject[] modifyArrayObject(AnObject[] arrayObject) {
        for (int i = 0; i < arrayObject.length; i++) {
            arrayObject[i] = modifyObject(arrayObject[i]);
        }
        return arrayObject;
    }

    public String modifyString(String string) {
        return string + " modified at: " + System.currentTimeMillis();
    }
}
