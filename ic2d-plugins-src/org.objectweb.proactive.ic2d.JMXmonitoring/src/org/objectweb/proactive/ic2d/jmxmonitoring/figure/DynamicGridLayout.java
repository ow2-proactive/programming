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
