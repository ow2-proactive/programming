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
package org.objectweb.proactive.extra.p2p.monitoring.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.objectweb.proactive.extra.p2p.monitoring.Dumper;
import org.objectweb.proactive.extra.p2p.monitoring.Link;
import org.objectweb.proactive.extra.p2p.monitoring.P2PNode;
import org.objectweb.proactive.extra.p2p.monitoring.PeerAttribute;
import org.objectweb.proactive.extra.p2p.monitoring.event.P2PNetworkListener;


/**
 * Used to maintain information about peers and compute their colors
 * 
 * @author fhuet
 * 
 */
public class NetworkColorScheme implements P2PNetworkListener {

    protected ArrayList<NetworkColorSchemeObserver> listeners = new ArrayList<NetworkColorSchemeObserver>();

    // the list of all possible attributes, divided into type ones
    // and others
    //  protected TreeSet<String> typeList = new TreeSet<String>();
    protected TreeSet<String> otherList = new TreeSet<String>();

    protected String typeAttribute = "Type";

    //    protected HashMap<String, LinearColor> color = new HashMap<String, LinearColor>();

    /**
     * Maintain association between a type and a list of attribute
     */
    protected HashMap<String, AttributeList> typeAttrib = new HashMap<String, AttributeList>();

    public NetworkColorScheme() {
    }

    //    public Color getColor(P2PNode node) {
    //        LinearColor l = null;
    //        String key = "";
    //        for (PeerAttribute pa : node.getAttributes()) {
    //            if (pa.getName().equals("Type")) {
    //                key = pa.getValue();
    //            } else {
    //                l = color.get(key);
    //                if (l != null && l.getName().equals(pa.getName())) {
    //                    return l.getColor(Float.parseFloat(pa.getValue()));
    //                }
    //            }
    //        }
    //        return Color.WHITE;
    //    }

    public Color getColor(P2PNode node, String att) {
        //        LinearColor l = null;
        String key = node.getType();

        if (key != null) {
            AttributeList al = typeAttrib.get(key);
            if (al != null) {
                return al.getColor(att, node.getAttributeValue(att));
            }
        }
        return Color.WHITE;
    }

    //    public Color getBaseColor(String type, String attribute) {
    //        LinearColor l = this.color.get(type);
    //        if (l == null || !l.getName().equals(attribute)) {
    //            return Color.WHITE;
    //        } else {
    //            return l.getBaseColor();
    //        }
    //
    //    }

    public Color getBaseColor(String type, String attribute) {
        AttributeList al = typeAttrib.get(type);
        if (al != null) {
            return al.getBaseColor(attribute);
        }
        return Color.WHITE;

    }

    /**
     * Set the color of the type-attribute couple Remove existing association for type
     */
    //    public void setBaseColor(String type, String attribute, Color col) {
    //        LinearColor l = this.color.remove(type);
    //        if (l == null || !l.getName().equals(attribute)) {
    //            color.put(type, new LinearColor(col, attribute, 1, 100));
    //        } else {
    //            // System.out.println("NetworkColorScheme.setBaseColor() changing color from " +
    //            // l.getBaseColor() + " to " + col);
    //            l.setBaseColor(col);
    //            this.color.put(type, l);
    //        }
    //    }
    public void setBaseColor(String type, String attribute, Color col) {
        AttributeList al = typeAttrib.get(type);
        if (al != null) {
            al.setBaseColor(attribute, col);

        }
        //        LinearColor l = this.color.remove(type);
        //        if (l == null || !l.getName().equals(attribute)) {
        //            color.put(type, new LinearColor(col, attribute, 1, 100));
        //        } else {
        //            // System.out.println("NetworkColorScheme.setBaseColor() changing color from " +
        //            // l.getBaseColor() + " to " + col);
        //            l.setBaseColor(col);
        //            this.color.put(type, l);
        //        }
    }

    // public float getDataMax(String type, String attribute) {
    // LinearColor l = this.color.get(type);
    // if (l == null || !l.getName().equals(attribute)) {
    // return 100;
    // } else {
    // return l.getDataMax();
    // }
    // }

    public float getDataMax(String type, String attribute) {
        AttributeList al = typeAttrib.get(type);
        if (al != null) {
            return al.getMax(attribute);
        } else {
            return 0;
        }

    }

    public void setDataMax(String type, String attribute, float f) {
        AttributeList al = typeAttrib.get(type);
        if (al != null) {
            al.setMax(attribute, f);
        }
    }

    // public void setDataMax(String type, String attribute, float f) {
    // LinearColor l = this.color.get(type);
    // if (l != null && l.getName().equals(attribute)) {
    // l.setDataMax(f);
    // }
    //
    // }

    // public float getDataMin(String type, String attribute) {
    // LinearColor l = this.color.get(type);
    // if (l == null || !l.getName().equals(attribute)) {
    // return 0;
    // } else {
    // return l.getDataMin();
    // }
    //
    // }

    public float getDataMin(String type, String attribute) {
        AttributeList al = typeAttrib.get(type);
        if (al != null) {
            return al.getMin(attribute);
        } else {
            return 0;
        }

    }

    public void setDataMin(String type, String attribute, float f) {
        AttributeList al = typeAttrib.get(type);
        //        System.out.println("NetworkColorScheme.setDataMin() for " +al);
        if (al != null) {
            al.setMin(attribute, f);
        }
    }

    //    public void setDataMin(String type, String attribute, float f) {
    //        LinearColor l = this.color.get(type);
    //        if (l != null && l.getName().equals(attribute)) {
    //            l.setDataMin(f);
    //        }
    //
    //    }

    // protected void setDataMin(String key, float f) {
    // LinearColor l = this.color.get(key);
    // if (l!=null) {
    // l.setDataMin(f);
    // }
    // }

    protected boolean isGrantPeer(P2PNode node) {
        ArrayList<PeerAttribute> al = node.getAttributes();
        for (PeerAttribute pa : al) {
            if (pa.getName().equals("Type")) {
                return pa.getValue().equals("Grant");
            }
        }
        return false;
    }

    public void newLink(Link link) {

    }

    public void newPeer(P2PNode node) {
    }

    public void refresh() {

    }

    /**
     * Add the attributes to the list of managed attribute Create a new <code>AttributeList</code>
     * if none exists
     * 
     * @Override
     */
    // public void addAttribute(P2PNode node, PeerAttribute[] att) {
    // String typeName = null;
    // for (int i = 0; i < att.length; i++) {
    // if (att[i].getName().equals(typeAttribute)) {
    // typeName = att[i].getValue();
    // typeList.add(typeName);
    // if (this.getAttributeList(att[i].getValue()) == null) {
    // this.typeAttrib.put(att[i].getValue(), new AttributeList());
    // }
    // } else {
    // otherList.add(att[i].getName());
    // if (typeName != null) {
    // // hopefully we should have seen the type already, so add
    // // the attributes to the list
    // AttributeList al = this.typeAttrib.get(typeName);
    // al.addAttribute(att[i]);
    // } else {
    // //not seen the type in these loops, so request it from the node itself
    // typeName=node.getType();
    // System.out.println("NetworkColorScheme.addAttribute() XXXXXXXXXXXXXXXXXXXXXXXX to "
    // +typeName);
    // }
    // }
    // }
    // this.notifyListeners();
    // }
    public void addAttribute(P2PNode node, PeerAttribute[] att) {
        String typeName = node.getType();
        for (int i = 0; i < att.length; i++) {
            if (att[i].getName().equals(typeAttribute)) {
                // typeName = att[i].getValue();
                // typeList.add(typeName);
                if (this.getAttributeList(att[i].getValue()) == null) {
                    this.typeAttrib.put(att[i].getValue(), new AttributeList());
                }
            } else {
                otherList.add(att[i].getName());
                if (typeName != null) {
                    // hopefully we should have seen the type already, so add
                    // the attributes to the list
                    System.out.println("NetworkColorScheme.addAttribute()  OK to " + typeName);
                    AttributeList al = this.typeAttrib.get(typeName);
                    al.addAttribute(att[i]);
                }
            }
        }
        this.notifyListeners();
    }

    public String[] getMainAttributeList() {
        // return this.typeList.toArray(new String[] {});
        return this.typeAttrib.keySet().toArray(new String[] {});
    }

    public String[] getOtherAttributeList() {

        return this.otherList.toArray(new String[] {});
    }

    public void addListener(NetworkColorSchemeObserver o) {
        this.listeners.add(o);
    }

    protected void notifyListeners() {
        for (NetworkColorSchemeObserver n : listeners) {
            n.update();
        }
    }

    /*   public Set<String> getKeys() {
           return color.keySet();
       }*/

    protected AttributeList getAttributeList(String name) {
        return this.typeAttrib.get(name);
    }

    public String toString() {
        String s = "";

        for (String keys : typeAttrib.keySet()) {
            AttributeList al = typeAttrib.get(keys);
            s += keys + "\n";
            s += "    " + al.toString() + "\n";
        }

        return s;
    }

    public static void main(String[] args) {
        Dumper d = new Dumper();
        // d.getP2PNetwork().addListener(gui);
        // d.getP2PNetwork().getColorScheme().addListener(PeersimLogView.group.getAttributeColorComposite());
        // PeersimLogView.group.setColorScheme(d.getP2PNetwork().getColorScheme());
        // gui.setColorScheme(d.getP2PNetwork().getColorScheme());
        d
                .createGraphFromPeerFile(
                        "/user/fhuet/home/workProActive/SVN/oasis/p2psimulation/src/resourceDiscoveryProtocol/simulations/simulationConfig2/rdpProtocol/traceClean",
                        null);
        System.out.println(d.getP2PNetwork().getColorScheme());
    }
}
