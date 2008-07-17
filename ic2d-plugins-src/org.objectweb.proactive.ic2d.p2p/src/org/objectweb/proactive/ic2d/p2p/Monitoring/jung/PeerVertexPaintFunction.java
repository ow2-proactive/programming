/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.p2p.Monitoring.jung;

import java.awt.Color;
import java.awt.Paint;

import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;
import org.objectweb.proactive.ic2d.p2p.Monitoring.views.PeersimLogView;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;


public class PeerVertexPaintFunction implements VertexPaintFunction {

    protected NetworkColorScheme scheme;

    public PeerVertexPaintFunction() {
        super();
    }

    public PeerVertexPaintFunction(NetworkColorScheme scheme) {
        this.scheme = scheme;
    }

    public Paint getDrawPaint(Vertex v) {
        return Color.black;
    }

    public Paint getFillPaint(Vertex v) {
        // int noa = v.degree();
        // return ((P2PUndirectedSparseVertex)v).getColor().getColor();
        P2PUndirectedSparseVertex pv = (P2PUndirectedSparseVertex) v;
        if (scheme != null) {
            String attribute = PeersimLogView.group.getAttributeColorComposite().getComposite(
                    pv.getNode().getType()).getSelectedAttribute();
            return scheme.getColor(pv.getNode(), attribute);
        }
        return null;
        // System.out.println("NOAVertexPaintFunction.getFillPaint() " +
        // ((P2PUndirectedSparseVertex) v).node.getMetadataAsString());
        // switch (noa) {
        // case 0:
        // return Color.BLACK;
        // case 1:
        // return Color.GRAY;
        // case 3:
        // return Color.BLUE;
        // case 5:
        // return Color.GREEN;
        // case 10:
        // return Color.RED;
        // default:
        // return Color.ORANGE;
        // }
    }

    public NetworkColorScheme getScheme() {
        return scheme;
    }

    public void setScheme(NetworkColorScheme scheme) {
        this.scheme = scheme;
    }

    //
    // public Color getForeColor(Vertex v) {
    // return Color.BLACK;
    // }
    //
    // public Color getBackColor(Vertex v) {
    // // int noa =((P2PUndirectedSparseVertex)v).getNoa();
    // int noa=v.degree();
    // switch (noa) {
    // case 0: return Color.BLACK;
    // case 1: return Color.GRAY;
    // case 3: return Color.BLUE;
    // case 5: return Color.GREEN;
    // case 10: return Color.RED;
    // default : return Color.ORANGE;
    // }
    // }
}
