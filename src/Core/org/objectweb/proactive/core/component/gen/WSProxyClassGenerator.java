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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.ProActiveInterfaceImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.WSComponent;
import org.objectweb.proactive.core.component.webservices.ProActiveWSCaller;
import org.objectweb.proactive.core.component.webservices.WSInfo;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.util.ClassDataCache;


/**
 * This class generates a proxy for binding a client interface of a component to a web service.
 * <br>
 * The generated class implements the Java interface corresponding to the client interface and
 * extends the {@link ProActiveInterfaceImpl} class.
 * <br>
 * Its {@link InterfaceType} is the server side {@link InterfaceType} corresponding to the
 * {@link InterfaceType} of the client interface.
 * <br>
 * Its owner is a {@link WSComponent} component containing the URL and the name of the class
 * to use to call the web service.
 * <br>
 * The functional interface implementation returned by calling {@link ProActiveInterface#getFcItfImpl()}
 * is the URL of the web service.
 *
 * @author The ProActive Team
 * @see ProActiveInterfaceImpl
 * @see InterfaceType
 * @see WSComponent
 */
public class WSProxyClassGenerator extends AbstractInterfaceClassGenerator {
    private static WSProxyClassGenerator instance;

    public static WSProxyClassGenerator instance() {
        if (instance == null) {
            return new WSProxyClassGenerator();
        } else {
            return instance;
        }
    }

    /**
     * Generate a proxy for binding a client interface of a component to a web service.
     *
     * @param interfaceName Name of the component interface of the generated class.
     * @param owner Component owner of the generated class. Should always be an instance of {@link WSComponent}.
     * @param interfaceType {@link InterfaceType} of the generated class.
     * @param isInternal Specify if the generated class is a component internal interface. Should always be false.
     * @param isFunctionalInterface Specify if the generated class is a component functional interface. Should
     * always be true.
     * @return The generated class.
     * @see InterfaceType
     * @see WSComponent
     */
    public ProActiveInterface generateInterface(String interfaceName, Component owner,
            ProActiveInterfaceType interfaceType, boolean isInternal, boolean isFunctionalInterface)
            throws InterfaceGenerationFailedException {
        try {
            WSInfo wsInfo = ((WSComponent) owner).getWSInfo();
            String wsProxyClassName = Utils.getWSProxyClassName(interfaceName, interfaceType
                    .getFcItfSignature(), wsInfo.getWSCallerClassName());
            Class<?> generatedClass;

            // Check whether class has already been generated
            try {
                generatedClass = loadClass(wsProxyClassName);
            } catch (ClassNotFoundException cnfe) {
                // Create class
                CtClass generatedCtClass = pool.makeClass(wsProxyClassName);

                // Set super class
                generatedCtClass.setSuperclass(pool.get(ProActiveInterfaceImpl.class.getName()));

                // Set interfaces to implement
                List<CtClass> itfs = new ArrayList<CtClass>();
                itfs.add(pool.get(interfaceType.getFcItfSignature()));
                itfs.add(pool.get(Serializable.class.getName()));
                generatedCtClass.setInterfaces(itfs.toArray(new CtClass[0]));
                addSuperInterfaces(itfs);

                // Add fields
                CtField loggerField = new CtField(pool.get(Logger.class.getName()), "logger",
                    generatedCtClass);
                loggerField.setModifiers(Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL +
                    Modifier.TRANSIENT);
                generatedCtClass.addField(loggerField,
                        "org.objectweb.proactive.core.util.log.ProActiveLogger.getLogger("
                            + "org.objectweb.proactive.core.util.log.Loggers.COMPONENTS_REQUESTS)");
                CtField wsCallerField = new CtField(pool.get(ProActiveWSCaller.class.getName()), "wsCaller",
                    generatedCtClass);
                wsCallerField.setModifiers(Modifier.PRIVATE);
                generatedCtClass.addField(wsCallerField, "new " + wsInfo.getWSCallerClassName() + "()");
                CtField proxyField = new CtField(pool.get(Proxy.class.getName()), "proxy", generatedCtClass);
                proxyField.setModifiers(Modifier.PRIVATE);
                generatedCtClass.addField(proxyField);

                // Add constructor
                CtConstructor constructorNoParam = CtNewConstructor.defaultConstructor(generatedCtClass);
                generatedCtClass.addConstructor(constructorNoParam);

                // Add getter and setter for private fields
                String bodyGetterFcItfImpl = "{\nreturn ((org.objectweb.proactive.core.component.type.WSComponent) getFcItfOwner()).getWSInfo().getWSUrl();\n}";
                CtMethod getterFcItfImpl = CtNewMethod.make(pool.get("java.lang.Object"), "getFcItfImpl",
                        null, null, bodyGetterFcItfImpl, generatedCtClass);
                generatedCtClass.addMethod(getterFcItfImpl);
                String bodySetterFcItfImpl = "{\ntry {\n"
                    + "((org.objectweb.proactive.core.component.type.WSComponent) getFcItfOwner()).getWSInfo().setWSUrl((String) $1);\n"
                    + "wsCaller.setup(Class.forName(((org.objectweb.fractal.api.type.InterfaceType) getFcItfType()).getFcItfSignature()), (String) $1);\n"
                    + "}\n"
                    + "catch(org.objectweb.fractal.api.control.IllegalBindingException ibe) {\n"
                    + "logger.error(\"Can not set binding to the web service address: \" + $1, ibe);\n"
                    + "}\n"
                    + "catch(ClassNotFoundException cnfe) {\n"
                    + "logger.error(\"Can not find class: \" + ((org.objectweb.fractal.api.type.InterfaceType) getFcItfType()).getFcItfSignature(), cnfe);\n"
                    + "}\n" + "}";
                CtMethod setterFcItfImpl = CtNewMethod.make(CtClass.voidType, "setFcItfImpl",
                        new CtClass[] { pool.get("java.lang.Object") }, null, bodySetterFcItfImpl,
                        generatedCtClass);
                generatedCtClass.addMethod(setterFcItfImpl);
                CtMethod getterProxy = CtNewMethod.getter("getProxy", proxyField);
                generatedCtClass.addMethod(getterProxy);
                CtMethod setterProxy = CtNewMethod.setter("setProxy", proxyField);
                generatedCtClass.addMethod(setterProxy);

                //  Add methods from implemented interfaces
                for (CtClass itf : itfs) {
                    CtMethod[] methods = itf.getDeclaredMethods();
                    for (CtMethod method : methods) {
                        String methodName = method.getName();
                        CtClass[] parametersType = method.getParameterTypes();
                        CtClass returnType = method.getReturnType();
                        String body = "{\n";
                        if (returnType != CtClass.voidType) {
                            body += "Object result = ";
                        }
                        body += "wsCaller.callWS(\"" + methodName + "\", $args";
                        if (returnType == CtClass.voidType) {
                            body += ", null);\n";
                        } else {
                            body += ", $type);\n";
                            body += "if (result != null) {\nreturn ($r) result;\n}\nelse {\nreturn " +
                                (returnType.isPrimitive() ? "0" : "null") + ";\n}\n";
                        }
                        body += "}";
                        CtMethod methodToGenerate = CtNewMethod.make(returnType, methodName, parametersType,
                                method.getExceptionTypes(), body, generatedCtClass);
                        generatedCtClass.addMethod(methodToGenerate);
                    }
                }

                //                generatedCtClass.stopPruning(true);
                //                generatedCtClass.writeFile("generated/");
                //                System.out.println("[JAVASSIST] generated class: " + wsProxyClassName);

                // Generate and add to cache the generated class
                byte[] bytecode = generatedCtClass.toBytecode();
                ClassDataCache.instance().addClassData(wsProxyClassName, bytecode);
                if (logger.isDebugEnabled()) {
                    logger.debug("added " + wsProxyClassName + " to cache");
                    logger.debug("generated classes cache is: " + ClassDataCache.instance().toString());
                }
                generatedClass = Utils.defineClass(wsProxyClassName, bytecode);
            }

            // Instantiate class
            ProActiveInterfaceImpl reference = (ProActiveInterfaceImpl) generatedClass.newInstance();
            reference.setFcItfName(interfaceName);
            reference.setFcItfOwner(owner);
            reference.setFcType(interfaceType);
            reference.setFcIsInternal(isInternal);
            reference.setFcItfImpl(wsInfo.getWSUrl());
            reference.setProxy(null);

            // Set up owner
            ((WSComponent) owner).setFcInterfaceImpl(reference);

            return reference;
        } catch (Exception e) {
            logger.error("Cannot generate web service proxy [" + interfaceName + "] with signature [" +
                interfaceType.getFcItfSignature() + "] with javassist: " + e.getMessage());
            throw new InterfaceGenerationFailedException(
                "Cannot generate web service proxy [" + interfaceName + "] with signature [" +
                    interfaceType.getFcItfSignature() + "] with javassist", e);
        }
    }
}
