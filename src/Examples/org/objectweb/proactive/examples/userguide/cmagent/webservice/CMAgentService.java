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
//@snippet-start webservice_cma_full
//@snippet-start webservice_cma_skeleton
package org.objectweb.proactive.examples.userguide.cmagent.webservice;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


@ActiveObject
public class CMAgentService extends CMAgentInitialized {

    // we cannot use the getLastRequestServeTime() method
    // directly since LongWrapper is not recognize
    // TODO: See why
    public long waitLastRequestServeTime() {
        return this.getLastRequestServeTime().longValue();
    }

    public static void main(String[] args) {

        String url = "";
        String wsFrameWork = "";
        if (args.length == 1) {
            url = AbstractWebServicesFactory.getLocalUrl();
            wsFrameWork = args[0];
        } else if (args.length == 2) {
            url = args[0];
            wsFrameWork = args[1];
        } else {
            System.out.println("Wrong number of arguments:");
            System.out.println("Usage: java CMAgentService [url] wsFrameWork");
            System.out.println("where wsFrameWork is either 'axis2' or 'cxf'");
            return;
        }

        System.out.println("Started a monitoring agent on : " + url);

        CMAgentService hw;
        try {
            hw = (CMAgentService) PAActiveObject.newActive(
                    "org.objectweb.proactive.examples.userguide.cmagent.webservice.CMAgentService",
                    new Object[] {});

            // TODO 1.Expose as web service (on URL 'url') the methods
            // "getLastRequestServeTime" and "getCurrentState" 
            // of 'hw' CMAgentService.
            // Name your service  "cmAgentService" and use the web service framework given
            // in argument.

            //@snippet-break webservice_cma_skeleton
            //@tutorial-break
            //@snippet-start ws_call
            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(wsFrameWork);
            WebServices ws = wsf.getWebServices(url);
            ws.exposeAsWebService(hw, "cmAgentService", new String[] { "waitLastRequestServeTime",
                    "getCurrentState" });
            //@snippet-end ws_call
            //@tutorial-resume
            //@snippet-resume webservice_cma_skeleton
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WebServicesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
//@snippet-end webservice_cma_skeleton
//@snippet-end webservice_cma_full
//@tutorial-end
