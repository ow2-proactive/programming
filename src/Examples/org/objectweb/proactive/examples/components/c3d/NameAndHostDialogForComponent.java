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
package org.objectweb.proactive.examples.components.c3d;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.naming.NamingException;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.examples.c3d.Dispatcher;
import org.objectweb.proactive.examples.c3d.gui.NameAndHostDialog;


/** Overide the AO NameAndHostDialog, to use proper conponent lookup. */
public class NameAndHostDialogForComponent extends NameAndHostDialog {
    private static final String COMPONENT_ALIAS = "Dispatcher";

    public NameAndHostDialogForComponent() {
        super();
    }

    /** Try to find a dispatcher, using the component methods, overiding the AO initial lookup.  */
    @Override
    protected void tryTheLookup() {
        String hostName = null;
        try {
            hostName = URIBuilder.getHostNameFromUrl(hostNameTextField.getText());
            int portNumber = URIBuilder.getPortNumber(hostNameTextField.getText());

            String protocol = URIBuilder.getProtocol(hostNameTextField.getText());

            //@snippet-start C3D_Components_3
            /* COMPONENT_ALIAS = "Dispatcher" */
            ProActiveComponentRepresentative a;
            a = Fractive.lookup(URIBuilder.buildURI(hostName, COMPONENT_ALIAS, protocol, portNumber)
                    .toString());
            this.c3dDispatcher = (Dispatcher) a.getFcInterface("user2dispatcher");
            //@snippet-end C3D_Components_3
            setVisible(false);
        } catch (UnknownHostException e) {
            treatException(e, "Sorry, host name '" + hostName + "' not found.");
        } catch (IOException e) {
            treatException(e, "Sorry, lookup failed on '" + hostName + "'.");
        } catch (NamingException e) {
            treatException(e, "Sorry, lookup failed on '" + hostName +
                "', no Component registered with name " + COMPONENT_ALIAS + ".");
        } catch (NoSuchInterfaceException e) {
            treatException(e, "Sorry, lookup failed on '" + hostName + "', component registered with name " +
                COMPONENT_ALIAS + " does not have the correct interface.");
        }
    }
}
