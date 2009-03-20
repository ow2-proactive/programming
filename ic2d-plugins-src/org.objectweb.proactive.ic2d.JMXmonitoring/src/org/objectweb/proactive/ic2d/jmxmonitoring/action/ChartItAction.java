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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ProActiveNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VirtualNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


/**
 * This action allows the user to open a ChartIt editor using as input a
 * resource descriptor based on an
 * {@link org.objectweb.proactive.ic2d.data.AbstractData}.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartItAction extends Action implements IActionExtPoint {

    /**
     * The text displayed by this action
     */
    public static final String SHOW_IN_CHARTIT_VIEW_ACTION = "Show in ChartIt";

    public static final String PARUNTIME_CHARTIT_CONFIG_FILENAME = "predef_paruntime_chartit_conf.xml";

    /**
     * The target data
     */
    private AbstractData<?, ?> target;

    /**
     * Creates a new instance of <code>ChartItAction</code>.
     */
    public ChartItAction() {
        super.setId(SHOW_IN_CHARTIT_VIEW_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.chartit.Activator.getDefault().getBundle(), new Path(
                    "icons/graph.gif"), null)));
        super.setToolTipText(SHOW_IN_CHARTIT_VIEW_ACTION);
        super.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint#setAbstractDataObject(org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData)
     */
    public void setAbstractDataObject(final AbstractData<?, ?> object) {
        if (object.getClass() == HostObject.class)
            return;
        this.target = object;
        super.setText("Show " + object.getName() + " in ChartIt");
        super.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint#setActiveSelect(org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData)
     */
    public void setActiveSelect(final AbstractData<?, ?> ref) {
        this.handleData(ref, false);
    }

    /**
     * Handles incoming abstract data reference ie opens a new or existing
     * editor associated to the data.
     * 
     * @param abstractData
     *            The incoming abstract data
     * @param createNewIfNotFound
     *            Creates new editor if not found
     */
    private void handleData(final AbstractData<?, ?> abstractData, final boolean createNewIfNotFound) {
        try {
            if (abstractData == null)
                return;
            // Get the current window instance
            final IWorkbenchWindow currentWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (!activateIfFound(currentWindow, abstractData.getName()) && createNewIfNotFound) {
                // First build a ResourceDescriptor
                final IResourceDescriptor resourceDescriptor;
                // If the data is world object create adequate descriptor
                if (abstractData instanceof WorldObject) {
                    resourceDescriptor = new WorldObjectResourceDescriptor((WorldObject) abstractData);
                } else if (abstractData instanceof RuntimeObject) {
                    // For runtime objects
                    resourceDescriptor = new RuntimeObjectResourceDescriptor((RuntimeObject) abstractData);
                } else if (abstractData instanceof ProActiveNodeObject) {
                    // For node objects
                    resourceDescriptor = new NodeObjectResourceDescriptor((ProActiveNodeObject) abstractData);
                } else if (abstractData instanceof ActiveObject) {
                    // For active objects
                    resourceDescriptor = new ActiveObjectResourceDescriptor((ActiveObject) abstractData);
                } else {
                    // For other objects that are not known 
                    resourceDescriptor = new AbstractDataDescriptor(abstractData);
                }
                // Open new editor based on the resource descriptor
                ChartItDataEditor.openNewFromResourceDescriptor(resourceDescriptor);
            }
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME)
                    .log(
                            "Could not open the editor for " + this.target.getName() + " message : " +
                                e.getMessage());
        }
    }

    /**
     * Activates an editor by name.
     * 
     * @param currentWindow
     *            The current window
     * @param name
     *            The name of the editor to activate
     * @return <code>True</code> if the existing editor was activated,
     *         <code>False</code> otherwise
     * @throws PartInitException
     *             Thrown if the part can not be activated
     */
    private boolean activateIfFound(final IWorkbenchWindow currentWindow, final String name)
            throws PartInitException {
        // Navigate through EditorReference->EditorInput then find the
        // Editor through ActivePage.findEditor(editorInputRef)
        // First list all EditorReferences
        for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
            if (ref.getEditorInput().getName().equals(name)) {
                // If the Editor input was found activate it
                currentWindow.getActivePage().activate(
                        currentWindow.getActivePage().findEditor(ref.getEditorInput()));
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // Handle the current target
        this.handleData(this.target, true);
    }

    /**
     * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
     * 
     */
    final class AbstractDataDescriptor implements IResourceDescriptor {
        /**
         * The abstract data described as a resource
         */
        final AbstractData<?, ?> abstractData;

        /**
         * Some custom data providers
         */
        final IDataProvider[] customProviders;

        /**
         * Creates a new instance of <code>AbstractDataDescriptor</code>
         * 
         * @param abstractData
         *            The abstract data described as a resource
         * @throws IOException
         *             Thrown if a problem occurred during custom providers
         *             creation
         */
        public AbstractDataDescriptor(final AbstractData<?, ?> abstractData) throws IOException {
            this.abstractData = abstractData;
            this.customProviders = new IDataProvider[0];
        }

        public String getHostUrlServer() {
            return this.abstractData.getHostUrlServer();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.abstractData.getMBeanServerConnection();
        }

        public String getName() {
            return this.abstractData.getName();
        }

        public ObjectName getObjectName() {
            return this.abstractData.getObjectName();
        }

        public IDataProvider[] getCustomDataProviders() {
            return this.customProviders;
        }

        public String getConfigFilename() {
            return null;
        }
    }

    final class RuntimeObjectResourceDescriptor implements IResourceDescriptor {
        final RuntimeObject runtimeObject;

        public RuntimeObjectResourceDescriptor(final RuntimeObject runtimeObject) {
            this.runtimeObject = runtimeObject;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] { new NodesCountFromRuntimeObject(this.runtimeObject) };
        }

        public String getHostUrlServer() {
            return this.runtimeObject.getHostUrlServer();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.runtimeObject.getMBeanServerConnection();
        }

        public String getName() {
            return this.runtimeObject.getName();
        }

        public ObjectName getObjectName() {
            return this.runtimeObject.getObjectName();
        }

        public String getConfigFilename() {
            return "predef_paruntime_chartit_conf.xml";
        }
    }

    final class NodesCountFromRuntimeObject implements IDataProvider {
        final RuntimeObject runtimeObject;

        public NodesCountFromRuntimeObject(final RuntimeObject runtimeObject) {
            this.runtimeObject = runtimeObject;
        }

        public String getDescription() {
            return "The number of node objects (based on monitoring collected data)";
        }

        public String getName() {
            return "NodesCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.runtimeObject.getMonitoredChildrenSize();
        }
    }

    final class ActiveObjectResourceDescriptor implements IResourceDescriptor {
        final ActiveObject activeObject;

        public ActiveObjectResourceDescriptor(ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] {
                    new IncomingCommunicationChannelsCountFromActiveObject(this.activeObject),
                    new OutgoingCommunicationChannelsCountFromActiveObject(this.activeObject),
                    new RequestQueueLengthFromActiveObject(this.activeObject) };
        }

        public String getHostUrlServer() {
            return this.activeObject.getHostUrlServer();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.activeObject.getMBeanServerConnection();
        }

        public String getName() {
            return this.activeObject.getName();
        }

        public ObjectName getObjectName() {
            return this.activeObject.getObjectName();
        }

        public String getConfigFilename() {
            return "predef_ao_chartit_conf.xml";
        }
    }

    final class IncomingCommunicationChannelsCountFromActiveObject implements IDataProvider {
        final ActiveObject activeObject;

        public IncomingCommunicationChannelsCountFromActiveObject(final ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public String getDescription() {
            return "The number of incoming communication channels (based on monitoring collected data)";
        }

        public String getName() {
            return "IncomingCommunicationChannelsCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.activeObject.getIncomingCommunications().size();
        }
    }

    final class OutgoingCommunicationChannelsCountFromActiveObject implements IDataProvider {
        final ActiveObject activeObject;

        public OutgoingCommunicationChannelsCountFromActiveObject(final ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public String getDescription() {
            return "The number of outgoing communication channels (based on monitoring collected data)";
        }

        public String getName() {
            return "OutgoingCommunicationChannelsCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.activeObject.getOutgoingCommunications().size();
        }
    }

    final class RequestQueueLengthFromActiveObject implements IDataProvider {
        final ActiveObject activeObject;

        public RequestQueueLengthFromActiveObject(final ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public String getDescription() {
            return "The length of the request queue (based on monitoring collected data)";
        }

        public String getName() {
            return "RequestQueueLength";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.activeObject.getRequestQueueLength();
        }
    }

    final class NodeObjectResourceDescriptor implements IResourceDescriptor {
        final ProActiveNodeObject nodeObject;

        public NodeObjectResourceDescriptor(ProActiveNodeObject nodeObject) {
            this.nodeObject = nodeObject;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] { new ActiveObjectsCountFromNodeObject(this.nodeObject) };
        }

        public String getHostUrlServer() {
            return this.nodeObject.getHostUrlServer();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.nodeObject.getMBeanServerConnection();
        }

        public String getName() {
            return this.nodeObject.getName();
        }

        public ObjectName getObjectName() {
            return this.nodeObject.getObjectName();
        }

        public String getConfigFilename() {
            return "predef_panode_chartit_conf.xml";
        }
    }

    final class ActiveObjectsCountFromNodeObject implements IDataProvider {
        final ProActiveNodeObject nodeObject;

        public ActiveObjectsCountFromNodeObject(final ProActiveNodeObject nodeObject) {
            this.nodeObject = nodeObject;
        }

        public String getDescription() {
            return "The number of registered active objects (based on monitoring collected data)";
        }

        public String getName() {
            return "ActiveObjectsCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.nodeObject.getMonitoredChildrenSize();
        }
    }

    final class WorldObjectResourceDescriptor implements IResourceDescriptor {
        final WorldObject worldObject;

        public WorldObjectResourceDescriptor(WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] { new HostsCountFromWorldObject(this.worldObject),
                    new RuntimesCountFromWorldObject(this.worldObject),
                    new VirtualNodesCountFromWorldObject(this.worldObject),
                    new NodesCountFromWorldObject(this.worldObject),
                    new ActiveObjectsCountFromWorldObject(this.worldObject) };
        }

        public String getHostUrlServer() {
            return "localhost";
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return ManagementFactory.getPlatformMBeanServer();
        }

        public String getName() {
            return worldObject.getName();
        }

        public ObjectName getObjectName() {
            return null;
        }

        public String getConfigFilename() {
            return "predef_world_chartit_conf.xml";
        }
    }

    final class HostsCountFromWorldObject implements IDataProvider {
        final WorldObject worldObject;

        public HostsCountFromWorldObject(final WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public String getDescription() {
            return "The number of monitored hosts (based on monitoring collected data)";
        }

        public String getName() {
            return "HostsCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.worldObject.getNumberOfHosts();
        }
    }

    final class RuntimesCountFromWorldObject implements IDataProvider {
        final WorldObject worldObject;

        public RuntimesCountFromWorldObject(final WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public String getDescription() {
            return "The number of monitored runtimes (based on monitoring collected data)";
        }

        public String getName() {
            return "RuntimesCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.worldObject.getNumberOfJVMs();
        }

    }

    final class VirtualNodesCountFromWorldObject implements IDataProvider {
        final WorldObject worldObject;

        public VirtualNodesCountFromWorldObject(final WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public String getDescription() {
            return "The number of monitored virtual nodes (based on monitoring collected data)";
        }

        public String getName() {
            return "VirtualNodesCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.worldObject.getVNChildren().size();
        }
    }

    final class NodesCountFromWorldObject implements IDataProvider {
        final WorldObject worldObject;

        public NodesCountFromWorldObject(final WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public String getDescription() {
            return "The number of monitored node objects (based on monitoring collected data)";
        }

        public String getName() {
            return "NodesCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            int n = 0;
            for (final VirtualNodeObject vnObject : this.worldObject.getVNChildren()) {
                n += vnObject.getMonitoredChildrenSize();
            }
            return n;
        }
    }

    final class ActiveObjectsCountFromWorldObject implements IDataProvider {
        final WorldObject worldObject;

        public ActiveObjectsCountFromWorldObject(final WorldObject worldObject) {
            this.worldObject = worldObject;
        }

        public String getDescription() {
            return "The number of monitored active objects (based on monitoring collected data)";
        }

        public String getName() {
            return "ActiveObjectsCount";
        }

        public String getType() {
            return "int";
        }

        public Object provideValue() {
            return this.worldObject.getNumberOfActiveObjects();
        }
    }
}