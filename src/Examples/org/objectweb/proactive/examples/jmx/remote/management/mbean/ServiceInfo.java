/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.jmx.remote.management.mbean;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


public class ServiceInfo implements Serializable, IJmx, DynamicMBean, ServiceListener {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    /**
     *
     */
    private Object actualService;
    private transient ServiceReference serviceReference;
    private BundleInfo bundleParent;
    private ObjectName on;
    private String path;
    private HashMap<String, Object> properties;

    public ServiceInfo(BundleInfo bundleParent, ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
        this.properties = new HashMap<String, Object>();
        this.bundleParent = bundleParent;
        copyService();
        this.path = this.bundleParent.getPath() + ",category=services";

        try {
            this.on = new ObjectName(this.bundleParent.getPath() + ",bundleName=" +
                this.bundleParent.getName() + ",category=services,className=" +
                this.actualService.getClass().getName());
            register();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    private void copyService() {
        String[] keys = this.serviceReference.getPropertyKeys();
        for (int i = 0; i < keys.length; i++) {
            Object value = this.serviceReference.getProperty(keys[i]);
            this.properties.put(keys[i], value);
        }
        this.actualService = OSGiStore.getInstance().getContext().getService(this.serviceReference);
    }

    public void register() throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.on);
    }

    public void unregister() throws InstanceNotFoundException, MBeanRegistrationException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.on);
    }

    /* implementation of Dynamic MBean */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        return this.properties.get(attribute).toString();
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList al = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            al.add(new Attribute(attributes[i], this.properties.get(attributes[i]).toString()));
        }
        return al;
    }

    private MBeanAttributeInfo[] getAttributesInfos() {
        MBeanAttributeInfo[] attInfos = new MBeanAttributeInfo[this.properties.keySet().size()];
        Iterator<String> i = this.properties.keySet().iterator();
        int j = 0;
        while (i.hasNext()) {
            String attribute = i.next();
            MBeanAttributeInfo info = new MBeanAttributeInfo(attribute, this.properties.get(attribute)
                    .getClass().getName(), attribute, true, false, false);
            attInfos[j++] = info;
        }
        return attInfos;
    }

    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attributes = getAttributesInfos();
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[] {};
        MBeanOperationInfo[] operations = new MBeanOperationInfo[] {};
        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[] {};
        MBeanInfo mbi = new MBeanInfo(this.getClass().getName(), "Mbean matching an OSGi service",
            attributes, constructors, operations, notifications);
        return mbi;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
            ReflectionException {
        return null;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    /**
     * Implementation of ServiceListener interface
     */
    public void serviceChanged(ServiceEvent event) {
    }
}
