/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.gcmdeployment;

import java.io.FileNotFoundException;
import java.net.URL;


public abstract class LocalHelpers {

    static public URL getDescriptor(Class<?> cl) throws FileNotFoundException {
        String classname = cl.getSimpleName();
        System.out.println(classname);
        URL resource = cl.getResource(classname + ".xml");
        //        if (!(desc.exists() && desc.isFile() && desc.canRead())) {
        //            throw new FileNotFoundException(desc.getAbsolutePath());
        //        }

        return resource;
    }

    static public URL getDescriptor(Object o) throws FileNotFoundException {
        return getDescriptor(o.getClass());
    }

    static public void waitAllocation() {
        wait(10000);
    }

    static public void wait(int sec) {
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
