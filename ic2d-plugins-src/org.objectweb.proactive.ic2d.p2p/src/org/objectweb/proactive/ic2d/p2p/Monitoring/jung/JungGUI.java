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
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.extra.p2p.monitoring.Dumper;
import org.objectweb.proactive.extra.p2p.monitoring.Link;
import org.objectweb.proactive.extra.p2p.monitoring.P2PNode;
import org.objectweb.proactive.extra.p2p.monitoring.PeerAttribute;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;
import org.objectweb.proactive.extra.p2p.monitoring.event.P2PNetworkListener;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStringer;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.ToolTipFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.DefaultSettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.LayoutMutable;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;


public class JungGUI implements ToolTipFunction, P2PNetworkListener {
    // the time in ms we wait after a vertex has been added
    private final int UPDATE_PAUSE = 1;
    protected Graph graph;
    protected CustomVisualizationViewer vv;
    protected PluggableRenderer pr;
    protected StringLabeller sl;
    protected ConstantEdgeStringer edgesLabeller;
    protected DefaultSettableVertexLocationFunction vertexLocations;

    // protected Integer key = new Integer(1);
    protected Layout layout;
    protected boolean mutable;
    protected GraphZoomScrollPane panel;
    // protected JPanel testPanel;
    // protected VertexPaintFunction vertexPaint;

    protected NetworkColorScheme colorScheme;

    // the eclipse display
    protected Display display;

    public JungGUI(Display d) {
        this.display = d;

        graph = new UndirectedSparseGraph(); // this.createGraph();
        vertexLocations = new DefaultSettableVertexLocationFunction();
        // layout = useNonMutableLayout(graph);
        // layout = useMutableLayout(graph);
        layout = useLayout(graph, 2);

        // Layout layout = new ISOMLayout(graph);
        sl = StringLabeller.getLabeller(graph);
        edgesLabeller = new ConstantEdgeStringer(null);

        pr = new PluggableRenderer();
        // pr = new CirclePluggableRenderer((CircleLayout) layout);
        pr.setVertexStringer(sl);
        pr.setVertexPaintFunction(new PeerVertexPaintFunction());

        // pr.setGraphLabelRenderer()
        // pr.setVertexLabelCentering(true);
        //vv = new VisualizationViewer(layout, pr);
        vv = new CustomVisualizationViewer(layout, pr);

        layout.initialize(new Dimension(1900, 1200));
        //        vv.getModel().setGraphLayout(layout, new Dimension(1900,1200));
        //        vv.getModel().restart();
        //vv.setRe
        //vv = new VisualizationViewer(layout, pr,new Dimension(1900, 1200));
        // vv.setPreferredSize(new Dimension(1900, 1200));
        vv.setPickSupport(new ShapePickSupport());
        pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
        vv.setBackground(Color.white);
        vv.setToolTipListener(this);
        vv.setPickSupport(new ShapePickSupport());
        EditingModalGraphMouse gm = new EditingModalGraphMouse();
        gm.setVertexLocations(vertexLocations);
        gm.setMode(Mode.PICKING);
        gm.add(new PeerPopupMenuPlugin(display, vertexLocations, this));
        vv.setGraphMouse(gm);
        panel = new GraphZoomScrollPane(vv);

        //        vv.addChangeListener(new ChangeListener() { 
        //            
        //            public void stateChanged(ChangeEvent e) { 
        //             
        ////            MultiLayerTransformer mlt = (MultiLayerTransformer)e.getSource(); 
        //            MutableTransformer lt = mlt.getTransformer(Layer.LAYOUT); 
        //            MutableTransformer vt = mlt.getTransformer(Layer.VIEW); 
        //            double scale = vt.getScale() * lt.getScale(); 
        //            zoomLabel.setText(scale+" %"); 
        //             
        //            }}); 

        //   JLabel label = new JLabel("tototot");
        //  panel.add(label);
        // testPanel = new JPanel();
        // testPanel.add(vv);
    }

    public void changeLayout(int i) {
        Layout l = useLayout(graph, i);
        vv.stop();
        vv.setGraphLayout(l);
        vv.restart();
    }

    /**
     * Indicates which layout to use 0 : circle layout 1 : KK Layout 2 : FR Layout 3 : Spring Layout
     * 
     * @param g
     * @param i
     *            the number of the layout to use
     * @return
     */
    public Layout useLayout(Graph g, int i) {
        switch (i) {
            case 0: {
                this.mutable = false;
                return this.useCircleLayout(g);
            }
            case 1: {
                this.mutable = false;
                return this.useKKLayout(g);
            }
            case 2: {
                this.mutable = true;
                return this.useFRLayout(g);
            }
            case 3: {
                this.mutable = true;
                return this.useSpringLayout(g);
            }

                // return this.useFadingVertexLayout(g);
                // return this.useTreeLayout(g);
            default:
                return null;
        }
    }

    // public Layout useMutableLayout(Graph g) {
    // this.mutable = true;
    // return this.useSpringLayout(g);
    // // return this.useFRLayout(g);
    // }
    //    
    public LayoutMutable useFRLayout(Graph g) {
        return new FRLayout(g);
    }

    /**
     * Mutable layout
     * 
     * @param g
     * @return
     */
    protected LayoutMutable useSpringLayout(Graph g) {
        SpringLayout l = new SpringLayout(graph);
        l.setRepulsionRange(500);
        l.setStretch(0.5);
        return l;
    }

    /**
     * Non mutable layout
     * 
     * @param g
     * @return
     */
    protected Layout useKKLayout(Graph g) {
        KKLayout kk = new KKLayout(g);
        kk.setLengthFactor(1.3);
        return kk;
    }

    protected Layout useCircleLayout(Graph g) {
        return new CircleLayout(g);
    }

    protected void generateGraphNodes(Dumper dump) {
        Set<Map.Entry<String, P2PNode>> map = (Set<Map.Entry<String, P2PNode>>) dump.getP2PNetwork()
                .getSenders().entrySet();
        Iterator it = map.iterator();
        while (it.hasNext()) {
            Map.Entry<String, P2PNode> entry = (Map.Entry<String, P2PNode>) it.next();

            // the node might have a -1 index because has never sent anything
            P2PNode node = ((P2PNode) entry.getValue());
            this.addVertex(node);
        }
        // layout.restart();
        this.updateView();
    }

    protected void generateGraphLinks(Dumper dump) {
        // now dump the links
        // int i = 0;
        Set<Map.Entry<String, Link>> map2 = (Set<Map.Entry<String, Link>>) dump.getP2PNetwork().getLinks()
                .entrySet();

        Iterator it = map2.iterator();
        while (it.hasNext()) {
            Link entry = ((Map.Entry<String, Link>) it.next()).getValue();

            // System.out.println("---- looking for sender " + entry.getSource());
            String source = entry.getSource();
            String dest = entry.getDestination();
            // this.addVertex(source);
            // this.addVertex(dest);
            this.addEdge(source, dest);
            // vv.repaint();
            // System.out.println("JungGUI.generateGraph()");
            this.updateView();
        }
    }

    protected void generateGraph(Dumper dump) {
        this.generateGraphNodes(dump);
        this.generateGraphLinks(dump);
    }

    protected void updateView() {
        // vv.suspend();
        if (mutable) {
            ((LayoutMutable) layout).update();
            if (!vv.isVisRunnerRunning()) {
                vv.init();
            }
        } else {
            // vv.setGraphLayout(this.useNonMutableLayout(graph));
            // this.layout.restart();
        }
        // try {
        // Thread.sleep(UPDATE_PAUSE);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // // make your changes to the graph here
        // graph.addVertex(new SparseVertex());
        //    	 
        // vv.unsuspend();
        vv.repaint();
    }

    protected void addVertex(P2PNode p) {
        String s = p.getName();
        P2PUndirectedSparseVertex v = (P2PUndirectedSparseVertex) sl.getVertex(s);
        if (v == null) {
            // we haven't seen this peer
            // System.out.println(" *** Adding peer --" + s + "--" + p.getNoa() + " " +
            // p.getMaxNOA());
            v = (P2PUndirectedSparseVertex) graph.addVertex(new P2PUndirectedSparseVertex(p));
            try {
                sl.setLabel(v, s);
            } catch (UniqueLabelException e) {
                e.printStackTrace();
            }
            // v.setName(s);
        }
        // in all cases we set its values
        // because they might not be correct
        // v.setMaxNoa(p.getMaxNOA());
        // v.setNoa(p.getNoa());
    }

    protected void addEdge(String source, String dest) {
        Vertex current = sl.getVertex(source);
        Vertex v = sl.getVertex(dest);
        System.out.println("JungGUI.addEdge() from " + current + " to " + v);
        graph.addEdge(new UndirectedSparseEdge(current, v));
    }

    /**
     * Remove the current P2P Network
     * 
     */
    public void clear() {
        vv.stop();
        graph.removeAllEdges();
        graph.removeAllVertices();
        sl.clear();
        vv.restart();
    }

    public JPanel getPanel() {
        return this.panel;
        //return this.testPanel;
    }

    public void setRepulsionRange(int i) {
        System.out.println("JungGUI.setRepulsionRange() " + i);
        ((SpringLayout) this.layout).setRepulsionRange(i);
    }

    public String getToolTipText(Vertex v) {
        // System.out.println("JungGUI.getToolTipText() " + v);
        // return "<html> " + sl.getLabel(v) + " <br> noa = " + ((P2PUndirectedSparseVertex)
        // v).getNoa() +
        // "</html>";
        return "<html> " + sl.getLabel(v) + "</html>";
        // return null;
    }

    public String getToolTipText(Edge e) {
        return null;
    }

    public String getToolTipText(MouseEvent event) {
        return null;
    }

    public void newPeer(P2PNode node) {
        this.addVertex(node);
        // layout.restart();
        // this.updateView();
    }

    public void newLink(Link link) {
        String source = link.getSource();
        String dest = link.getDestination();
        // this.addVertex(source);
        // this.addVertex(dest);
        this.addEdge(source, dest);
        // vv.repaint();
        // System.out.println("JungGUI.generateGraph()");
        // this.updateView();
    }

    public void refresh() {
        this.updateView();
    }

    @Override
    public void addAttribute(P2PNode node, PeerAttribute[] att) {
        // TODO Auto-generated method stub

    }

    public PeerVertexPaintFunction getVertexPaint() {
        return (PeerVertexPaintFunction) pr.getVertexPaintFunction();
    }

    public void setColorScheme(NetworkColorScheme colorScheme) {
        vv.setColorScheme(colorScheme);

    }

    // public void setVertexPaint(VertexPaintFunction vertexPaint) {
    // // this.vertexPaint = vertexPaint;
    // pr.setVertexPaintFunction(vertexPaint);
    // }

    // public static void main(String[] args) {
    // JFrame f = new JFrame();
    // JungGUI gui = new JungGUI();
    //
    // f.add(gui.getPanel());
    // f.pack();
    // f.setVisible(true);
    // Dumper dump = new Dumper();
    // dump.getP2PNetwork().addListener(gui);
    // //dump.createGraphFromFile2(args[0]);
    // try {
    // Dumper aDump = (Dumper) ProActive.turnActive(dump);
    // Dumper.requestAcquaintances(args[0], aDump);
    // } catch (ActiveObjectCreationException e) {
    // e.printStackTrace();
    // } catch (NodeException e) {
    // e.printStackTrace();
    // }
    // }
}
