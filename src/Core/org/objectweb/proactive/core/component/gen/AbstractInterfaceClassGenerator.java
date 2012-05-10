/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.core.component.gen;

import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is the parent of classes for generating component interfaces. It provides utility methods that are used in subclasses.
 *
 * @author The ProActive Team
 */
public abstract class AbstractInterfaceClassGenerator {
    protected static final transient Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GEN_ITFS);
    protected static final ClassPool pool = ClassPool.getDefault();

    protected Class<?> loadClass(final String className) throws ClassNotFoundException {
        // try to fetch the class from the default class loader
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    public PAInterface generateControllerInterface(final String controllerInterfaceName, Component owner,
            PAGCMInterfaceType interfaceType) throws InterfaceGenerationFailedException {
        return generateInterface(controllerInterfaceName, owner, interfaceType, false, false);
    }

    public PAInterface generateFunctionalInterface(final String functionalInterfaceName, Component owner,
            PAGCMInterfaceType interfaceType) throws InterfaceGenerationFailedException {
        return generateInterface(functionalInterfaceName, owner, interfaceType, false, true);
    }

    public abstract PAInterface generateInterface(final String interfaceName, Component owner,
            PAGCMInterfaceType interfaceType, boolean isInternal, boolean isFunctionalInterface)
            throws InterfaceGenerationFailedException;

    /**
     * Gets all super-interfaces from the interfaces of this list, and
     * adds them to this list.
     * @param interfaces a list of interfaces
     */
    public static void addSuperInterfaces(List<CtClass> interfaces) throws NotFoundException {
        for (int i = 0; i < interfaces.size(); i++) {
            CtClass[] super_itfs_table = interfaces.get(i).getInterfaces();
            List<CtClass> super_itfs = new ArrayList<CtClass>(super_itfs_table.length); // resizable list
            for (int j = 0; j < super_itfs_table.length; j++) {
                super_itfs.add(super_itfs_table[j]);
            }
            addSuperInterfaces(super_itfs);
            CtClass super_itf;
            for (int j = 0; j < super_itfs.size(); j++) {
                if (!interfaces.contains(super_itfs.get(j))) {
                    super_itf = super_itfs.get(j);
                    if (!(super_itf.equals(pool.get(PAInterface.class.getName())) || super_itf.equals(pool
                            .get(Interface.class.getName())))) {
                        interfaces.add(super_itfs.get(j));
                    }
                }
            }
        }
    }
}
