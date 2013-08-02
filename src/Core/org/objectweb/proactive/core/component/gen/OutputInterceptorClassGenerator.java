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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.ItfStubObject;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.PAInterfaceImpl;
import org.objectweb.proactive.core.component.control.PAInterceptorControllerImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.ClassDataCache;


/**
 * This class generates output interceptors for intercepting outgoing functional invocations.
 * We could also use a dynamic proxy, but the current way keeps homogeneity with other generators for ProActive components.
 *
 * @author The ProActive Team
 */
public class OutputInterceptorClassGenerator extends AbstractInterfaceClassGenerator {
    private static OutputInterceptorClassGenerator instance;

    private PAInterceptorControllerImpl interceptorController;

    private String clientInterfaceName;

    private OutputInterceptorClassGenerator() {
    }

    public static OutputInterceptorClassGenerator instance() {
        if (instance == null) {
            instance = new OutputInterceptorClassGenerator();
        }

        return instance;
    }

    public PAInterface generateInterface(PAInterface representative,
            PAInterceptorControllerImpl interceptorController, String clientInterfaceName)
            throws InterfaceGenerationFailedException {
        this.interceptorController = interceptorController;
        this.clientInterfaceName = clientInterfaceName;
        PAInterface generated = generateInterface(representative.getFcItfName(), representative
                .getFcItfOwner(), (PAGCMInterfaceType) representative.getFcItfType(), false, true);

        ((StubObject) generated).setProxy(((StubObject) representative).getProxy());

        return generated;
    }

    @Override
    public PAInterface generateInterface(final String interfaceName, Component owner,
            PAGCMInterfaceType interfaceType, boolean isInternal, boolean isFunctionalInterface)
            throws InterfaceGenerationFailedException {
        try {
            String representativeClassName = org.objectweb.proactive.core.component.gen.Utils
                    .getOutputInterceptorClassName(interfaceName, interfaceType.getFcItfSignature());
            Class<?> generated_class;

            // check whether class has already been generated
            try {
                generated_class = loadClass(representativeClassName);
            } catch (ClassNotFoundException cnfe) {
                CtMethod[] reifiedMethods;
                CtClass generatedCtClass = pool.makeClass(representativeClassName);

                //this.fcInterfaceName = fcInterfaceName;
                //isPrimitive = ((PAComponentRepresentativeImpl) owner).getHierarchicalType()
                //                                                    .equals(ComponentParameters.PRIMITIVE);
                List<CtClass> interfacesToImplement = new ArrayList<CtClass>();

                // add interface to reify
                CtClass functional_itf = pool.get(interfaceType.getFcItfSignature());
                generatedCtClass.addInterface(functional_itf);

                interfacesToImplement.add(functional_itf);

                // add Serializable interface
                interfacesToImplement.add(pool.get(Serializable.class.getName()));
                generatedCtClass.addInterface(pool.get(Serializable.class.getName()));

                // add StubObject, so we can set the proxy
                generatedCtClass.addInterface(pool.get(StubObject.class.getName()));

                // add ItfStubObject, so we can set the sender itf
                generatedCtClass.addInterface(pool.get(ItfStubObject.class.getName()));
                Utils.createItfStubObjectMethods(generatedCtClass);

                //interfacesToImplement.add(pool.get(StubObject.class.getName()));
                List<CtClass> interfacesToImplementAndSuperInterfaces = new ArrayList<CtClass>(
                    interfacesToImplement);
                addSuperInterfaces(interfacesToImplementAndSuperInterfaces);
                generatedCtClass.setSuperclass(pool.get(PAInterfaceImpl.class.getName()));
                JavassistByteCodeStubBuilder.createStubObjectMethods(generatedCtClass);
                CtField interfaceNameField = new CtField(ClassPool.getDefault().get(String.class.getName()),
                    "interfaceName", generatedCtClass);
                interfaceNameField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(interfaceNameField, "\"" + interfaceName + "\"");

                CtField methodsField = new CtField(pool.get("java.lang.reflect.Method[]"),
                    "overridenMethods", generatedCtClass);
                methodsField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(methodsField);

                // field for remembering generic parameters
                CtField genericTypesMappingField = new CtField(pool.get("java.util.Map"),
                    "genericTypesMapping", generatedCtClass);

                genericTypesMappingField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(genericTypesMappingField);

                // add interceptorControllerField and clientInterfaceNameField
                generatedCtClass.addInterface(pool.get(OutputInterceptorHelper.class.getName()));
                CtField interceptorControllerField = new CtField(pool.get(PAInterceptorControllerImpl.class
                        .getName()), "interceptorController", generatedCtClass);
                generatedCtClass.addField(interceptorControllerField, "null;");
                CtMethod interceptorControllerSetter = CtNewMethod.setter("setInterceptorController",
                        interceptorControllerField);
                generatedCtClass.addMethod(interceptorControllerSetter);
                CtField clientInterfaceNameField = new CtField(pool.get(String.class.getName()),
                    "clientInterfaceName", generatedCtClass);
                generatedCtClass.addField(clientInterfaceNameField, "null;");
                CtMethod clientInterfaceNameSetter = CtNewMethod.setter("setClientInterfaceName",
                        clientInterfaceNameField);
                generatedCtClass.addMethod(clientInterfaceNameSetter);

                // list all methods to implement
                Map<String, CtMethod> methodsToImplement = new HashMap<String, CtMethod>();
                List<String> classesIndexer = new Vector<String>();

                CtClass[] params;
                CtClass itf;

                // now get the methods from implemented interfaces
                Iterator<CtClass> it = interfacesToImplementAndSuperInterfaces.iterator();
                while (it.hasNext()) {
                    itf = it.next();
                    if (!classesIndexer.contains(itf.getName())) {
                        classesIndexer.add(itf.getName());
                    }

                    CtMethod[] declaredMethods = itf.getDeclaredMethods();

                    for (int i = 0; i < declaredMethods.length; i++) {
                        CtMethod currentMethod = declaredMethods[i];

                        // Build a key with the simple name of the method
                        // and the names of its parameters in the right order
                        String key = "";
                        key = key + currentMethod.getName();
                        params = currentMethod.getParameterTypes();
                        for (int k = 0; k < params.length; k++) {
                            key = key + params[k].getName();
                        }

                        // this gives the actual declaring Class<?> of this method
                        methodsToImplement.put(key, currentMethod);
                    }
                }

                reifiedMethods = (methodsToImplement.values()
                        .toArray(new CtMethod[methodsToImplement.size()]));

                // Determines which reifiedMethods are valid for reification
                // It is the responsibility of method checkMethod in class Utils
                // to decide if a method is valid for reification or not
                Vector<CtMethod> v = new Vector<CtMethod>();
                int initialNumberOfMethods = reifiedMethods.length;

                for (int i = 0; i < initialNumberOfMethods; i++) {
                    if (JavassistByteCodeStubBuilder.checkMethod(reifiedMethods[i])) {
                        v.addElement(reifiedMethods[i]);
                    }
                }
                CtMethod[] validMethods = new CtMethod[v.size()];
                v.copyInto(validMethods);

                reifiedMethods = validMethods;

                JavassistByteCodeStubBuilder.createStaticInitializer(generatedCtClass, reifiedMethods,
                        classesIndexer, interfaceType.getFcItfSignature(), null);

                createReifiedMethods(generatedCtClass, reifiedMethods, isFunctionalInterface);
                //                generatedCtClass.stopPruning(true);
                //                generatedCtClass.writeFile("generated/");
                //                System.out.println("[JAVASSIST] generated class : " +
                //                    representativeClassName);
                byte[] bytecode = generatedCtClass.toBytecode();
                ClassDataCache.instance().addClassData(representativeClassName, bytecode);
                if (logger.isDebugEnabled()) {
                    logger.debug("added " + representativeClassName + " to cache");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("generated classes cache is : " + ClassDataCache.instance().toString());
                }

                // convert the bytes into a Class<?>
                generated_class = Utils.defineClass(representativeClassName, bytecode);
            }

            PAInterfaceImpl reference = (PAInterfaceImpl) generated_class.newInstance();
            reference.setFcItfName(interfaceName);
            reference.setFcItfOwner(owner);
            reference.setFcType(interfaceType);
            reference.setFcIsInternal(isInternal);

            ((OutputInterceptorHelper) reference).setInterceptorController(this.interceptorController);
            ((OutputInterceptorHelper) reference).setClientInterfaceName(this.clientInterfaceName);

            return reference;
        } catch (Exception e) {
            throw new InterfaceGenerationFailedException(
                "Cannot generate output interceptor on interface [" + interfaceName + "] with signature [" +
                    interfaceType.getFcItfSignature() + "] with javassist", e);
        }
    }

    protected static void createReifiedMethods(CtClass generatedClass, CtMethod[] reifiedMethods,
            boolean isFunctionalInterface) throws NotFoundException, CannotCompileException {
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();
            String body = ("{\nObject[] parameters = new Object[" + paramTypes.length + "];\n");
            for (int j = 0; j < paramTypes.length; j++) {
                if (paramTypes[j].isPrimitive()) {
                    body += ("  parameters[" + j + "]=" +
                        JavassistByteCodeStubBuilder.wrapPrimitiveParameter(paramTypes[j], "$" + (j + 1)) + ";\n");
                } else {
                    body += ("  parameters[" + j + "]=$" + (j + 1) + ";\n");
                }
            }

            body += ("org.objectweb.proactive.core.mop.MethodCall methodCall = org.objectweb.proactive.core.mop.MethodCall.getComponentMethodCall(" +
                "(java.lang.reflect.Method)overridenMethods[" + i + "]" + ", parameters, null, interfaceName, senderItfID);\n");

            // delegate to outputinterceptors
            body += "java.util.List outputInterceptors = this.interceptorController.getInterceptors(this.clientInterfaceName);\n";
            body += "java.util.ListIterator it = outputInterceptors.listIterator();\n";
            body += "while (it.hasNext()) {\n";
            body += "  ((org.objectweb.proactive.core.component.interception.Interceptor) it.next()).beforeMethodInvocation(this.clientInterfaceName, methodCall);\n";
            body += "}\n";

            CtClass returnType = reifiedMethods[i].getReturnType();
            if (returnType != CtClass.voidType) {
                body += "Object result = ";
            }
            body += ("myProxy.reify(methodCall);\n");

            //  delegate to outputinterceptors
            body += "it = outputInterceptors.listIterator();\n";
            // use output interceptors in reverse order after invocation
            // go to the end of the list first
            body += "while (it.hasNext()) {\n";
            body += "it.next();\n";
            body += "}\n";
            body += "while (it.hasPrevious()) {\n";
            body += "  ((org.objectweb.proactive.core.component.interception.Interceptor) it.previous()).afterMethodInvocation(this.clientInterfaceName, methodCall, ";
            if (returnType != CtClass.voidType) {
                body += "result);\n";
            } else {
                body += "null);\n";
            }
            body += "}\n";

            // return casted result
            String postWrap = null;
            String preWrap = null;

            if (returnType != CtClass.voidType) {
                if (!returnType.isPrimitive()) {
                    preWrap = "(" + returnType.getName() + ")";
                } else {
                    //boolean, byte, char, short, int, long, float, double
                    if (returnType.equals(CtClass.booleanType)) {
                        preWrap = "((Boolean)";
                        postWrap = ").booleanValue()";
                    }
                    if (returnType.equals(CtClass.byteType)) {
                        preWrap = "((Byte)";
                        postWrap = ").byteValue()";
                    }
                    if (returnType.equals(CtClass.charType)) {
                        preWrap = "((Character)";
                        postWrap = ").charValue()";
                    }
                    if (returnType.equals(CtClass.shortType)) {
                        preWrap = "((Short)";
                        postWrap = ").shortValue()";
                    }
                    if (returnType.equals(CtClass.intType)) {
                        preWrap = "((Integer)";
                        postWrap = ").intValue()";
                    }
                    if (returnType.equals(CtClass.longType)) {
                        preWrap = "((Long)";
                        postWrap = ").longValue()";
                    }
                    if (returnType.equals(CtClass.floatType)) {
                        preWrap = "((Float)";
                        postWrap = ").floatValue()";
                    }
                    if (returnType.equals(CtClass.doubleType)) {
                        preWrap = "((Double)";
                        postWrap = ").doubleValue()";
                    }
                }
                body += "return ";
                if (preWrap != null) {
                    body += preWrap;
                }
                body += "result ";
            }

            if (postWrap != null) {
                body += postWrap;
            }
            body += ";";
            body += "\n}";

            CtMethod methodToGenerate = CtNewMethod.make(reifiedMethods[i].getReturnType(), reifiedMethods[i]
                    .getName(), reifiedMethods[i].getParameterTypes(), reifiedMethods[i].getExceptionTypes(),
                    body, generatedClass);
            generatedClass.addMethod(methodToGenerate);
        }
    }
}
