/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import java.util.List;

import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;


public class DynamicGridLayout extends GridLayout {

    private String name;
    private int nb_lines;

    public DynamicGridLayout(String name) {
        super();
        this.name = name;
        numColumns = 1;
    }

    protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
        List children = container.getChildren();
        int numChildren = children.size();

        if (numChildren != 0) {

            Double squareroot = Math.sqrt(container.getChildren().size());
            int nb_colums = Math.max(new Double(Math.floor(squareroot)).intValue(), 1);
            int nb_lines = Math.max(new Double(Math.ceil((double) numChildren / nb_colums)).intValue(), 1);

            if ((this.numColumns != nb_colums) || (this.nb_lines != nb_lines)) {
                this.numColumns = nb_colums;
                this.nb_lines = nb_lines;
                container.getLayoutManager().layout(container);
                container.invalidateTree();
            }

            Dimension prefSize;
            IFigure child;
            Dimension totalDim = new Dimension();
            Dimension temp;

            temp = new Dimension();

            for (int i = 0; i < numChildren; i++) {
                child = (IFigure) children.get(i);
                prefSize = child.getPreferredSize(wHint, hHint);

                temp.width += prefSize.width;
                temp.height = Math.max(temp.height, prefSize.height);

                if ((i % nb_colums) == nb_colums - 1) {
                    totalDim.width = Math.max(temp.width, totalDim.width) + horizontalSpacing;
                    temp.width = 0;
                    totalDim.height += temp.height + verticalSpacing;
                    temp.height = 0;
                }

            }

            if ((numChildren % nb_colums) != 0) {
                totalDim.height += temp.height + verticalSpacing;
                temp.height = 0;
            }

            totalDim.width += 2 * horizontalSpacing;
            totalDim.height += 2 * verticalSpacing;

            return totalDim;

        } else {
            return new Dimension(24, 11);
        }

    }
}
