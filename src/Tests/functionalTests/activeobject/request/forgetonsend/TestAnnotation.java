/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.activeobject.request.forgetonsend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;

import static junit.framework.Assert.assertTrue;


/**
 * Test ForgetOnSend strategies on SPMD groups
 */

public class TestAnnotation extends FunctionalTest {

    private B b1, b2;

    /**
     * Check if @Sterile annotation is taken into account for distinction
     * between local wait and global wait
     */
    @Test
    public void ptpAnnotation() {
        try {
            b1 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] { "B1" });
            b2 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] { "B2" });
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        PAActiveObject.setForgetOnSend(b1, "h");
        PAActiveObject.setForgetOnSend(b2, "f");
        PAActiveObject.setForgetOnSend(b2, "i");

        b1.setAsImmediate("takeFast");
        b2.setAsImmediate("takeFast");

        b1.a();
        b2.a();

        b1.h(new SlowlySerializableObject("test", 200)); // slow fos
        b2.i(new SlowlySerializableObject("test", 400)); // slow fos
        b1.g(); // standard, sterile annotation (=> only waits on b1)
        b1.e(); // standard, without annotation (=> global wait)

        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        Assert.assertEquals("aahgie", b1.takeFast());
    }
}