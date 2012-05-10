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
package org.objectweb.proactive.extensions.webservices.common;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * This class supply methods to convert methods to be exposed (specified as arguments of
 * expose methods) into methods to be excluded.
 * During the creation of a service, we can specify in an array of
 * Method for CXF, the methods we want to exclude from the service.
 * However, from the user side, it is more convenient to specify the methods we want to expose
 * (in particular when only one method has to be exposed).
 * This class is therefore in charge of this conversion.
 * It is also in charge of converting arrays of String to arrays of Method and vice versa.
 *
 * @author The ProActive Team
 */
public class MethodUtils {

    private Method[] objectMethods;
    private ArrayList<Method> disallowedMethods;

    /**
     * Constructor
     *
     * @param objectClass
     */
    public MethodUtils(Class<?> objectClass) {
        this.objectMethods = objectClass.getMethods();
        this.disallowedMethods = getCorrespondingMethods(WSConstants.disallowedMethods
                .toArray(new String[WSConstants.disallowedMethods.size()]));
    }

    private boolean contains(Object[] objects, Object o) {
        Class<?> oClass = o.getClass();
        for (Object object : objects) {
            if ((oClass.cast(object)).equals(oClass.cast(o))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks at the class of method and raise and an exception if this class
     * is the stub class instead of the object (frequent mistake).
     *
     * TODO: Implements the conversion of a stub method into its
     * corresponding object method.
     *
     * @param method Method to be checked
     * @throws WebServicesException
     */
    public static void checkMethodClass(Method method) throws WebServicesException {
        if (method.getDeclaringClass().getName().startsWith(
                org.objectweb.proactive.core.mop.Utils.STUB_DEFAULT_PACKAGE))
            throw new WebServicesException("Method " + method.getName() +
                " is a method form the stub not from the class. \n" + "This method will not be exposed. \n" +
                "Use <class name>.class.getMethod(String methodName, Class<?>... parameters) to solve this issue");
    }

    /**
     * The same as checkMethodClass but for an array of Method.
     *
     * @param methods Methods to be checked
     * @throws WebServicesException
     */
    public static void checkMethodsClass(Method[] methods) throws WebServicesException {
        if (methods != null) {
            for (Method method : methods) {
                checkMethodClass(method);
            }
        }
    }

    /**
     * From an array of method, returns the corresponding ArrayList of serializable methods.
     *
     * @param methods Methods to be serialized
     * @return The corresponding array of serializable methods.
     */
    public static ArrayList<SerializableMethod> getSerializableMethods(Method[] methods) {
        if (methods == null || methods.length == 0)
            return null;
        ArrayList<SerializableMethod> serializableMethods = new ArrayList<SerializableMethod>();
        for (Method method : methods) {
            serializableMethods.add(new SerializableMethod(method));
        }
        return serializableMethods;
    }

    /**
     * From an array of serializable methods, returns the corresponding methods (of type Method)
     *
     * @param serializableMethods Array of serializable methods
     * @return The corresponding methods
     */
    public static Method[] getMethodsFromSerializableMethods(ArrayList<SerializableMethod> serializableMethods) {
        if (serializableMethods == null) {
            return null;
        }

        Method[] methods = new Method[serializableMethods.size()];
        for (int i = 0; i < serializableMethods.size(); i++) {
            methods[i] = serializableMethods.get(i).getMethod();
        }
        return methods;
    }

    /**
     * From an array of methods (of type Method), returns the corresponding of method names.
     *
     * @param methods Array of methods
     * @return The corresponding array of method names
     */
    public static ArrayList<String> getCorrespondingMethodsName(Method[] methods) {
        ArrayList<String> methodsName = new ArrayList<String>();
        for (Method method : methods) {
            if (!methodsName.contains(method.getName()))
                methodsName.add(method.getName());
        }
        return methodsName;
    }

    /**
     * From an array of methods name (of type String), returns the corresponding array of Method
     * If a method name corresponds to several methods (with different signature), all these methods
     * will be in the returned array.
     *
     * @param methodsName Array of method names
     * @return The corresponding array of methods
     */
    public ArrayList<Method> getCorrespondingMethods(String[] methodsName) {
        ArrayList<Method> methods = new ArrayList<Method>();
        ArrayList<Method> correspondingMethods;
        for (String name : methodsName) {
            correspondingMethods = getMethodsFromName(name);
            if (correspondingMethods != null)
                methods.addAll(correspondingMethods);
        }
        return methods;
    }

    /**
     * From a class and a method name, returns all the methods of this class whose
     * name is the same as the name given in parameter.
     * If several methods match (so with different signature), all these methods are
     * returned.
     */
    private ArrayList<Method> getMethodsFromName(String methodName) {
        ArrayList<Method> correspondingMethods = new ArrayList<Method>();

        for (Method method : this.objectMethods) {
            if (methodName.equals(method.getName()))
                correspondingMethods.add(method);
        }

        if (correspondingMethods.size() == 0)
            return null;

        return correspondingMethods;
    }

    /**
     * Returns the methods to be excluded in the shape of a String array. These methods are methods defined in the
     * WSConstants.disallowedMethods vector and methods which are not in deployedMethods.
     * In case of a null methodsName, only methods in disallowdMethods vector are
     * returned.
     *
     * @param deployedMethods Array of deployed method names
     * @return the ArrayList of method names (String) to be excluded
     */
    public ArrayList<String> getExcludedMethodsName(String[] deployedMethods) {
        ArrayList<String> excludedMethodsName = new ArrayList<String>();

        excludedMethodsName.addAll(WSConstants.disallowedMethods);

        if ((deployedMethods == null) || (deployedMethods.length == 0))
            return excludedMethodsName;

        for (Method m : this.objectMethods) {
            if (!contains(deployedMethods, m.getName())) {
                excludedMethodsName.add(m.getName());
            }
        }
        return excludedMethodsName;
    }

    /**
     * Returns the methods to be excluded in the shape of a Method array. These methods are methods defined in the
     * WSConstants.disallowedMethods vector and methods which are not in deployedMethods.
     * In case of a null methodsName, only methods in disallowdMethods vector are
     * returned.
     *
     * @param deployedMethods Array of Methods
     * @return the ArrayList of Methods to be excluded
     */
    public ArrayList<Method> getExcludedMethods(Method[] deployedMethods) {
        ArrayList<Method> excludedMethods = new ArrayList<Method>();

        excludedMethods.addAll(this.disallowedMethods);

        if ((deployedMethods == null) || (deployedMethods.length == 0))
            return excludedMethods;

        for (Method m : this.objectMethods) {
            if (!contains(deployedMethods, m)) {
                excludedMethods.add(m);
            }
        }
        return excludedMethods;
    }

}
