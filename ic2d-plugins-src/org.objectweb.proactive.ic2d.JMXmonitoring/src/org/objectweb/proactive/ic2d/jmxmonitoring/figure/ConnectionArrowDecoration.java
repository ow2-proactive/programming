/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;


/**
 * A decoration for the connection between active objects
 * @author The ProActive Team
 *
 */
public class ConnectionArrowDecoration extends PolygonDecoration {

    private boolean visible = true;

    public ConnectionArrowDecoration() {
        super();
    }

    @Override
    public void outlineShape(Graphics g) {
        if (!visible) {
            return;
        }
        super.outlineShape(g);

    }

    public void fillShape(Graphics g) {
        if (!visible) {
            return;
        }
        super.fillShape(g);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
