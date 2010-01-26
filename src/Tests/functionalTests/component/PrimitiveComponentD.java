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
package functionalTests.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 */
public class PrimitiveComponentD implements I1Multicast, BindingController {
    protected final static Logger logger = ProActiveLogger.getLogger("functionalTestss.components");
    public final static String MESSAGE = "-->d";

    //public final static Message MESSAGE = new Message("-->PrimitiveComponentD");
    public final static String I2_ITF_NAME = "i2";

    // typed collective interface
    Map<String, I2> i2Map = new HashMap<String, I2>();

    public PrimitiveComponentD() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.UserBindingController#addFcBinding(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) throws IllegalBindingException {
        if (clientItfName.startsWith(I2_ITF_NAME) && !clientItfName.equals(I2_ITF_NAME)) {
            // conformance to the Fractal API
            i2Map.put(clientItfName, (I2) serverItf);
        } else {
            throw new IllegalBindingException("Binding impossible : wrong client interface name (" +
                serverItf + ")");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see functionalTests.component.creation.Input#processInputMessage(java.lang.String)
     */
    public List<Message> processInputMessage(Message message) {
        //		logger.info("transferring message :" + message.toString());
        List<Message> listResultMsg = new ArrayList<Message>();
        if (!i2Map.isEmpty()) {
            message.append(MESSAGE);
            for (I2 itf : i2Map.values()) {
                listResultMsg.add(itf.processOutputMessage(message).append(MESSAGE));
            }
        } else {
            Assert.fail("cannot forward message (binding missing)");
            message.setInvalid();
            listResultMsg.add(message);
        }
        return listResultMsg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        Set<String> itf_names = i2Map.keySet();
        return (String[]) itf_names.toArray(new String[itf_names.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (i2Map.containsKey(clientItfName)) {
            return i2Map.get(clientItfName);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (clientItfName.startsWith(I2_ITF_NAME)) {
            i2Map.remove(clientItfName);
            if (logger.isDebugEnabled()) {
                logger.debug(clientItfName + " interface unbound");
            }
        } else {
            throw new NoSuchInterfaceException("client interface not found");
        }
    }
}
