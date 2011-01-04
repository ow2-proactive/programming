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
package org.objectweb.proactive.core.component.type;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of {@link ComponentType}.
 *
 * @author The ProActive Team
 */
public class PAComponentTypeImpl implements ComponentType, PAGCMInterfaceType, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * The types of the interfaces of components of this type.
     */
    private final InterfaceType[] interfaceTypes;

    /**
     * Constructor for PAComponentTypeImpl.
     */
    public PAComponentTypeImpl(final InterfaceType[] interfaceTypes) throws InstantiationException {
        this.interfaceTypes = clone(interfaceTypes);
        // verifications
        for (int i = 0; i < interfaceTypes.length; ++i) {
            String p = interfaceTypes[i].getFcItfName();
            boolean collection = interfaceTypes[i].isFcCollectionItf();
            for (int j = i + 1; j < interfaceTypes.length; ++j) {
                String q = interfaceTypes[j].getFcItfName();
                if (p.equals(q)) {
                    throw new InstantiationException("Two interfaces have the same name '" + q + "'");
                }
                if (collection && q.startsWith(p)) {
                    throw new InstantiationException("The name of the interface '" + q + "' starts with '" +
                        p + "', which is the name of a collection interface");
                }
                if (interfaceTypes[j].isFcCollectionItf() && p.startsWith(q)) {
                    throw new InstantiationException("The name of the interface '" + p + "' starts with '" +
                        q + "', which is the name of a collection interface");
                }
            }
        }
    }

    /**
     * Seems doesn't be used
     * copy constructor
     */
    //    public PAComponentTypeImpl(final ComponentType componentType) {
    //        InterfaceType[] tempItfTypes = componentType.getFcInterfaceTypes();
    //        this.interfaceTypes = new InterfaceType[tempItfTypes.length];
    //        for (int i = 0; i < interfaceTypes.length; i++) {
    //            // deep copy
    //            interfaceTypes[i] = new PAInterfaceTypeImpl(tempItfTypes[i]);
    //        }
    //    }
    /**
     * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceTypes()
     */
    public InterfaceType[] getFcInterfaceTypes() {
        return interfaceTypes;
    }

    /**
     * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceType(String)
     */
    public InterfaceType getFcInterfaceType(String itfName) throws NoSuchInterfaceException {
        for (int i = 0; i < interfaceTypes.length; i++) {
            if (interfaceTypes[i].isFcCollectionItf()) {
                if (itfName.startsWith(interfaceTypes[i].getFcItfName()) &&
                    !itfName.equals(interfaceTypes[i].getFcItfName())) {
                    return interfaceTypes[i];
                }
            } else {
                if (itfName.equals(interfaceTypes[i].getFcItfName())) {
                    return interfaceTypes[i];
                }
            }
        }
        throw new NoSuchInterfaceException(itfName);
    }

    /**
     * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
     */
    public boolean isFcSubTypeOf(Type type) {
        throw new RuntimeException("not yet implemented");
    }

    /**
     * Returns a copy of the given interface type array. This method is used to
     * return a copy of the field of this class, instead of the field itself, so
     * that its content can not be modified from outside.
     *
     * @param types the array to be cloned.
     * @return a clone of the given array, or an empty array if <tt>type</tt> is
     *      <tt>null</tt>.
     */
    private static InterfaceType[] clone(final InterfaceType[] types) {
        if (types == null) {
            return new InterfaceType[0];
        } else {
            InterfaceType[] clone = new InterfaceType[types.length];
            System.arraycopy(types, 0, clone, 0, types.length);
            return clone;
        }
    }

    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    public String getFcItfSignature() {
        return PAComponent.class.getName();
    }

    public boolean isFcClientItf() {
        return false;
    }

    public boolean isFcCollectionItf() {
        return false;
    }

    public boolean isFcOptionalItf() {
        return false;
    }

    public boolean isGCMSingletonItf() {
        return true;
    }

    public boolean isGCMCollectionItf() {
        return false;
    }

    public String getGCMCardinality() {
        return GCMTypeFactory.SINGLETON_CARDINALITY;
    }

    public boolean isGCMGathercastItf() {
        return false;
    }

    public boolean isGCMMulticastItf() {
        return false;
    }

    public boolean isGCMCollectiveItf() {
        return false;
    }

    public boolean isStreamItf() {
        return false;
    }

    public boolean isInternal() {
        return false;
    }
}
