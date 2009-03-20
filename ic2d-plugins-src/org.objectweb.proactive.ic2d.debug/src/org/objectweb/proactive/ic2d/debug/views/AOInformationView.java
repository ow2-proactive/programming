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
package org.objectweb.proactive.ic2d.debug.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.debug.stepbystep.BreakpointType;
import org.objectweb.proactive.core.debug.stepbystep.DebugBreakpointInfo;
import org.objectweb.proactive.core.debug.stepbystep.DebugInfo;
import org.objectweb.proactive.ic2d.debug.actions.ClearAoInformationViewAction;
import org.objectweb.proactive.ic2d.debug.actions.CollapseAllAoInformationViewAction;
import org.objectweb.proactive.ic2d.debug.actions.ExpandAllAoInformationViewAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;


public class AOInformationView extends ViewPart {

    /* The view ID */
    public static final String ID = "org.objectweb.proactive.ic2d.jmxmonitoring.view.AOStateProperties";

    /* This view is a singleton */
    private static AOInformationView singleton;

    /* Tree for displaying information */
    private Tree tree;

    /* Map containing the tree's items */
    private Map<UniqueID, TreeItem> treeItems = new HashMap<UniqueID, TreeItem>();

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    /*
     * The constructor
     */

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //
    /*
     * Return the instance of this view singleton
     *
     * @return
     */
    //    public static synchronized AOInformationView getInstance() {
    //        if (singleton == null)
    //            singleton = new AOInformationView();
    //        return singleton;
    //    }
    /*
     * Create the view's part
     */
    @Override
    public void createPartControl(Composite parent) {

        //        parent.setLayout(new FormLayout());

        // Toolbar Manager
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        ExpandAllAoInformationViewAction toolbarExpandAction = new ExpandAllAoInformationViewAction(this);
        CollapseAllAoInformationViewAction toolbarCollapseAction = new CollapseAllAoInformationViewAction(
            this);
        ClearAoInformationViewAction toolbarCleanAction = new ClearAoInformationViewAction(this);

        toolBarManager.add(toolbarExpandAction);
        toolBarManager.add(toolbarCollapseAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(toolbarCleanAction);
        toolBarManager.add(new Separator());

        // Set a legend label
        Label legendLabel = new Label(parent, SWT.NONE);
        legendLabel
                .setText("Click on an Active Object to see its informations.\nTo go to the next breakpoint, clik on the breakpoint type.");
        FormData legendLabelFormData = new FormData();
        legendLabelFormData.left = new FormAttachment(0, 0);
        legendLabel.setLayoutData(legendLabelFormData);

        // Set the tree
        tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        // Set the listener which will unblock a breakpoint on click

        tree.addListener(SWT.MouseDown, new Listener() {

            public void handleEvent(Event event) {
                Point point = new Point(event.x, event.y);
                TreeItem item = tree.getItem(point);
                if (item != null && item.getData() != null) {
                    AOContainer aoContainer = (AOContainer) item.getData();
                    aoContainer.getAo().nextStep(aoContainer.getBreakpoint().getBreakpointId());
                    populateTree(aoContainer.getAo());
                }
            }
        });

        FormData treeData = new FormData();
        treeData.top = new FormAttachment(legendLabel, 2);
        treeData.bottom = new FormAttachment(100, 0);
        tree.setLayoutData(treeData);

        // Set the tree's columns : Active Object Information | Value
        TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText("Active Object");
        column1.setWidth(210);
        TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
        column2.setText("Value");
        column2.setWidth(1024);

        /* -------------------------------- */

        // Set the child as the scrolled content of the ScrolledComposite
        //sc.setContent(child);
        // Set the minimum size
        //child.setSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        //sc.setMinSize(child.getSize().x, child.getSize().y);
        // Expand both horizontally and vertically
        //sc.setExpandHorizontal(true);
        //sc.setExpandVertical(true);
    }

    @Override
    public void setFocus() {
        /* Do Nothing */
    }

    /*
     * Get information from the AbstractData given and display it in the view
     *
     * @param data
     */
    @SuppressWarnings("unchecked")
    public void selectItem(AbstractData data) {
        collapseAll();
        if (data instanceof ActiveObject) {
            populateTree((ActiveObject) data);
            TreeItem item = treeItems.get(((ActiveObject) data).getUniqueID());
            tree.setSelection(item);
        }
    }

    /*
     * Create the item in the tree with the wanted information from an ActiveObject
     *
     * @param ao Active Object from which the information are gotten
     */
    private void populateTree(final ActiveObject ao) {
        final DebugInfo info = ao.getDebugInfo();
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                TreeItem item = treeItems.get(ao.getUniqueID());
                if (item == null) {
                    item = new TreeItem(tree, SWT.NONE);
                    treeItems.put(ao.getUniqueID(), item);
                }
                item.setText(new String[] { ao.getName() });
                item.removeAll();

                // Id
                TreeItem subItem0 = new TreeItem(item, SWT.NONE);
                subItem0.setText(new String[] { "Id", "" + info.getActiveObjectId() });

                // Node URL
                TreeItem subItem1 = new TreeItem(item, SWT.NONE);
                subItem1.setText(new String[] { "Host URL", info.getNodeUrl() });

                // Step by Step activate ?
                TreeItem subItem2 = new TreeItem(item, SWT.NONE);
                subItem2.setText(new String[] { "StepByStep",
                        (info.isStepByStepMode()) ? "Enabled" : "Disabled" });

                // Blocked in Breakpoint ?
                if (info.isBlockedInBreakpoint()) {
                    int i = 0;
                    for (DebugBreakpointInfo bp : info.getDebugBreakpointInfo()) {
                        // + Breakpoint #i
                        TreeItem subItem = new TreeItem(item, SWT.NONE);
                        subItem.setText(new String[] { "Breakpoint #" + i++ });
                        // Breakpoint
                        TreeItem subsubItem1 = new TreeItem(subItem, SWT.NONE);
                        subsubItem1.setData(new AOContainer(ao, bp));
                        subsubItem1.setText(new String[] {
                                "Blocked on breakpoint",
                                bp.getBreakpointType().toString() + ((bp.isImmediate()) ? " (IS)" : "") +
                                    " (click here to go to the next step)" });
                        // Method
                        if (bp.getMethodName() != null) {
                            TreeItem subsubItem2 = new TreeItem(subItem, SWT.NONE);
                            subsubItem2.setText(new String[] { "On method",
                                    bp.getMethodName() + ((bp.isImmediate()) ? " (IS)" : "") });
                        }
                        subItem.setBackground(new Color(Display.getCurrent(), 245, 245, 245));
                        //subItem.setExpanded(true);
                    }
                } else {
                    TreeItem subItem = new TreeItem(item, SWT.NONE);
                    subItem.setText(new String[] { "", "Not blocked on a breakpoint..." });
                }
                // List of breakpoint filter enabled
                boolean first = true;
                String res = "";
                for (BreakpointType filter : info.getBreakpointTypeFilter()) {
                    if (first) {
                        first = false;
                    } else {
                        res += ", ";
                    }
                    res += filter;
                }
                TreeItem subItem5 = new TreeItem(item, SWT.NONE);
                subItem5.setText(new String[] { "List of Breakpoints enabled", res });
                item.setBackground(new Color(Display.getCurrent(), 230, 230, 230));
                item.setExpanded(true);
                for (TreeItem it : item.getItems()) {
                    it.setExpanded(true);
                }
            }
        });

    }

    public void clearAoInformationView() {
        tree.removeAll();
        treeItems.clear();
    }

    public void expandAll() {
        for (final TreeItem item : tree.getItems()) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    item.setExpanded(true);
                }
            });
        }
    }

    public void collapseAll() {
        for (final TreeItem item : tree.getItems()) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    item.setExpanded(false);
                }
            });
        }
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //

    /*
     * Container for an active object and one of its breakpoint
     */
    private class AOContainer {

        private ActiveObject ao;
        private DebugBreakpointInfo breakpoint;

        public AOContainer(ActiveObject ao, DebugBreakpointInfo breakpoint) {
            this.ao = ao;
            this.breakpoint = breakpoint;
        }

        public ActiveObject getAo() {
            return ao;
        }

        public DebugBreakpointInfo getBreakpoint() {
            return breakpoint;
        }

        public String toString() {
            return ao.toString() + " : " + breakpoint.getBreakpointId();
        }

    }

}
