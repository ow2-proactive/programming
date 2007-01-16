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

public interface SchedulerConstants {

    /**
    * <code>SCHEDULER_NODE_NAME</code>: name of the scheduler node where the service is deployed.
    */
    public static final String SCHEDULER_NODE_NAME = "SchedulerNode";

    // -------------------------------------------------------------------------
    // Java system properties names
    // -------------------------------------------------------------------------

    /** policy class name of the scheduler */
    public static final String POLICY_NAME = "proactive.scheduler.policy";

    /** scheduler url (protocol://host:port) */
    public static final String SCHEDULER_URL = "proactive.scheduler.url";

    /** jvm parameters */
    public static final String JVM_PARAMETERS = "proactive.scheduler.jvmParameters";

    //    /** xml path where the xml files are being deployed */
    //    public static final String XML_PATH = "proactive.scheduler.xmlPath";

    /** the job ID of the job to be run locally */
    public static final String JOB_ID = "proactive.scheduler.genericJob.jobId";

    /** the complete path of the XML Deployement Descriptor */
    public static final String XML_PATH = "XMLDescriptorFile";
}
