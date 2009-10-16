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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.mop;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.PAProxyCustomBodyMethod;
import org.objectweb.proactive.annotation.PAProxyDoNotReifyMethod;
import org.objectweb.proactive.annotation.PAProxyEmptyMethod;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.lock.AbstractRemoteLocksManager;
import org.objectweb.proactive.core.mop.lock.RemoteLocksManager;
import org.objectweb.proactive.core.mop.proxy.PAProxy;


public class PAProxyBuilder {

    public static String PAPROXY_CLASSNAME_SUFFIX = "_PAProxy";

    public static String generatePAProxyClassName(String baseClass) {
        return baseClass + PAPROXY_CLASSNAME_SUFFIX;
    }

    public static String getBaseClassNameFromPAProxyName(String paproxyClassName) {
        int i = paproxyClassName.indexOf(PAPROXY_CLASSNAME_SUFFIX);

        return paproxyClassName.substring(0, i);
    }

    public static boolean doesClassNameEndWithPAProxySuffix(String className) {
        return className.endsWith(PAPROXY_CLASSNAME_SUFFIX);
    }

    public static boolean isPAProxy(Class<?> clazz) {
        return PAProxy.class.isAssignableFrom(clazz);
    }

    public static boolean hasPAProxyAnnotation(Class<?> clazz) throws NotFoundException {
        CtClass ctClass = ClassPool.getDefault().get(clazz.getName());
        return JavassistByteCodeStubBuilder.hasAnnotation(ctClass,
                org.objectweb.proactive.annotation.PAProxy.class);
    }

    public static byte[] generatePAProxy(String superClazzName) throws NotFoundException,
            CannotCompileException, IOException {
        ClassPool pool = ClassPool.getDefault();

        CtClass generatedCtClass = pool.makeClass(generatePAProxyClassName(superClazzName));

        // insert super class
        CtClass superCtClass = pool.get(superClazzName);

        if (superCtClass.isInterface()) {
            generatedCtClass.addInterface(superCtClass);
            generatedCtClass.setSuperclass(pool.get(Object.class.getName()));
        } else {
            generatedCtClass.setSuperclass(superCtClass);
        }

        if (!generatedCtClass.subtypeOf(pool.get(Serializable.class.getName()))) {
            generatedCtClass.addInterface(pool.get(Serializable.class.getName()));
        }

        generatedCtClass.addInterface(pool.get(PAProxy.class.getName()));
        generatedCtClass.addInterface(pool.get(RemoteLocksManager.class.getName()));
        generatedCtClass.addInterface(pool.get(InitActive.class.getName()));

        // mandatory fields

        CtField proxyField = new CtField(superCtClass, "proxiedModel", generatedCtClass);

        generatedCtClass.addField(proxyField);

        CtClass hashtableClass = pool.get(Hashtable.class.getName());

        CtField locksField = new CtField(hashtableClass, "locks", generatedCtClass);

        generatedCtClass.addField(locksField);

        //constructor 

        CtConstructor ctCons = new CtConstructor(new CtClass[] { superCtClass }, generatedCtClass);

        ctCons.setBody("{ this.proxiedModel = $1 ; this.locks = new java.util.Hashtable(); }");

        generatedCtClass.addConstructor(ctCons);

        // empty constructor 
        ctCons = new CtConstructor(new CtClass[] {}, generatedCtClass);
        ctCons.setBody("{}");
        generatedCtClass.addConstructor(ctCons);

        // extends base class

        java.util.Map<String, Method> temp = new HashMap<String, Method>();
        List<String> classesIndexer = new Vector<String>();

        temp = JavassistByteCodeStubBuilder.methodsIndexer(superCtClass, classesIndexer);

        // add methods from base class

        java.util.Map<String, Method> filtered = new HashMap<String, Method>();

        Iterator<String> is = temp.keySet().iterator();

        while (is.hasNext()) {

            String key = is.next();
            Method m = temp.get(key);
            CtMethod ctMethod = m.getCtMethod();
            if (!Modifier.isPrivate(ctMethod.getModifiers()) && !Modifier.isNative(ctMethod.getModifiers()) &&
                !Modifier.isFinal(ctMethod.getModifiers()) &&
                !JavassistByteCodeStubBuilder.hasAnnotation(ctMethod, PAProxyDoNotReifyMethod.class)) {
                //                System.out.println("adding " + m.getCtMethod().getLongName() + " attr " +
                //                    Modifier.toString(m.getCtMethod().getModifiers()));
                filtered.put(key, m);
            }
            //            else {
            //                System.out.println("discarding " + m.getCtMethod().getLongName() + " attr " +
            //                    Modifier.toString(m.getCtMethod().getModifiers()));
            //            }
        }

        temp = filtered;
        is = temp.keySet().iterator();
        while (is.hasNext()) {

            Method m = temp.get(is.next());
            CtMethod ctMethod = m.getCtMethod();
            CtClass returnType = ctMethod.getReturnType();
            String body = "";
            if (m.hasMethodAnnotation(PAProxyEmptyMethod.class)) {
                body = "{}";
            } else if (m.hasMethodAnnotation(PAProxyCustomBodyMethod.class)) {
                PAProxyCustomBodyMethod papcbm = JavassistByteCodeStubBuilder.getAnnotation(ctMethod,
                        PAProxyCustomBodyMethod.class);
                String b = papcbm.getBody();
                if (b != null) {
                    body = b;
                }
            } else {

                if (returnType != CtClass.voidType) {
                    body = " { return ($r) this.proxiedModel." + ctMethod.getName() + "($$); }";
                } else {
                    body = " {this.proxiedModel." + ctMethod.getName() + "($$); }";
                }
            }
            CtMethod methodToGenerate = null;
            try {
                methodToGenerate = CtNewMethod.copy(ctMethod, generatedCtClass, null);
                methodToGenerate.setBody(body.toString());
                methodToGenerate.setModifiers(methodToGenerate.getModifiers() & ~Modifier.ABSTRACT);
                //                System.out.println("PAProxyBuilder.generatePAProxy()" + methodToGenerate.getLongName() +
                //                    " attr " + Modifier.toString(methodToGenerate.getModifiers()));
                generatedCtClass.addMethod(methodToGenerate);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        }

        //// initactivity

        CtMethod initActivity = CtNewMethod.make(
                " public void initActivity(org.objectweb.proactive.Body body) { " +
                    "java.lang.reflect.Method[] m = " + generatedCtClass.getName() +
                    ".class.getDeclaredMethods();\n" + "for ( int i = 0 ; i< m.length; i++) { " +
                    " org.objectweb.proactive.api.PAActiveObject.setImmediateService(m[i].getName(),true);" +
                    " } " + " } ", generatedCtClass);

        generatedCtClass.addMethod(initActivity);

        //// getWrapper.

        generatedCtClass.addMethod(CtNewMethod.make("public Object "
            + " getTarget() { return this.proxiedModel; }", generatedCtClass));

        // RemoteLockManager 

        temp = JavassistByteCodeStubBuilder.methodsIndexer(superCtClass, classesIndexer);

        CtClass rLockManagerClazz = pool.get(AbstractRemoteLocksManager.class.getName());

        java.util.Map<String, Method> rLockManagerMethods = JavassistByteCodeStubBuilder.methodsIndexer(
                rLockManagerClazz, classesIndexer);

        Iterator<Entry<String, Method>> iterateLockManagerMethod = rLockManagerMethods.entrySet().iterator();

        CtMethod[] rlmDeclaredMethods = rLockManagerClazz.getDeclaredMethods();

        for (CtMethod ctMethod2 : rlmDeclaredMethods) {

            CtClass bodyClass = pool.get(Body.class.getName());

            CtMethod methodToGenerate = null;

            if (!Modifier.isNative(ctMethod2.getModifiers()) && !Modifier.isFinal(ctMethod2.getModifiers())) {

                try {
                    methodToGenerate = CtNewMethod.copy(ctMethod2, generatedCtClass, null);

                    methodToGenerate.setModifiers(methodToGenerate.getModifiers() & ~Modifier.ABSTRACT);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            generatedCtClass.addMethod(methodToGenerate);

        }

        generatedCtClass.debugWriteFile(PAProperties.PA_MOP_GENERATEDCLASSES_DIR.getValue());

        byte[] bytecode = generatedCtClass.toBytecode();

        generatedCtClass.detach();

        return bytecode;
    }

}
