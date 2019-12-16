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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.*;
import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.*;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class generates the bytecode for proactive stubs using Javassist.
 *
 * @author The ProActive Team
 */
public class JavassistByteCodeStubBuilder {

    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.STUB_GENERATION);

    private static boolean classPoolInitialized = false;

    private static ClassPool pool = ClassPool.getDefault();

    public synchronized static ClassPool getClassPool() {
        if (!classPoolInitialized) {
            pool.appendClassPath(new LoaderClassPath(ProActiveRuntime.class.getClassLoader()));
            pool.appendClassPath(new LoaderClassPath(MOPClassLoader.getMOPClassLoader()));
            classPoolInitialized = true;
        }
        return pool;
    }

    /**
     * <p>Creates the bytecode for a stub on the given class</p>
     * <p>This method should be accessed by one thread only for a given class name, otherwise
     * it may lead to unsupported concurrent class generation, resulting in a "frozen class" javassist runtime exception </p>
     *
     * @param className         the name of the class on which a stub class is created
     * @param genericParameters TODO
     * @return the bytecode for the corresponding stub class
     * @throws NoClassDefFoundError if the specified classname does not correspond to a class in the classpath
     */
    @SuppressWarnings("unchecked")
    public static byte[] create(String className, Class<?>[] genericParameters) throws NoClassDefFoundError {
        synchronized (MOPClassLoader.getMOPClassLoader()) {
            CtClass generatedCtClass = null;

            if (genericParameters == null) {
                genericParameters = new Class<?>[0];
            }
            Method[] reifiedMethodsWithoutGenerics;
            try {
                ClassPool pool = getClassPool();
                generatedCtClass = pool.makeClass(Utils.convertClassNameToStubClassName(className, genericParameters));
                generatedCtClass.getClassFile().setMajorVersion(ClassFile.JAVA_6);

                CtClass superCtClass = null;
                try {
                    superCtClass = pool.get(className);
                } catch (NotFoundException e) {
                    // may happen in environments with multiple classloaders: className is not available
                    // in the initial classpath of javassist's class pool
                    // ==> try to append classpath of the class corresponding to className
                    pool.appendClassPath(new LoaderClassPath(Class.forName(className).getClassLoader()));
                    superCtClass = pool.get(className);
                }

                // Fix for PROACTIVE-1163: serialVersionUIDs, when defined, should be reported on generated Stubs
                // The stub class must have the same serialVersionUID as the reified class
                // Using CtClass#getDeclaredFields() instead of CtClass#getDeclaredField() to avoid NotFoundException
                for (CtField declaredField : superCtClass.getDeclaredFields()) {
                    if ("serialVersionUID".equals(declaredField.getName())) {
                        generatedCtClass.addField(new CtField(declaredField, generatedCtClass));
                    }
                }

                CtField outsideOfConstructorField = new CtField(pool.get(CtClass.booleanType.getName()),
                                                                "outsideOfConstructor",
                                                                generatedCtClass);

                generatedCtClass.addField(outsideOfConstructorField, (superCtClass.isInterface() ? " false" : "true"));

                if (superCtClass.isInterface()) {
                    generatedCtClass.addInterface(superCtClass);
                    generatedCtClass.setSuperclass(pool.get(Object.class.getName()));
                } else {
                    generatedCtClass.setSuperclass(superCtClass);
                }

                if (!generatedCtClass.subtypeOf(pool.get(Serializable.class.getName()))) {
                    generatedCtClass.addInterface(pool.get(Serializable.class.getName()));
                }

                CtClass ctStubO = null;
                try {
                    ctStubO = pool.get(StubObject.class.getName());
                } catch (NotFoundException e) {
                    // may happen in environments with multiple classloaders: StubObject is not available
                    // in the initial classpath of javassist's class pool
                    // ==> try to append classpath of the class corresponding to StubObject
                    pool.appendClassPath(new ClassClassPath(Class.forName(StubObject.class.getName())));
                    ctStubO = pool.get(StubObject.class.getName());
                }

                if (!generatedCtClass.subtypeOf(ctStubO)) {
                    generatedCtClass.addInterface(ctStubO);
                }

                CtMethod[] proxyMethods = createStubObjectMethods(generatedCtClass);

                CtField methodsField = new CtField(pool.get("java.lang.reflect.Method[]"),
                                                   "overridenMethods",
                                                   generatedCtClass);

                methodsField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(methodsField);

                CtField genericTypesMappingField = new CtField(pool.get("java.util.Map"),
                                                               "genericTypesMapping",
                                                               generatedCtClass);

                genericTypesMappingField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(genericTypesMappingField);

                //   This map is used for keeping track of the method signatures / methods that are to be reified
                java.util.Map<String, Method> temp = new HashMap<String, Method>();
                List<String> classesIndexer = new Vector<String>();

                temp = methodsIndexer(superCtClass, classesIndexer);

                reifiedMethodsWithoutGenerics = (temp.values().toArray(new Method[temp.size()]));

                // Determines which reifiedMethods are valid for reification
                // It is the responsibility of method checkMethod
                // to decide if a method is valid for reification or not
                Vector<Method> v = new Vector<Method>();
                int initialNumberOfMethods = reifiedMethodsWithoutGenerics.length;

                for (int i = 0; i < initialNumberOfMethods; i++) {
                    if (checkMethod(reifiedMethodsWithoutGenerics[i].getCtMethod(), proxyMethods)) {
                        v.addElement(reifiedMethodsWithoutGenerics[i]);
                    }
                }
                Method[] validMethods = new Method[v.size()];
                CtMethod[] validCtMethods = new CtMethod[v.size()];
                //            v.copyInto(validMethods);

                // Installs the list of valid reifiedMethods as an instance variable of this object
                for (int i = 0; i < validMethods.length; i++) {
                    validMethods[i] = v.get(i);
                    validCtMethods[i] = v.get(i).getCtMethod();
                }

                Class realSuperClass = Class.forName(className);
                TypeVariable<GenericDeclaration>[] tv = realSuperClass.getTypeParameters();
                Map<TypeVariable, Class<?>> genericTypesMapping = new HashMap<TypeVariable, Class<?>>();
                if (genericParameters.length != 0) {
                    // only deal with cases where parameters have been specified
                    for (int i = 0; i < tv.length; i++) {
                        genericTypesMapping.put(tv[i], genericParameters[i]);
                    }
                }

                // create static block with method initializations
                createStaticInitializer(generatedCtClass, validCtMethods, classesIndexer, className, genericParameters);

                createReifiedMethods(generatedCtClass, validMethods, superCtClass.isInterface());

                if (logger.isTraceEnabled()) {
                    logger.debug("generated class : " + generatedCtClass.getName() + "loaded into " +
                                 generatedCtClass.getClass().getClassLoader().toString() +
                                 ", initial class' classloader " + superCtClass.getClass().getClassLoader() + ", url " +
                                 superCtClass.getURL());
                } else if (logger.isDebugEnabled()) {
                    logger.debug("generated class : " + generatedCtClass.getName());
                }

                // detach to fix "frozen class" errors encountered in some large scale deployments
                byte[] bytecode = generatedCtClass.toBytecode();

                if (CentralPAPropertyRepository.PA_MOP_WRITESTUBONDISK.isTrue()) {
                    generatedCtClass.debugWriteFile(CentralPAPropertyRepository.PA_MOP_GENERATEDCLASSES_DIR.getValue());
                }

                generatedCtClass.detach();

                return bytecode;
            } catch (Exception e) {
                // generatedCtClass.debugWriteFile();
                throw new RuntimeException("Failed to generate stub for class " + className + " with javassist : " +
                                           e.getMessage(), e);
            }
        }
    }

    static Map<String, Method> methodsIndexer(CtClass superCtClass, List<String> classesIndexer)
            throws NotFoundException {
        // Recursively calls getDeclaredMethods () on the target type
        // and each of its superclasses, all the way up to java.lang.Object
        // We have to be careful and only take into account overriden methods once
        CtClass currentCtClass = superCtClass;

        java.util.Map<String, Method> temp = new HashMap<String, Method>();

        CtClass[] params;
        Object exists;

        classesIndexer.add(superCtClass.getName());

        // If the target type is an interface, the only thing we have to do is to
        // get the list of all its public reifiedMethods.
        if (superCtClass.isInterface()) {
            CtMethod[] allPublicMethods = superCtClass.getMethods();
            for (int i = 0; i < allPublicMethods.length; i++) {
                String key = generateMethodKey(allPublicMethods[i]);
                temp.put(key, new Method(allPublicMethods[i]));
            }
            classesIndexer.add("java.lang.Object");
        } else // If the target type is an actual class, we climb up the tree
        {
            do {
                if (!classesIndexer.contains(currentCtClass.getName())) {
                    classesIndexer.add(currentCtClass.getName());
                }

                // The declared reifiedMethods for the current class
                CtMethod[] declaredCtMethods = currentCtClass.getDeclaredMethods();

                // For each method declared in this class
                for (int i = 0; i < declaredCtMethods.length; i++) {
                    CtMethod currentMethod = declaredCtMethods[i];

                    // Build a key with the simple name of the method
                    // and the names of its parameters in the right order
                    String key = generateMethodKey(currentMethod);
                    // Tests if we already have met this method in a subclass
                    exists = temp.get(key);
                    params = currentMethod.getParameterTypes();
                    if (exists == null) {
                        // The only method we ABSOLUTELY want to be called directly
                        // on the stub (and thus not reified) is
                        // the protected void finalize () throws Throwable
                        if ((key.equals("finalize")) && (params.length == 0)) {
                            // Do nothing, simply avoid adding this method to the list
                        } else {
                            // If not, adds this method to the Vector that
                            // holds all the reifiedMethods for this class
                            // tempVector.addElement(currentMethod);
                            temp.put(key, new Method(currentMethod));
                        }
                    } else {
                        // We already know this method because it is overriden
                        // in a subclass. Then we just check for annotations
                        Method met = temp.get(key);
                        met.grabMethodandParameterAnnotation(currentMethod);
                    }
                }
                currentCtClass = currentCtClass.getSuperclass();
            } while (currentCtClass != null); // Continue until we ask for the superclass of java.lang.Object
        }

        // now get the methods from implemented interfaces
        List<CtClass> superInterfaces = new Vector<CtClass>();
        addSuperInterfaces(superCtClass, superInterfaces);

        CtClass[] implementedInterfacesTable = (superInterfaces.toArray(new CtClass[superInterfaces.size()]));

        for (int itfsIndex = 0; itfsIndex < implementedInterfacesTable.length; itfsIndex++) {
            if (!classesIndexer.contains(implementedInterfacesTable[itfsIndex].getName())) {
                classesIndexer.add(implementedInterfacesTable[itfsIndex].getName());
            }

            // The declared methods for the current interface
            CtMethod[] declaredMethods = implementedInterfacesTable[itfsIndex].getDeclaredMethods();

            // For each method declared in this class
            for (int i = 0; i < declaredMethods.length; i++) {
                CtMethod currentMethod = declaredMethods[i];

                // Build a key with the simple name of the method
                // and the names of its parameters in the right order
                String key = generateMethodKey(currentMethod);

                // replace with current one, because this gives the actual declaring Class<?> of this method
                Method m = temp.get(key);
                if (m == null) {
                    m = new Method(currentMethod);
                    temp.put(key.toString(), m);
                }
                // m.setCtMethod(currentMethod);
                m.grabMethodandParameterAnnotation(currentMethod);

            }
        }

        return temp;
    }

    private static String generateMethodKey(CtMethod method) throws NotFoundException {
        CtClass[] params;
        StringBuilder key = new StringBuilder();
        key.append(method.getName());
        params = method.getParameterTypes();
        for (int k = 0; k < params.length; k++) {
            key.append(params[k].getName());
        }
        return key.toString();
    }

    /**
     * @param generatedClass
     * @param reifiedMethods
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private static void createReifiedMethods(CtClass generatedClass, Method[] reifiedMethods, boolean stubOnInterface)
            throws NotFoundException, CannotCompileException {
        for (int i = 0; i < reifiedMethods.length; i++) {
            Method reifiedMethod = reifiedMethods[i];
            StringBuilder body = new StringBuilder("{");

            if (hasAnnotation(reifiedMethod.getCtMethod(), Self.class)) {
                body.append("return this;");
            } else {

                handleUnwrapFutureAnnotation(reifiedMethod, body);

                handleTurnRemoteAnnotation(reifiedMethod, body);

                handleTurnActiveAnnotation(reifiedMethod, body);

                if (hasAnnotation(reifiedMethod.getCtMethod(), TurnRemoteParam.class)) {
                    TurnRemoteParam trp = getAnnotation(reifiedMethod.getCtMethod(), TurnRemoteParam.class);
                    int parameterIndex = parameterNameToIndex(reifiedMethod.getCtMethod(), trp.parameterName());
                    body.append("$" + parameterIndex + " = org.objectweb.proactive.api.PARemoteObject.turnRemote($" +
                                parameterIndex + "); \n");
                }

                if (hasAnnotation(reifiedMethod.getCtMethod(), TurnActiveParam.class)) {
                    TurnActiveParam trp = getAnnotation(reifiedMethod.getCtMethod(), TurnActiveParam.class);
                    int parameterIndex = parameterNameToIndex(reifiedMethod.getCtMethod(), trp.parameterName());
                    body.append("$" + parameterIndex + " = org.objectweb.proactive.api.PAActiveObject.turnActive($" +
                                parameterIndex + "); \n");
                }

                boolean fieldToCache = hasAnnotation(reifiedMethod.getCtMethod(), Cache.class);
                CtField cachedField = null;

                if (fieldToCache) {
                    // the generated has to cache the method

                    cachedField = new CtField(getClassPool().get(reifiedMethod.getCtMethod().getReturnType().getName()),
                                              reifiedMethod.getCtMethod().getName() + i,
                                              generatedClass);

                    generatedClass.addField(cachedField);

                    body.append("if (" + cachedField.getName() + " == null) { ");
                }

                //                body.append("\nObject[] parameters = $args;\n");

                CtClass returnType = reifiedMethod.getCtMethod().getReturnType();

                String postWrap = null;
                String preWrap = null;

                if (hasAnnotation(reifiedMethod.getCtMethod(), NoReify.class)) {
                    body.append("if (myProxy instanceof org.objectweb.proactive.core.remoteobject.SynchronousProxy) { return ($r) ((org.objectweb.proactive.core.remoteobject.SynchronousProxy) myProxy).receiveMessage($$); }  \n");
                }

                if (returnType != CtClass.voidType) {
                    if (!returnType.isPrimitive()) {
                        preWrap = "($r)";
                    } else {
                        preWrap = "($w)";
                    }
                }

                if (fieldToCache) {
                    body.append(cachedField.getName() + "=");
                } else {
                    body.append("return ($r)");
                }

                if (preWrap != null) {
                    body.append(preWrap);
                }

                body.append("myProxy.reify(org.objectweb.proactive.core.mop.MethodCall.getMethodCall(" +
                            "(java.lang.reflect.Method)overridenMethods[" + i + "]" + ", $args, genericTypesMapping))");

                if (fieldToCache) {
                    body.append(";\n } \n return ($r)" + cachedField.getName());
                }

                body.append(";");

                // the following is for inserting conditional statement for method code executing
                // within or outside the construction of the object
                if (!stubOnInterface && !Modifier.isAbstract(reifiedMethod.getCtMethod().getModifiers())) {
                    String preReificationCode = "{if (outsideOfConstructor) ";

                    // outside of constructor : object is already constructed
                    String postReificationCode = "\n} else {";

                    // if inside constructor (i.e. in a method called by a
                    // constructor from a super class)
                    if (!reifiedMethod.getCtMethod().getReturnType().equals(CtClass.voidType)) {
                        postReificationCode += "return ";
                    }
                    postReificationCode += ("super." + reifiedMethod.getCtMethod().getName() + "($$);");
                    //                    postReificationCode += ("super.$proceed($$);");
                    postReificationCode += "}";
                    body.insert(0, preReificationCode);
                    body.append(postReificationCode);
                }
            }

            body.append("\n}");

            CtMethod methodToGenerate = null;

            try {
                methodToGenerate = CtNewMethod.make(reifiedMethod.getCtMethod().getReturnType(),
                                                    reifiedMethod.getCtMethod().getName(),
                                                    reifiedMethod.getCtMethod().getParameterTypes(),
                                                    reifiedMethod.getCtMethod().getExceptionTypes(),
                                                    body.toString(),
                                                    generatedClass);
            } catch (RuntimeException e) {
                logger.error("", e);
            }

            generatedClass.addMethod(methodToGenerate);

            //            if (fieldToCache) {
            //              CtMethod proxySetterMethod = generatedClass.getMethod(proxySetter.getName(), proxySetter.getSignature());
            //              String statementsToAdd = "if (myProxy != null ) { \n" +  cachedField.getName() + " = null ; \n " + reifiedMethods[i].getName() + "(); } \n ";
            //              System.out
            //                      .println("JavassistByteCodeStubBuilder.createReifiedMethods() statementsToAdd " + statementsToAdd);
            //              proxySetterMethod.insertAfter(statementsToAdd);
            //            }
        }
    }

    /**
     * @param reifiedMethod a method that has some parameters
     * @param parameterName the name of the parameters
     * @return the index of the parameters in the list of parameters (first parameter has index 1)
     */
    private static int parameterNameToIndex(CtMethod reifiedMethod, String parameterName) {
        CodeAttribute codeAttribute = (CodeAttribute) reifiedMethod.getMethodInfo().getAttribute(CodeAttribute.tag);
        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        for (int j = 0; j < localVariableAttribute.tableLength(); j++) {
            String name = localVariableAttribute.getConstPool().getUtf8Info(localVariableAttribute.nameIndex(j));
            if (!name.equals("this")) {
                if (name.equals(parameterName)) {
                    return j;
                }
            }
        }
        throw new RuntimeException("parameter " + parameterName + "not found in method " + reifiedMethod.getLongName());
    }

    /**
     * @param generatedClass
     * @param reifiedMethods
     * @param classesIndexer
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public static void createStaticInitializer(CtClass generatedClass, CtMethod[] reifiedMethods,
            List<String> classesIndexer, String superClassName, Class<?>[] genericParameters)
            throws CannotCompileException, NotFoundException {
        if (genericParameters == null) {
            genericParameters = new Class<?>[0];
        }
        CtConstructor classInitializer = generatedClass.makeClassInitializer();

        StringBuilder classInitializerBody = new StringBuilder("{\n");
        classInitializerBody.append("Class[] genericParameters = new Class[" + genericParameters.length + "];\n");
        for (int i = 0; i < genericParameters.length; i++) {
            classInitializerBody.append("genericParameters[" + i + "] = Class.forName(\"" +
                                        genericParameters[i].getName() + "\");\n");
        }
        classInitializerBody.append("Class realSuperClass = Class.forName(\"" + superClassName + "\");\n");
        classInitializerBody.append("java.lang.reflect.TypeVariable[] tv = realSuperClass.getTypeParameters();\n");
        classInitializerBody.append("genericTypesMapping = new java.util.HashMap();\n");

        // generic types mapping only occurs when parameters are specified
        if (genericParameters.length != 0) {
            classInitializerBody.append("for (int i = 0; i < tv.length; i++) {\n");
            classInitializerBody.append("     genericTypesMapping.put(tv[i], genericParameters[i]);\n");
            classInitializerBody.append("}\n");
        }

        classInitializerBody.append("overridenMethods = new java.lang.reflect.Method[" + reifiedMethods.length +
                                    "];\n");
        classInitializerBody.append("Class classes[] = new Class[" + (classesIndexer.size()) + "];\n");
        classInitializerBody.append("Class[] temp;\n");

        int methodsIndex = 0;
        Iterator<String> it = classesIndexer.iterator();
        int index = 0;
        while (it.hasNext()) {
            classInitializerBody.append("classes[" + index + "] = Class.forName(\"" + it.next() + "\");\n");
            index++;
        }
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();
            classInitializerBody.append("temp = new Class[" + paramTypes.length + "];\n");
            for (int n = 0; n < paramTypes.length; n++) {
                if (paramTypes[n].isPrimitive()) {
                    classInitializerBody.append("temp[" + n + "] = " + getClassTypeInitializer(paramTypes[n], false) +
                                                ";\n");
                } else {
                    classInitializerBody.append("temp[" + n + "] = Class.forName(\"" +
                                                getClassTypeInitializer(paramTypes[n], false) + "\");\n");
                }
            }
            classInitializerBody.append("overridenMethods[" + (methodsIndex) + "] = classes[" +
                                        classesIndexer.indexOf(reifiedMethods[i].getDeclaringClass().getName()) +
                                        "].getDeclaredMethod(\"" + reifiedMethods[i].getName() + "\", temp);\n");
            methodsIndex++;
        }

        classInitializerBody.append("\n}");
        //        System.out.println(classInitializerBody);
        classInitializer.setBody(classInitializerBody.toString());
    }

    /**
     * @param generatedClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public static CtMethod[] createStubObjectMethods(CtClass generatedClass)
            throws CannotCompileException, NotFoundException {
        CtField proxyField = new CtField(getClassPool().getDefault().get(Proxy.class.getName()),
                                         "myProxy",
                                         generatedClass);
        generatedClass.addField(proxyField);
        CtMethod proxyGetter = CtNewMethod.getter("getProxy", proxyField);
        generatedClass.addMethod(proxyGetter);
        CtMethod proxySetter = CtNewMethod.setter("setProxy", proxyField);
        generatedClass.addMethod(proxySetter);

        return new CtMethod[] { proxyGetter, proxySetter };
    }

    private static String getClassTypeInitializer(CtClass param, boolean elementInArray) throws NotFoundException {
        if (param.isArray()) {
            return "[" + getClassTypeInitializer(param.getComponentType(), true);
        } else if (param.equals(CtClass.byteType)) {
            return elementInArray ? "B" : "Byte.TYPE";
        } else if (param.equals(CtClass.charType)) {
            return elementInArray ? "C" : "Character.TYPE";
        } else if (param.equals(CtClass.doubleType)) {
            return elementInArray ? "D" : "Double.TYPE";
        } else if (param.equals(CtClass.floatType)) {
            return elementInArray ? "F" : "Float.TYPE";
        } else if (param.equals(CtClass.intType)) {
            return elementInArray ? "I" : "Integer.TYPE";
        } else if (param.equals(CtClass.longType)) {
            return elementInArray ? "J" : "Long.TYPE";
        } else if (param.equals(CtClass.shortType)) {
            return elementInArray ? "S" : "Short.TYPE";
        } else if (param.equals(CtClass.booleanType)) {
            return elementInArray ? "Z" : "Boolean.TYPE";
        } else if (param.equals(CtClass.voidType)) {
            return elementInArray ? "V" : "Void.TYPE";
        } else {
            return elementInArray ? ("L" + param.getName() + ";") : (param.getName());
        }
    }

    public static String wrapPrimitiveParameter(CtClass paramType, String paramString) {
        if (CtClass.booleanType.equals(paramType)) {
            return "new Boolean(" + paramString + ")";
        }
        if (CtClass.byteType.equals(paramType)) {
            return "new Byte(" + paramString + ")";
        }
        if (CtClass.charType.equals(paramType)) {
            return "new Character(" + paramString + ")";
        }
        if (CtClass.doubleType.equals(paramType)) {
            return "new Double(" + paramString + ")";
        }
        if (CtClass.floatType.equals(paramType)) {
            return "new Float(" + paramString + ")";
        }
        if (CtClass.intType.equals(paramType)) {
            return "new Integer(" + paramString + ")";
        }
        if (CtClass.longType.equals(paramType)) {
            return "new Long(" + paramString + ")";
        }
        if (CtClass.shortType.equals(paramType)) {
            return "new Short(" + paramString + ")";
        }

        // that should not happen
        return null;
    }

    static public boolean checkMethod(CtMethod met, CtMethod[] proxyMethods) throws NotFoundException {
        int modifiers = met.getModifiers();

        // Final reifiedMethods cannot be reified since we cannot redefine them
        // in a subclass
        if (Modifier.isFinal(modifiers)) {
            return false;
        }

        // Static reifiedMethods cannot be reified since they are not 'virtual'
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        // We allow reification of every methods but private ones 
        // the private methods can't be reified as the private method of the parent class 
        // will always be called instead of the stub one. 
        if ((Modifier.isPrivate(modifiers))) {
            return false;
        }

        // If method is finalize (), don't reify it
        if ((met.getName().equals("finalize")) && (met.getParameterTypes().length == 0)) {
            return false;
        }

        if ((met.getSignature().equals(proxyMethods[0].getSignature()) ||
             met.getSignature().equals(proxyMethods[1].getSignature()))) {
            return false;
        }

        return true;
    }

    private static void addSuperInterfaces(CtClass cl, List<CtClass> superItfs) throws NotFoundException {
        //        if (!cl.isInterface() && !Modifier.isAbstract(cl.getModifiers())) {
        //            // inspect interfaces AND abstract classes
        //            return;
        //        }
        CtClass[] super_interfaces = cl.getInterfaces();
        for (int i = 0; i < super_interfaces.length; i++) {
            superItfs.add(super_interfaces[i]);
            addSuperInterfaces(super_interfaces[i], superItfs);
        }
    }

    /**
     * return true if the annotation <code>annotation</code> is set on the member (field, constructor, method)
     *
     * @param member     the member (field, constructor, method) onto check the annotation's presence
     * @param annotation the annotation to check
     * @return returns true if the annotation <code>annotation</code> is set on the method
     */
    public static boolean hasAnnotation(CtMember member, Class<? extends Annotation> annotation) {
        Object[] o = member.getAvailableAnnotations();
        if (o != null) {
            for (Object object : o) {
                if (annotation.isAssignableFrom(object.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * return true if the annotation <code>annotation</code> is set on the member (field, constructor, method)
     *
     * @param ctClass    the member (field, constructor, method) onto check the annotation's presence
     * @param annotation the annotation to check
     * @return returns true if the annotation <code>annotation</code> is set on the method
     */
    public static boolean hasAnnotation(CtClass ctClass, Class<? extends Annotation> annotation) {
        Object[] o = ctClass.getAvailableAnnotations();
        if (o != null) {
            for (Object object : o) {
                if (annotation.isAssignableFrom(object.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T extends Annotation> T getAnnotation(CtMember member, Class<T> annotation) {
        Object[] o = member.getAvailableAnnotations();
        if (o != null) {
            for (Object object : o) {
                if (annotation.isAssignableFrom(object.getClass())) {
                    return annotation.cast(object);
                }
            }
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotation(CtClass ctClass, Class<T> annotation) {
        Object[] o = ctClass.getAvailableAnnotations();
        if (o != null) {
            for (Object object : o) {
                if (annotation.isAssignableFrom(object.getClass())) {
                    return annotation.cast(object);
                }
            }
        }
        return null;
    }

    private static void handleTurnRemoteAnnotation(Method method, StringBuilder body) {

        List<MethodParameter> lmp = method.getListMethodParameters();

        for (int i = 0; i < lmp.size(); i++) {
            MethodParameter mp = lmp.get(i);
            List<javassist.bytecode.annotation.Annotation> la = mp.getAnnotations();
            for (javassist.bytecode.annotation.Annotation annotation : la) {
                if (TurnRemote.class.getName().equals(annotation.getTypeName())) {
                    body.append("$" + (i + 1) + " = org.objectweb.proactive.api.PARemoteObject.turnRemote($" + (i + 1) +
                                "); \n");
                    logger.trace("method " + method.getCtMethod().getLongName() + ", param " + i +
                                 " has TurnRemote Annotation");
                    break;
                }
            }
        }
    }

    private static void handleTurnActiveAnnotation(Method method, StringBuilder body) {

        List<MethodParameter> lmp = method.getListMethodParameters();

        for (int i = 0; i < lmp.size(); i++) {
            MethodParameter mp = lmp.get(i);
            List<javassist.bytecode.annotation.Annotation> la = mp.getAnnotations();
            for (javassist.bytecode.annotation.Annotation annotation : la) {
                if (TurnActive.class.getName().equals(annotation.getTypeName())) {
                    body.append("$" + (i + 1) + " = org.objectweb.proactive.api.PAActiveObject.turnActive($" + (i + 1) +
                                "); \n");
                    logger.trace("method " + method.getCtMethod().getLongName() + ", param " + i +
                                 " has TurnActive Annotation");
                    break;
                }
            }
        }
    }

    private static void handleUnwrapFutureAnnotation(Method method, StringBuilder body) {
        List<MethodParameter> lmp = method.getListMethodParameters();

        for (int i = 0; i < lmp.size(); i++) {
            MethodParameter mp = lmp.get(i);
            List<javassist.bytecode.annotation.Annotation> la = mp.getAnnotations();
            for (javassist.bytecode.annotation.Annotation annotation : la) {

                if (UnwrapFuture.class.getName().equals(annotation.getTypeName())) {
                    body.append("$" + (i + 1) + " = org.objectweb.proactive.api.PAFuture.getFutureValue($" + (i + 1) +
                                "); \n");
                    logger.trace("method " + method.getCtMethod().getLongName() + ", param " + i +
                                 " has UnwrapFuture Annotation");
                    break;
                }
            }
        }
    }

}
