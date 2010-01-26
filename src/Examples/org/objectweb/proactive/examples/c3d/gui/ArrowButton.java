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
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;


/**
 * A Button extension, which has painted on it an arrow.
 * Possible directions are "left", "right", "up", "down", "spinleft", or "spindown"
 * See PNG files in directory org/objectweb/proactive/examples/c3d/arrows/
 */
public class ArrowButton extends JButton {
    private static Logger logger = Logger.getLogger(Loggers.EXAMPLES);
    final static String PATH = "org/objectweb/proactive/examples/c3d/arrows/";
    final static String EXTENSION = "Arrow.png";
    final Dimension dimension;

    /**
     * A fixed size JButton, with an image indicating a direction.
     * @param direction should be one of "left", "right", "up", or "down"
     */
    public ArrowButton(String direction) {
        super();

        ImageIcon icon;
        String fileName = PATH + direction + EXTENSION;
        try { // load the image, from the ProActive hierarchy
            ClassLoader cl = this.getClass().getClassLoader();
            java.net.URL u = cl.getResource(fileName);
            Image image = getToolkit().getImage(u);
            icon = new ImageIcon(image);
        } catch (Exception e) { // if ever there was a RunTimeException, of whatever cause
            icon = null;
        }

        if ((icon == null) || (icon.getIconWidth() == 0)) { // if trouble loading Image 
            logger.error("Image file not found " + fileName);
            setText(direction);
            this.dimension = getPreferredSize();
        } else { // just set the button to look like the Image
            setIcon(icon);
            this.dimension = new Dimension(icon.getIconWidth(), icon.getIconHeight());
            setPreferredSize(this.dimension);
        }
    }

    // Implemented so that the button stays with the original size ==> Button has fixed size 
    @Override
    public Dimension getMaximumSize() {
        return dimension;
    }

    @Override
    public Dimension getMinimumSize() {
        return dimension;
    }

    /**
     * Convenience method, makes a panel surrounding the (centered) ArrowButton.
     * @return a variable size JPanel which contains the Fixed size button
     */
    public JComponent getJPanel() {
        // This is way too complicated, it's equivalent to a BorderLayout.CENTER
        // But BorderLayout.CENTER doesn't seem to work... And setLayout(null) doesn't center vertically
        Box box = Box.createVerticalBox();
        box.add(Box.createGlue());
        Box insidebox = Box.createHorizontalBox();
        insidebox.add(Box.createGlue());
        insidebox.add(this);
        insidebox.add(Box.createGlue());
        box.add(insidebox);
        box.add(Box.createGlue());
        return box;
    }
}
