/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package functionalTests.component;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 */
public class PrimitiveComponentC implements I1, BindingController {
    private final static Logger logger = ProActiveLogger.getLogger("functionalTestss.components");
    public final static String MESSAGE = "-->c";
    public final static String I1_CLIENT_ITF_NAME = "i1-client";
    I1 i1Client;

    /**
     *
     */
    public PrimitiveComponentC() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.UserBindingController#addFcBinding(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) {
        if (clientItfName.equals(I1_CLIENT_ITF_NAME)) {
            i1Client = (I1) serverItf;
            //logger.debug("MotorImpl : added binding on a wheel");
        } else {
            logger.error("no such binding is possible : client interface name does not match");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.UserBindingController#getFcBindings(java.lang.String)
     */
    public Object getFcBindings(String arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.UserBindingController#removeFcBinding(java.lang.String,
     *      java.lang.Object)
     */
    public void removeFcBinding(String clientItfName, Object serverItf) {
        if (clientItfName.equals(I1_CLIENT_ITF_NAME)) {
            if (serverItf.equals(i1Client)) {
                i1Client = null;
                logger.debug("removed binding on i2");
            } else {
                logger.error("server object does not match");
            }
        } else {
            logger.error("client interface name does not match");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see functionalTests.component.creation.Input#processInputMessage(java.lang.String)
     */
    public Message processInputMessage(Message message) {
        //		/logger.info("transferring message :" + message.toString());
        if (i1Client != null) {
            return (i1Client.processInputMessage(message.append(MESSAGE))).append(MESSAGE);
        } else {
            logger.error("cannot forward message (binding missing)");
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { I1_CLIENT_ITF_NAME };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
        if (clientItf.equals(I1_CLIENT_ITF_NAME)) {
            return i1Client;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot find " + I1_CLIENT_ITF_NAME + " interface");
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (clientItf.equals(I1_CLIENT_ITF_NAME)) {
            i1Client = null;
            if (logger.isDebugEnabled()) {
                logger.debug(I1_CLIENT_ITF_NAME + " interface unbound");
            }
        }
    }
}
