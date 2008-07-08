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
package org.objectweb.proactive.extra.p2p.monitoring.color;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class LinearColor extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;

    protected Color base;

    protected String name;
    protected float dataMin;
    protected float dataMax;
    protected float data;

    protected double brightMin = 0.3;
    protected double brightMax = 0.8;

    public LinearColor() {

    }

    public LinearColor(Color base, String name, float min, float max) {
        this.base = base;
        this.name = name;
        this.dataMin = min;
        this.dataMax = max;
    }

    public Color getBaseColor() {
        return this.base;
    }

    public void setBaseColor(Color col) {
        this.base = col;
    }

    public Color getColor() {
        return this.getColor(data);
    }

    public Color getColor(float i) {
        float af[] = Color.RGBtoHSB((int) base.getRed(), base.getGreen(), base.getBlue(), null);
        double tmpMax = dataMax - dataMin;

        double b = i * (brightMax - brightMin) / tmpMax + brightMin;
        return new Color(Color.HSBtoRGB(af[0], af[1], (float) b));
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle2D r = new Rectangle2D.Float(10, 10, 30, 30);
        g2.setPaint(this.base);
        g2.fill(r);
        int j = 1;
        for (float i = 0; i < (this.dataMax - this.dataMin); i += 0.01) {
            Rectangle2D r2 = new Rectangle2D.Float(10 + 10 * j, 10, 30, 30);
            g2.setPaint(this.getColor(i));
            g2.fill(r2);
            j++;
        }

        this.brightMin = 0.3;
        j = 0;
        for (float i = 0; i < (this.dataMax - this.dataMin); i += 0.01) {
            Rectangle2D r2 = new Rectangle2D.Float(10 + 10 * j, 50, 30, 30);
            g2.setPaint(this.getColor(i));
            g2.fill(r2);
            j++;
        }

    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        LinearColor l = new LinearColor(Color.RED, "test", 0, 1);

        f.getContentPane().add(l);
        f.setSize(1300, 250);
        f.setVisible(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        l.setBaseColor(new Color(22, 56, 219));
        f.repaint();
    }

    public float getDataMin() {
        return dataMin;
    }

    public void setDataMin(float dataMin) {
        this.dataMin = dataMin;
    }

    public float getDataMax() {
        return dataMax;
    }

    public void setDataMax(float dataMax) {
        this.dataMax = dataMax;
    }

    public void setData(float data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return this.name + "," + this.base + "," + this.dataMin + "," + this.dataMax;
    }

}
