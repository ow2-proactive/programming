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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.p2p.Monitoring.jung;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Set;

import org.objectweb.proactive.extra.p2p.monitoring.color.LinearColor;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;

import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;


public class CustomVisualizationViewer extends VisualizationViewer {

    protected NetworkColorScheme colorScheme;

    public CustomVisualizationViewer(Layout layout, Renderer renderer) {
        super(layout, renderer);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void renderGraph(Graphics2D arg0) {
        super.renderGraph(arg0);
        // System.out.println("CustomVisualizationViewer.renderGraph()");
        this.colorLegend(arg0);
    }

    protected void colorLegend(Graphics2D g) {
        if (this.colorScheme != null) {
            g.setColor(Color.BLACK);
            g.drawString("Color Scale", 10, 20);
            // Set<String> keys = colorScheme.getKeys();
            // int y = 15;
            // for(String s : keys) {
            // g.drawString(s, 10, 20+y);
            // y+=15;
            // }

        }
    }

    protected void colorRectangle(Graphics2D g, LinearColor lc, int x, int y) {

        // for (float i = 0; i < (this.dataMax - this.dataMin); i += 0.01) {
        // Rectangle2D r2 = new Rectangle2D.Float(10 + 10 * j, 10, 30, 30);
        // g2.setPaint(this.getColor(i));
        // g2.fill(r2);
        // j++;
        // }
    }

    public void setColorScheme(NetworkColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

}
