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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.core.component.type.annotations.gathercast.MethodSynchro;
import org.objectweb.proactive.core.component.type.annotations.multicast.Reduce;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of {@link PAGCMInterfaceType}.
 *
 * @author The ProActive Team
 */
public class PAGCMInterfaceTypeImpl implements PAGCMInterfaceType, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;

    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * The generatedClassName of the interface described by this type.
     */
    private String name;

    /**
     * The Java type of the interface described by this type.
     */
    private String signature;
    private boolean isClient;
    private boolean isOptional;
    private boolean isInternal;
    private boolean isStream;
    private String cardinality;

    /**
     * Constructor for PAInterfaceTypeImpl.
     */
    public PAGCMInterfaceTypeImpl() {
        super();
    }

    /**
     * Constructor for PAInterfaceTypeImpl.
     */
    public PAGCMInterfaceTypeImpl(String name, String signature, boolean isClient, boolean isOptional,
            String cardinality) throws InstantiationException {
        this.name = name;
        this.signature = signature;
        this.isClient = isClient;
        this.isOptional = isOptional;
        this.isStream = checkIsStream(signature);
        this.cardinality = cardinality;
        checkMethodSignatures(signature, cardinality);
    }

    public PAGCMInterfaceTypeImpl(String name, String signature, boolean isClient, boolean isOptional,
            String cardinality, boolean isInternal) throws InstantiationException {
        this(name, signature, isClient, isOptional, cardinality);
        this.isInternal = isInternal;
    }

    private boolean checkIsStream(String signature) throws InstantiationException {
        try {
            Class<?> c = Class.forName(signature);
            return StreamInterface.class.isAssignableFrom(c);
        } catch (ClassNotFoundException e) {
            InstantiationException ie = new InstantiationException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    private void checkMethodSignatures(String signature, String cardinality) throws InstantiationException {
        checkStreamMethods(signature);
        checkMethodCardinalities(signature, cardinality);
    }

    private void checkStreamMethods(String signature) throws InstantiationException {
        try {
            if (isStream) {
                Class<?> c = Class.forName(signature);
                Method[] methods = c.getMethods();
                for (Method m : methods) {
                    if (!(Void.TYPE.equals(m.getReturnType()))) {
                        throw new InstantiationException("methods of a stream interface must return void, " +
                            "which is not the case for method " + m.toString() + " in interface " + signature);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            InstantiationException ie = new InstantiationException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    private void checkMethodCardinalities(String signature, String cardinality) throws InstantiationException {
        try {
            if (GCMTypeFactory.GATHERCAST_CARDINALITY.equals(cardinality)) {
                Class<?> c = Class.forName(signature);
                Method[] methods = c.getMethods();
                for (Method m : methods) {
                    MethodSynchro sc = m.getAnnotation(MethodSynchro.class);
                    if (sc != null) {
                        if ((!sc.waitForAll()) && (sc.timeout() != MethodSynchro.DEFAULT_TIMEOUT))
                            throw new InstantiationException(
                                "methods of a gathercast interface can not have a timeout if waitForAll is specified to false, " +
                                    "which is not the case for method " +
                                    m.toString() +
                                    " in interface " +
                                    signature);
                    }
                }
            } else if (GCMTypeFactory.MULTICAST_CARDINALITY.equals(cardinality)) {
                Class<?> c = Class.forName(signature);
                Method[] methods = c.getMethods();
                for (Method m : methods) {
                    if (m.getAnnotation(Reduce.class) == null) {
                        if (!(m.getGenericReturnType() instanceof ParameterizedType) &&
                            !(Void.TYPE.equals(m.getReturnType()))) {
                            throw new InstantiationException(
                                "methods of a multicast interface must return parameterized types or void, " +
                                    "which is not the case for method " + m.toString() + " in interface " +
                                    signature);
                        }
                    } else {
                        // removed constraint in order to allow reduction
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            InstantiationException ie = new InstantiationException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    // -------------------------------------------------------------------------
    // Implementation of the InterfaceType interface
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#getFcItfName()
     */
    public String getFcItfName() {
        return name;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#getFcItfSignature()
     */
    public String getFcItfSignature() {
        return signature;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcClientItf()
     */
    public boolean isFcClientItf() {
        return isClient;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcCollectionItf()
     */
    public boolean isFcCollectionItf() {
        return GCMTypeFactory.COLLECTION_CARDINALITY.equals(cardinality);
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcOptionalItf()
     */
    public boolean isFcOptionalItf() {
        return isOptional;
    }

    /**
     * TODO : provide implementation for isFcSubTypeOf
     * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
     */
    public boolean isFcSubTypeOf(final Type type) {
        throw new RuntimeException("Not yet implemented.");
    }

    public String getGCMCardinality() {
        return cardinality;
    }

    public boolean isGCMSingletonItf() {
        return GCMTypeFactory.SINGLETON_CARDINALITY.equals(cardinality);
    }

    public boolean isGCMCollectionItf() {
        return GCMTypeFactory.COLLECTION_CARDINALITY.equals(cardinality);
    }

    public boolean isGCMGathercastItf() {
        return GCMTypeFactory.GATHERCAST_CARDINALITY.equals(cardinality);
    }

    public boolean isGCMMulticastItf() {
        return GCMTypeFactory.MULTICAST_CARDINALITY.equals(cardinality);
    }

    public boolean isGCMCollectiveItf() {
        return (GCMTypeFactory.GATHERCAST_CARDINALITY.equals(cardinality) || (GCMTypeFactory.MULTICAST_CARDINALITY
                .equals(cardinality)));
    }

    public boolean isStreamItf() {
        return isStream;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public int hashCode() {
        return (this.getFcItfName() + this.getFcItfSignature() + this.isFcClientItf() +
            this.isFcOptionalItf() + this.isStreamItf() + this.isFcCollectionItf() +
            this.isGCMGathercastItf() + this.isGCMMulticastItf() + this.isGCMSingletonItf()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PAGCMInterfaceType) {
            PAGCMInterfaceType itf = (PAGCMInterfaceType) obj;
            return this.getFcItfName().equals(itf.getFcItfName()) &&
                this.getFcItfSignature().equals(itf.getFcItfSignature()) &&
                (this.isFcClientItf() == itf.isFcClientItf()) &&
                (this.isFcOptionalItf() == itf.isFcOptionalItf()) &&
                (this.isStreamItf() == itf.isStreamItf()) &&
                (this.isFcCollectionItf() == itf.isFcCollectionItf()) &&
                (this.isGCMGathercastItf() == itf.isGCMGathercastItf()) &&
                (this.isGCMMulticastItf() == itf.isGCMMulticastItf()) &&
                (this.isGCMSingletonItf() == itf.isGCMSingletonItf()) &&
                this.isInternal() == itf.isInternal();
        } else
            return false;
    }
}
