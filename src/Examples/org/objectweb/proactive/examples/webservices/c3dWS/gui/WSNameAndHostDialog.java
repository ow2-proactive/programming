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
package org.objectweb.proactive.examples.webservices.c3dWS.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extensions.webservices.WebServicesFrameWorkFactoryRegistry;


/** A dialog with two text fields, which handles incorrect entries.
 * It is used to select a dispatcher host and a user name.
 * Inspired from the java Swing Dialog tutorial */
public class WSNameAndHostDialog extends JDialog implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 52;
    private String userName = "Bob";
    private JTextField userTextField;
    private JOptionPane optionPane;
    private String enterButtonString = "Enter";
    private String cancelButtonString = "Cancel";
    protected JTextField dispatcherUrl;
    private String wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
    private JTextField wsFWTextField;

    /** This is NOT an Active Object: constructor is configurable! */
    public WSNameAndHostDialog() {
        super();

        String localHostUrl = WSNameAndHostDialog.getLocalHostUrl();
        int index = localHostUrl.lastIndexOf(":");
        String localHostUrlService = localHostUrl.substring(0, index);
        localHostUrlService = "http:" + localHostUrlService + ":8080/";

        setTitle("Welcome to the Collaborative 3D Environment.");

        this.userTextField = new JTextField(this.userName, 10);
        this.userTextField.addActionListener(this);

        this.dispatcherUrl = new JTextField(localHostUrlService, 10);
        this.dispatcherUrl.addActionListener(this);

        this.wsFWTextField = new JTextField(this.wsFrameWork, 4);
        this.wsFWTextField.addActionListener(this);

        //Create an array of the text and components to be displayed.
        Object[] array = { "Please enter your name, ", this.userTextField, "the C3DDispatcher Service url",
                this.dispatcherUrl, "and the web service framework", this.wsFWTextField };

        //Create an array specifying the number of dialog buttons and their text.
        Object[] options = { this.enterButtonString, this.cancelButtonString };

        //Create the JOptionPane.
        this.optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null,
            options, options[0]);

        //Make this dialog display it.
        setContentPane(this.optionPane);

        //Handle window closing correctly.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                // handle closing behavior in propertyChange ()
                WSNameAndHostDialog.this.optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                WSNameAndHostDialog.this.userTextField.requestFocusInWindow();
            }
        });

        //Register an event handler that reacts to option pane state changes.
        this.optionPane.addPropertyChangeListener(this);
        pack(); // find optimal size
        setModal(true); // cannot play with other windows when this one is visible
        setVisible(true);
    }

    /** Handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        this.optionPane.setValue(this.enterButtonString);
    }

    /** Reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent event) {
        String prop = event.getPropertyName();

        if (isVisible() && (event.getSource() == this.optionPane) &&
            (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = this.optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value. If you don't do this, then if the user
            //presses the same button next time, no property change event will be fired.
            this.optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (this.enterButtonString.equals(value)) {
                this.userName = this.userTextField.getText();

                if (this.userName.equals("")) { //userName text was invalid
                    this.userName = "Bob";
                }

                this.wsFrameWork = this.wsFWTextField.getText();

                if (!WebServicesFrameWorkFactoryRegistry.isValidFrameWork(this.wsFrameWork)) {
                    this.wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
                }

            } else { //user closed dialog or clicked cancel
                this.userName = null;
            }
            setVisible(false);
        }
    }

    /** Always contains some characters, default value is Bob. */
    public String getUserName() {
        return this.userName;
    }

    public String getDispatcherService() {
        return this.dispatcherUrl.getText();
    }

    public String getWsFrameWork() {
        return this.wsFrameWork;
    }

    /** Gets the name of the machine this is running on.
     * @return a url which is suitable for looking up active objects. */
    public static String getLocalHostUrl() {
        String localhost = "";

        int port = -1;
        String protocol = CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue();

        try {
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(protocol);
            port = rof.getPort();
        } catch (UnknownProtocolException e) {
            // Well should not happen ...
            e.printStackTrace();
        }

        localhost = URIBuilder.buildURI(
                URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()), null, null, port)
                .toString();

        return localhost;
    }
}
