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
package org.objectweb.proactive.ic2d.p2p.Monitoring.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorSchemeObserver;


public class PeerAttributeColorComposite implements NetworkColorSchemeObserver {

    protected Composite comp;

    protected ArrayList<TypeAttributeColorComposite> typeAttribute;

    protected NetworkColorScheme n;

    public PeerAttributeColorComposite(Composite parent, int style) {
        this.typeAttribute = new ArrayList<TypeAttributeColorComposite>();
        this.comp = new Composite(parent, style);
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginRight = 10;
        rowLayout.type = SWT.VERTICAL;
        this.comp.setLayout(rowLayout);
        comp.pack();
    }

    /**
     * Add a new type if not present Create a new composite if not present
     * 
     * @param s
     */
    protected void addPeerType(String s) {
        boolean contains = false;

        for (TypeAttributeColorComposite c : typeAttribute) {
            if (c.getType().equals(s)) {
                contains = true;
            }
        }

        if (!contains) {
            // we need to create a new composite
            this.createNewTypeAttributeComposite(s);
        }
    }

    /**
     * Create a new TypeAttributeColorComposite in the current composite to handle the color of a
     * main type
     * 
     * @param type
     */
    protected void createNewTypeAttributeComposite(String type) {
        System.out.println("PeerAttributeColorComposite.createNewTypeAttributeComposite()");
        TypeAttributeColorComposite tAtt = new TypeAttributeColorComposite(this.comp, SWT.NONE, this.n);
        tAtt.setType(type);
        typeAttribute.add(tAtt);
        comp.pack();
    }

    public TypeAttributeColorComposite getComposite(String type) {
        for (TypeAttributeColorComposite tc : typeAttribute) {
            if (tc.getType().equals(type)) {
                return tc;
            }
        }
        return null;
    }

    /**
     * Add the attribute to all existing TypeAttributeColorComposite
     * 
     * @param s
     */
    protected void addPeerAttribute(String s) {
        for (TypeAttributeColorComposite c : typeAttribute) {
            c.addPeerAttribute(s);
        }
    }

    public Composite getComposite() {
        return comp;
    }

    protected void getColorScheme() {

    }

    public void update() {
        final String[] main = n.getMainAttributeList();
        final String[] other = n.getOtherAttributeList();
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                for (int i = 0; i < main.length; i++) {
                    System.out.println("xxxxx   .run() " + main[i]);
                    addPeerType(main[i]);
                }
                for (int j = 0; j < other.length; j++) {
                    addPeerAttribute(other[j]);
                }
            }
        });
    }

    public void setColorScheme(NetworkColorScheme colorScheme) {
        this.n = colorScheme;
    }

}
