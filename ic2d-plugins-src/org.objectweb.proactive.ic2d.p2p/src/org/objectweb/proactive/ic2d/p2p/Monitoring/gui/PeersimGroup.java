package org.objectweb.proactive.ic2d.p2p.Monitoring.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;


public class PeersimGroup {

    protected Group group;
    protected Label fileLabel;
    protected Label fileNameLabel;
    protected PeerAttributeColorComposite colorComposite;
    protected Composite peerComposite;

    public PeersimGroup(Composite parent, int style) {
        group = new Group(parent, style);
        group.setText("Peersim Log");
        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.VERTICAL;
        group.setLayout(rowLayout);
        //        GridLayout gridLayout = new GridLayout();
        //        gridLayout.numColumns = 2;
        //        group.setLayout(gridLayout);

        fileLabel = new Label(group, SWT.SHADOW_IN);
        fileNameLabel = new Label(group, SWT.SHADOW_IN);
        this.colorComposite = new PeerAttributeColorComposite(group, SWT.NONE);
        // peerComposite = .getComposite();
        // group.setLayout(new RowLayout(SWT.VERTICAL));

        this.test();
        group.pack();
    }

    protected void test() {
        this.fileLabel.setText("File : ");
        this.fileNameLabel.setText(" ");
    }

    public void setFileName(String s) {
        this.fileNameLabel.setText(s);
    }

    public Group getGroup() {
        return group;
    }

    public PeerAttributeColorComposite getAttributeColorComposite() {
        return this.colorComposite;
    }

    public void setColorScheme(NetworkColorScheme colorScheme) {
        this.colorComposite.setColorScheme(colorScheme);
    }

}
