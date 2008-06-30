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
package org.objectweb.proactive.extra.p2p.monitoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class P2PNode {
    protected String name;
    protected String type;
    // protected LinearColor color;
    // the current node index
    // if -1, indicates that this node has not been seen
    // as a sender
    protected int index;
    protected int maxNOA;
    protected int noa;
    protected ArrayList<PeerAttribute> attributes;

    public P2PNode(String name) {
        this.name = name;
        this.index = -1;
        // this.color = new LinearColor(Color.RED,0,1);
    }

    public P2PNode(String name, PeerAttribute[] pa) {
        this(name, -1);
        // metaData.add(Array.pa);

        // }
        this.attributes = new ArrayList<PeerAttribute>(Arrays.asList(pa));
    }

    public P2PNode(String name, int index) {
        this(name);
        this.index = index;
    }

    public P2PNode(String name, int index, int noa, int maxNOA) {
        this(name, index);
        this.noa = noa;
        this.maxNOA = maxNOA;
    }

    public P2PNode(String name, int index, int noa, int maxNOA, PeerAttribute data) {
        this(name, index);
        this.noa = noa;
        this.maxNOA = maxNOA;
        this.attributes.add(data);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getMaxNOA() {
        return maxNOA;
    }

    public int getNoa() {
        return noa;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public void setMaxNOA(int maxNOA) {
        this.maxNOA = maxNOA;
    }

    public void setNoa(int noa) {
        this.noa = noa;
    }

    public String[] getAttribute() {
        Iterator<PeerAttribute> it = attributes.iterator();
        String[] tmp = new String[attributes.size()];
        int j = 0;
        while (it.hasNext()) {
            PeerAttribute peerAttribute = (PeerAttribute) it.next();
            tmp[j] = peerAttribute.toString();
            j++;
        }
        return tmp;
        // metaData.toArray(new String[] {});
    }

    public void addAttribute(PeerAttribute d) {
        this.attributes.add(d);
    }

    public void addAttribute(PeerAttribute[] metaData) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<PeerAttribute>();
        }
        this.attributes.addAll(Arrays.asList(metaData));
    }

    public String getMetadataAsString() {
        String s = "";
        Iterator<PeerAttribute> it = attributes.iterator();
        while (it.hasNext()) {
            s += it.next() + "\n";
        }
        return s;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        this.type = t;
    }

    public ArrayList<PeerAttribute> getAttributes() {
        return attributes;
    }

    public float getAttributeValue(String att) {
        for (PeerAttribute pa : attributes) {
            if (pa.getName().equals(att)) {
                return Float.parseFloat(pa.getValue());
            }

        }
        return 0;
    }

    // public LinearColor getColor() {
    // return color;
    // }
}
