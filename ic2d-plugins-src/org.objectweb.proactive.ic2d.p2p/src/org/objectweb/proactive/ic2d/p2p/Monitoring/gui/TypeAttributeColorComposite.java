package org.objectweb.proactive.ic2d.p2p.Monitoring.gui;

import java.awt.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;


public class TypeAttributeColorComposite {

    protected Label typeLabel;
    protected String type;

    protected Composite comp;

    protected Combo peerAttribute;
    protected String attribute;

    protected Label colorLabel;
    protected Label minLabel;
    protected Text min;
    protected Label maxLabel;
    protected Text max;

    protected NetworkColorScheme nColor;

    public TypeAttributeColorComposite(Composite parent, int style, NetworkColorScheme n) {
        this.comp = new Composite(parent, style);

        this.nColor = n;
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginRight = 10;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.pack = false;
        rowLayout.justify = false;
        this.comp.setLayout(rowLayout);

        this.typeLabel = new Label(comp, style);
        // this.createComboComposite();
        peerAttribute = new Combo(comp, SWT.READ_ONLY);

        peerAttribute.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                update();

            }

            public void widgetSelected(SelectionEvent e) {
                update();

            }

        });

        minLabel = new Label(comp, SWT.NONE);
        minLabel.setText("min ");
        min = new Text(comp, SWT.BORDER);
        min.setLayoutData(new RowData(50, 18));
        min.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    System.out.println(".keyTraversed()");
                    pushDataMinToScheme();
                }
            }
        });
        min.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {
                pushDataMinToScheme();
            }

        });

        // min.set
        maxLabel = new Label(comp, SWT.NONE);
        maxLabel.setText("max ");
        max = new Text(comp, SWT.BORDER);
        max.setLayoutData(new RowData(50, 18));

        max.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    System.out.println(".keyTraversed()");
                    pushDataMaxToScheme();
                }
            }
        });
        max.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {
                pushDataMaxToScheme();
            }

        });

        // max.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
        // GridData.GRAB_HORIZONTAL));
        this.createColorLabel(comp);
        max.setText(this.getDataMaxFromScheme() + "");
        this.comp.pack();
    }

    public void setType(String s) {
        this.typeLabel.setText(s);
        this.type = s;
    }

    public String getType() {
        return this.type;
        // return this.typeLabel.getText();
    }

    public String getSelectedAttribute() {
        return this.attribute;
        //        int index = peerAttribute.getSelectionIndex();
        //        if (index >= 0) {
        //            return peerAttribute.getItem(index);
        //        }
        //        return null;
    }

    protected String[] getSelections() {
        // String ty = typeLabel.getText();
        int index = peerAttribute.getSelectionIndex();
        if (index >= 0) {
            String att = peerAttribute.getItem(index);
            return new String[] { type, att };
        } else {
            return null;
        }
    }

    /**
     * Look for the color set by the current NetworkColorScheme for the corresponding type+attribute
     * value
     */
    protected void setColorFromScheme() {
        String[] select = null;
        if ((select = getSelections()) != null) {
            // String att = peerAttribute.getItem(index);
            Color col = this.nColor.getBaseColor(select[0], select[1]);
            colorLabel.setBackground(new org.eclipse.swt.graphics.Color(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell().getDisplay(), col.getRed(), col.getGreen(), col
                    .getBlue()));
        } else {

            colorLabel.setBackground(new org.eclipse.swt.graphics.Color(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell().getDisplay(), 255, 255, 255));
        }
    }

    /**
     * Set the chosen value in the NetworkColorScheme
     */
    protected void pushColorToScheme(Color col) {
        // first get the current selection for type and attribute
        // String ty = typeLabel.getText();
        String att = peerAttribute.getItem(peerAttribute.getSelectionIndex());
        System.out.println("TypeAttributeColorComposite.pushColorToScheme() setting color for " + type + att +
            " to " + col);
        // this.nColor.setBaseColor(ty + att, col);
        this.nColor.setBaseColor(type, att, col);

    }

    protected float getDataMaxFromScheme() {
        String[] select = null;
        if ((select = getSelections()) != null) {
            return this.nColor.getDataMax(select[0], select[1]);
        }
        return 0;
    }

    protected void pushDataMaxToScheme() {
        float f = Float.parseFloat(max.getText());
        String[] select = null;
        if ((select = getSelections()) != null) {
            this.nColor.setDataMax(select[0], select[1], f);
        }

    }

    protected float getDataMinFromScheme() {
        String[] select = null;
        if ((select = getSelections()) != null) {
            return this.nColor.getDataMin(select[0], select[1]);
        }
        return 0;
    }

    protected void pushDataMinToScheme() {
        float f = Float.parseFloat(min.getText());
        String[] select = null;
        if ((select = getSelections()) != null) {
            this.nColor.setDataMin(select[0], select[1], f);
        }

    }

    protected void createColorLabel(Composite parent) {
        colorLabel = new Label(parent, SWT.BORDER);
        colorLabel.setLayoutData(new RowData(50, 18));
        this.setColorFromScheme();
        colorLabel.addMouseListener(new MouseListener() {
            public void mouseDown(MouseEvent e) {
                ColorDialog dlg = new ColorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getShell());

                // Set the selected color in the dialog from
                // user's selected color
                dlg.setRGB(colorLabel.getBackground().getRGB());

                // Change the title bar text
                dlg.setText("Choose a Color");

                // Open the dialog and retrieve the selected color
                RGB rgb = dlg.open();
                if (rgb != null) {
                    // Dispose the old color, create the
                    // new one, and set into the label
                    // color.dispose();
                    // color = n
                    colorLabel.setBackground(new org.eclipse.swt.graphics.Color(PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell().getDisplay(), rgb));
                    System.out.println(".mouseDown() " + rgb);
                    pushColorToScheme(new Color(rgb.red, rgb.green, rgb.blue));
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {

            }
        });
    }

    protected void addPeerAttribute(String s) {

        boolean contains = this.contains(s, peerAttribute);
        if (!contains) {
            peerAttribute.add(s);
            peerAttribute.setText(peerAttribute.getItem(0));
            comp.pack();
        }
    }

    protected boolean contains(String s, Combo c) {
        boolean contains = false;
        String[] items = c.getItems();
        for (int i = 0; i < items.length; i++) {
            String string = items[i];
            if (string.equals(s)) {
                contains = true;
            }
        }
        return contains;
    }

    protected void update() {
        System.out.println("TypeAttributeColorComposite.update()");
        int index = peerAttribute.getSelectionIndex();
        if (index >= 0) {
            this.attribute = peerAttribute.getItem(index);
        }
        this.setColorFromScheme();
        min.setText(this.getDataMinFromScheme() + "");
        max.setText(this.getDataMaxFromScheme() + "");
    }

}
