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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.scheduler;

import java.util.ArrayList;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.services.SchedulerLookupService;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorHandler;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


/**
 * This is the main class used for parsing the jobs submitted with the xml deployment
 * descriptor file. This class will launch the parsing of the file and the extraction of
 * the descriptions of the job.
 * @author cjarjouh
 *
 */
public class ProActiveJobHandler extends AbstractUnmarshallerDecorator
    implements ProActiveDescriptorConstants {
    private ProActiveDescriptorHandler proActiveDescriptorHandler;
    private String jobId;
    private Scheduler scheduler;

    public ProActiveJobHandler(Scheduler scheduler, String jobId,
        String xmlDescriptorUrl) {
        super();
        this.scheduler = scheduler;
        this.jobId = jobId;
        proActiveDescriptorHandler = new ProActiveDescriptorHandler(scheduler,
                jobId, xmlDescriptorUrl);
        this.addHandler(PROACTIVE_DESCRIPTOR_TAG, proActiveDescriptorHandler);
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
        return null;
    }

    public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
    }

    /**
     * we redefine this method so that we can collect in the end the total
     * amount of information from the created Virtual nodes. Like for instance
     * the total amount of needed ressources.
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        ProActiveDescriptor pad = (ProActiveDescriptor) activeHandler.getResultObject();

        int nodeNb = 0;
        int minNodeNb = 0;

        VirtualNode[] vns = pad.getVirtualNodes();
        for (int i = 0; i < vns.length; ++i) {
            VirtualNode vn = vns[i];
            ArrayList vms = ((VirtualNodeImpl) vn).getVirtualMachines();
            for (int j = 0; j < vms.size(); ++j) {
                VirtualMachine vm = (VirtualMachine) vms.get(j);

                UniversalService service = vm.getService();
                if (service.getServiceName()
                               .equals(SchedulerConstants.SCHEDULER_NODE_NAME)) {
                    SchedulerLookupService schedulerLookupService = ((SchedulerLookupService) service);
                    nodeNb += schedulerLookupService.getNodeNumber();
                    minNodeNb += schedulerLookupService.getMinNodeNumber();
                }
            }
        }
        GenericJob job = scheduler.getTmpJob(jobId);
        job.setRessourceNb(nodeNb);
        job.setMinNbOfNodes(minNodeNb);
        scheduler.commit(jobId);
    }
}
