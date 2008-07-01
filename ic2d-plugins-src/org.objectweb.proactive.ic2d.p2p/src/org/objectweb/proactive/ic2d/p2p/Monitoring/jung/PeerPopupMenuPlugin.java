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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.eclipse.swt.widgets.Display;

import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;


public class PeerPopupMenuPlugin extends AbstractPopupGraphMousePlugin {
    SettableVertexLocationFunction vertexLocations;
    protected Display display;
    protected JungGUI gui;

    public PeerPopupMenuPlugin(Display d, SettableVertexLocationFunction vertexLocations, JungGUI g) {
        this.vertexLocations = vertexLocations;
        this.display = d;
        this.gui = g;
    }

    @Override
    protected void handlePopup(MouseEvent e) {
        final VisualizationViewer vv = (VisualizationViewer) e.getSource();
        final Point2D ivp = vv.inverseViewTransform(e.getPoint());
        PickSupport pickSupport = vv.getPickSupport();
        JPopupMenu popup = new JPopupMenu();
        System.out.println("PeerPopupMenuPlugin.handlePopup()");
        if (pickSupport != null) {
            final P2PUndirectedSparseVertex vertex = (P2PUndirectedSparseVertex) pickSupport.getVertex(ivp
                    .getX(), ivp.getY());

            if (vertex != null) {
                //JMenu submenu = new JMenu(vertex.getName());
                // JMenu noaSubmenu = new JMenu("Noa : " + vertex.getNoa());
                // JMenu maxNoaSubmenu = new JMenu("Max Noa : " + vertex.getMaxNoa());

                //  submenu.add(noaSubmenu);
                //   submenu.add(maxNoaSubmenu);
                //  popup.add(submenu);
                //                popup.add(new JLabel(vertex.getName(), SwingConstants.RIGHT));
                popup.add(this.buildDataPanel(vertex));
                //popup.set(SwingConstants.RIGHT);

                //
                //                noaSubmenu.add(new AbstractAction("Set Noa") {
                //                    public void actionPerformed(ActionEvent e) {
                //                        System.out.println(".handlePopup()xxxxxxxxxxxxxxxxxxxxxxxxxxxxx display is " +
                //                            display);
                //
                //                        display.asyncExec(new Runnable() {
                //                            public void run() {
                //                                new ChangeNOADialog("NOA", vertex.getName(), vertex.getNoa(), display
                //                                        .getActiveShell());
                //                                //                    	    	        	   System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ " + display.getActiveShell());
                //                            }
                //                        });
                //
                //                        //SetValueDialog v = new SetValueDialog("Set Value", "Noa",vertex.getNoa());
                //                        //v.setVisible(true);
                //                        //  System.out.println(v.getValue());
                //                    }
                //                });

                //                maxNoaSubmenu.add(new AbstractAction("Set Max Noa") {
                //                    public void actionPerformed(ActionEvent e) {
                //                        SetValueDialog v = new SetValueDialog("Set Value", "Max Noa", vertex.getMaxNoa());
                //                        v.setVisible(true);
                //                        System.out.println(v.getValue());
                //                    }
                //                });
                //                popup.add(new AbstractAction("Update") {
                //                    public void actionPerformed(ActionEvent e) {
                //                        //SetValueDialog v = new SetValueDialog("Set Value", "Max Noa",vertex.getMaxNoa());
                //                        //	v.setVisible(true);
                //                        System.out.println("Update!");
                //                    }
                //                });
            } else {
                popup.add(new AbstractAction("Update") {
                    public void actionPerformed(ActionEvent e) {
                        //       gui.panel.get
                        System.out.println(".actionPerformed() vv size " + gui.vv.getSize());
                        System.out.println(".actionPerformed() layout size " + gui.layout.getCurrentSize());
                        //      System.out.println(".actionPerformed() layout size " + gui.pr.get);

                        System.out.println(".actionPerformed() transformer scaleX " +
                            gui.vv.getLayoutTransformer().getScaleX());
                        System.out.println(".actionPerformed() transformer scaleY " +
                            gui.vv.getLayoutTransformer().getScaleY());
                        //System.out.println(".actionPerformed() " + gui.pr.getScreenDevice().getSize());
                        //System.out.println(".actionPerformed() " + gui.);

                        //gui.refresh();
                    }
                });
            }
            popup.show(vv, e.getX(), e.getY());
        }
    }

    protected JLabel buildDataPanel(P2PUndirectedSparseVertex n) {
        String[] data = n.getNode().getAttribute();
        System.out.println("PeerPopupMenuPlugin.buildDataPanel() metadata  " + data);
        //JLabel l = new JLabel();
        String s = "<html>" + n.getNode().getName() + "<br>";
        for (int i = 0; i < data.length; i++) {
            s += data[i] + "<br>";
        }
        s += "</html>";
        JLabel l = new JLabel(s);
        l.setHorizontalTextPosition(SwingConstants.LEFT);
        return l;
    }

}
