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
import java.util.HashMap;

import org.objectweb.proactive.extra.p2p.monitoring.PeerAttribute;


/**
 * Maintain a list of attribute and their min/max values
 * 
 * @author fhuet
 * 
 */
public class AttributeList {

    protected HashMap<String, AttributeItem> list = new HashMap<String, AttributeItem>();

    public AttributeList() {

    }

    public void addAttribute(PeerAttribute pa) {
        System.out.println("AttributeList.addAttribute() adding " + pa);
        AttributeItem i = list.get(pa.getName());
        // LinearColor l = list.get(name);
        float value = Float.parseFloat(pa.getValue());
        if (i == null) {
            list.put(pa.getName(), new AttributeItem(Color.WHITE, pa.getName(), value, value));
        } else {
            System.out.println("AttributeList.addAttribute() value " + value);
            if (value < i.getMin()) {
                i.setMin(value);
            }
            if (value > i.getMax()) {
                i.setMax(value);
            }
        }
    }

    public Color getColor(String name, float value) {
        AttributeItem i = list.get(name);
        if (i != null) {
            return i.l.getColor(value);
        }
        return Color.WHITE;
    }

    public void setMin(String attribute, float f) {
        AttributeItem i = this.list.get(attribute);
        if (i != null) {
            i.l.setDataMin(f);

        }
    }

    public float getMin(String attribute) {
        AttributeItem i = this.list.get(attribute);
        if (i == null) {
            return 0;
        } else {
            return i.l.getDataMin();
        }
    }

    public void setMax(String attribute, float f) {
        AttributeItem i = this.list.get(attribute);
        if (i != null) {
            i.l.setDataMax(f);

        }
    }

    public float getMax(String attribute) {
        AttributeItem i = this.list.get(attribute);
        if (i == null) {
            return 0;
        } else {
            return i.l.getDataMax();
        }
    }

    public String toString() {
        String s = "";
        for (String keys : list.keySet()) {
            s += list.get(keys);
        }
        return s;
    }

    private class AttributeItem {
        private LinearColor l;

        private AttributeItem(Color base, String name, float min, float max) {
            l = new LinearColor(base, name, min, max);
        }

        private float getMin() {
            return l.getDataMin();
        }

        private void setMin(float min) {
            l.setDataMin(min);
        }

        private void setMax(float max) {
            l.setDataMax(max);
        }

        private float getMax() {
            return l.getDataMax();
        }

        public String toString() {
            return "(" + l.getName() + "," + l.getBaseColor() + "," + l.getDataMin() + "," + l.getDataMax() +
                ")";
        }
    }

    public Color getBaseColor(String attribute) {
        AttributeItem i = list.get(attribute);
        if (i != null) {
            return i.l.getBaseColor();
        }
        return Color.WHITE;
    }

    public void setBaseColor(String attribute, Color col) {
        AttributeItem i = list.get(attribute);
        if (i != null) {
            i.l.setBaseColor(col);
        }

    }

}
