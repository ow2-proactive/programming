package org.objectweb.proactive.ic2d.debug.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.objectweb.proactive.core.debug.stepbystep.BreakpointType;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class StepByStepFilterDialog extends Dialog {
    private Shell shell = null;
    private Tree tree;
    private Button okButton;
    private Button cancelButton;
    private WorldObject world;
    private ActiveObject activeObject;

    private Map<String, Boolean> checkedType;

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    /**
     * Constructor which create the dialog for the world
     * @param parent the shell parent
     * @param world the world
     */
    public StepByStepFilterDialog(Shell parent, WorldObject world) {
        this(parent, world, null);
    }

    /**
     * Constructor which create the dialog for an active object
     * @param parent the shell parent
     * @param ao the active object
     */
    public StepByStepFilterDialog(Shell parent, ActiveObject ao) {
        this(parent, null, ao);
    }

    /**
     * Constructor which initialize the dialog
     * @param parent the shell parent
     */
    private StepByStepFilterDialog(Shell parent, WorldObject world, ActiveObject ao) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        this.activeObject = ao;
        this.world = world;

        /* Init the display */
        Display display = getParent().getDisplay();

        /* Init the shell */
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Set Step by Step motion Delay");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        /* Label */
        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setText("Check/Uncheck a breakpoint to enable/disable it :");
        FormData titleLabelFormData = new FormData();
        titleLabelFormData.left = new FormAttachment(0, 0);
        titleLabel.setLayoutData(titleLabelFormData);

        /* Tree */
        this.tree = new Tree(shell, SWT.NO_BACKGROUND | SWT.CHECK);
        BreakpointType[] bptype = BreakpointType.getAllTypes();
        checkedType = new HashMap<String, Boolean>();
        for (int i = 0; i < bptype.length; i++) {
            TreeItem ti = new TreeItem(tree, SWT.NONE);
            ti.setText(bptype[i].name());
            if (i != 0) {
                ti.setChecked(true); // Doesn't check the "send request" by default
                checkedType.put(ti.getText(), new Boolean(true));
            } else {
                ti.setChecked(false);
                checkedType.put(ti.getText(), new Boolean(false));
            }

        }

        tree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    Boolean value = new Boolean(true);
                    if (checkedType.get(((TreeItem) event.item).getText()) != null) {
                        value = new Boolean(!checkedType.get(((TreeItem) event.item).getText())
                                .booleanValue());
                    }
                    checkedType.put(((TreeItem) event.item).getText(), value);
                }
            }

        });
        FormData treeFormData = new FormData();
        treeFormData.top = new FormAttachment(titleLabel, 5);
        treeFormData.left = new FormAttachment(15, 15);
        tree.setLayoutData(treeFormData);

        // button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new StepByStepDelayListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(tree, 20);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new StepByStepDelayListener());
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(tree, 20);
        cancelFormData.left = new FormAttachment(50, 10);
        cancelFormData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(cancelFormData);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    /**
     * Listener for activate or disable filter
     */
    private class StepByStepDelayListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                BreakpointType[] disablePoint = createBreakpointTypeTable();
                // If we work on the world
                if (world != null) {
                    for (ActiveObject a : world.getAOChildren()) {
                        a.disableBreakpointTypes(disablePoint);
                    }
                } else {
                    // or just on an active object
                    if (activeObject != null) {
                        activeObject.disableBreakpointTypes(disablePoint);
                    }
                }

                shell.close();
            }
            if (e.widget == cancelButton) {
                shell.close();
            }
        }

        /**
         * Create a table with the breakpoint filter to disable
         * @return the table with the filter
         */
        private BreakpointType[] createBreakpointTypeTable() {
            BreakpointType[] bptype = BreakpointType.getAllTypes();
            List<String> items = new ArrayList<String>();
            for (String item : checkedType.keySet()) {
                if (checkedType.get(item).booleanValue()) {
                    items.add(item);
                }
            }
            BreakpointType[] res = new BreakpointType[bptype.length - items.size()];
            int ind = 0;
            for (int i = 0; i < bptype.length; i++) {
                if (!items.contains(bptype[i].name())) {
                    res[ind++] = bptype[i];
                }
            }
            return res;
        }
    }
}
