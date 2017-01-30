/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.mop;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;


/**
 * A reified constructor call.
 */
public class ConstructorCallImpl implements ConstructorCall, Serializable {

    /**
     * The array holding the arguments of the constructor
     */
    protected Object[] effectiveArguments;

    /**
     * The corresponding constructor object
     */
    protected Constructor<?> reifiedConstructor;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Effective constructor
     * @param reifiedConstructor the constructor object which is called
     * @param effectiveArguments the array holding the effective args
     */
    public ConstructorCallImpl(Constructor<?> reifiedConstructor, Object[] effectiveArguments) {
        this.reifiedConstructor = reifiedConstructor;
        this.effectiveArguments = effectiveArguments;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        String ls = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("ConstructorCallImpl");
        sb.append(ls).append("reifiedConstructor=").append(reifiedConstructor).append(ls);
        sb.append("effectiveArguments=");
        if (effectiveArguments == null) {
            sb.append("null").append(ls);
        } else {
            sb.append(ls);
            for (int i = 0; i < effectiveArguments.length; i++) {
                sb.append("   effectiveArguments[");
                sb.append(i);
                sb.append("]=");
                sb.append(effectiveArguments[i]);
                sb.append(ls);
            }
            sb.append(ls);
        }
        return sb.toString();
    }

    //
    // -- implements ConstructorCall -----------------------------------------------
    //

    /**
     * Make a deep copy of all arguments of the constructor
     */
    public void makeDeepCopyOfArguments() throws java.io.IOException {
        effectiveArguments = Utils.makeDeepCopy(effectiveArguments);
    }

    /**
     * Return the name of the target class that constructor is for
     */
    public String getTargetClassName() {
        return getReifiedClass().getName();
    }

    /**
     * Performs the object construction that is reified vy this object
     * @throws InvocationTargetException
     * @throws ConstructorCallExecutionFailedException
     */
    public Object execute() throws InvocationTargetException, ConstructorCallExecutionFailedException {
        // System.out.println("ConstructorCall: The constructor is " + reifiedConstructor); 
        try {
            // if the reified class is an member class, add the implicit parameter
            if (getReifiedClass().isMemberClass() && !Modifier.isStatic(getReifiedClass().getModifiers())) {
                Class<?> enclosingClass = getReifiedClass().getEnclosingClass();
                Object[] tmp = effectiveArguments;
                effectiveArguments = new Object[tmp.length + 1];
                effectiveArguments[0] = enclosingClass.newInstance();
                for (int i = 1; i < effectiveArguments.length; i++) {
                    effectiveArguments[i] = tmp[i - 1];
                }
            }
            return reifiedConstructor.newInstance(effectiveArguments);
        } catch (IllegalAccessException e) {
            throw new ConstructorCallExecutionFailedException("Access rights to the constructor denied: " + e);
        } catch (IllegalArgumentException e) {
            throw new ConstructorCallExecutionFailedException("Illegal constructor arguments: " + e);
        } catch (InstantiationException e) {
            if (getReifiedClass().isInterface()) {
                throw new ConstructorCallExecutionFailedException("Cannot build an instance of an interface: " + e);
            } else if (Modifier.isAbstract(getReifiedClass().getModifiers())) {
                throw new ConstructorCallExecutionFailedException("Cannot build an instance of an abstract class: " +
                                                                  e);
            } else {
                throw new ConstructorCallExecutionFailedException("Instanciation problem: " + e +
                                                                  ". Strange enough, the reified class is neither abstract nor an interface.");
            }
        } catch (ExceptionInInitializerError e) {
            throw new ConstructorCallExecutionFailedException("Cannot build object because the initialization of its class failed: " +
                                                              e);
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     *        Returns a <code>Class</code> object representing the type of
     * the object this reified constructor will build when reflected
     */
    protected Class<?> getReifiedClass() {
        return reifiedConstructor.getDeclaringClass();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // We want to implement a workaround the Constructor
        // not being Serializable
        out.writeObject(this.effectiveArguments);

        // Constructor needs to be converted because it is not serializable
        Class<?> declaringClass;
        Class<?>[] parameters;

        declaringClass = this.reifiedConstructor.getDeclaringClass();
        out.writeObject(declaringClass);

        parameters = this.reifiedConstructor.getParameterTypes();
        out.writeObject(parameters);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Class<?> declaringClass = null;
        Class<?>[] parameters;
        try {
            this.effectiveArguments = (Object[]) in.readObject();
        } catch (IOException e) {
            //  System.out.println("Stream is  " + in.getClass().getName());
            //    e.printStackTrace();
            throw e;
        }

        declaringClass = (Class<?>) in.readObject();
        parameters = (Class<?>[]) in.readObject();

        try {
            this.reifiedConstructor = declaringClass.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Lookup for constructor failed: " + e +
                                        ". This may be caused by having different versions of the same class on different VMs. Check your CLASSPATH settings.");
        }
    }

    public Object[] getEffectiveArguments() {
        return effectiveArguments;
    }

    public void setEffectiveArguments(Object[] effectiveArguments) {
        this.effectiveArguments = effectiveArguments;
    }

}
