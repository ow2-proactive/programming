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
package functionalTests.mop.concurrency;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import javassist.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


/**
 * The purpose of this class is to test concurrency issues that may occurs between
 * some MOP and Javassist classes through direct or indirect method calls invoked
 * in parallel.
 * <p/>
 * It was introduced for the following bug reports: PALIGHT-46, PROACTIVE-1027.
 *
 * @author The ProActive Team
 */
public class MOPConcurrencyTest {

    public static final int NUMBER_OF_CLASSES = 500;

    public static final int NUMBER_OF_THREADS = 20;

    public static final String METHOD_NAME = "hello";

    private ExecutorService threadPool;

    private List<String> classNames;

    /**
     * Setups a few things that are reused for all tests. In particular, it creates
     * new reifiable classes and loads them into MOP caches. Then, these classes are
     * used to create stubs during the tests.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        String classNameBase = MOPConcurrencyTest.class.getName();

        classNames = new ArrayList<String>(NUMBER_OF_CLASSES);
        for (int i = 1; i <= NUMBER_OF_CLASSES; i++) {
            classNames.add(classNameBase + i);
        }

        // creates new classes that are reifiable and that can be used to create
        // stubs later in the test
        try {
            loadClassesIntoMop(classNames);
        } catch (CannotCompileException e) {
            // Javassist compilation errors may occur if classes are already generated and loaded
        }

        threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    /**
     * Invokes MOP#newInstance and JavassistByteCodeStubBuilder#create in parallel from
     * multiple threads to reproduce a deadlock that was reported.
     *
     * @throws Throwable
     */
    @Test(timeout = 120000)
    public void testConcurrencyBetweenNewMopInstanceAndStubCreation() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);

        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int index = i;
            tasks.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // we synchronize all workers to maximize concurrency issues
                    latch.countDown();
                    latch.await();

                    for (String className : classNames) {
                        if (index % 2 == 0) {
                            StubObject stub = (StubObject) MOP.newInstance(className,
                                                                           new Class[0],
                                                                           null,
                                                                           Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME,
                                                                           null);
                        } else {
                            JavassistByteCodeStubBuilder.create(className, null);
                            Thread.sleep(20);
                        }
                    }

                    return true;
                }
            });
        }

        List<Future<Boolean>> results = threadPool.invokeAll(tasks);

        for (Future<Boolean> result : results) {
            Assert.assertFalse(result.isCancelled());
            Assert.assertTrue(result.get());
        }
    }

    /**
     * As for the previous test, this one checks whether the invocation of two
     * different methods causes a deadlock or not. The methods used for the test
     * are the one reported for an issue.
     *
     * @throws Throwable
     */
    @Test(timeout = 120000)
    public void testConcurrencyBetweenNewActiveAndGetClassData() throws Throwable {
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();

        for (int i = 0; i < NUMBER_OF_CLASSES; i++) {
            final int finalI = i;
            tasks.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws InterruptedException {
                    // System.out.println("MOPConcurrencyTest.call threadId ==> " + Thread.currentThread().getId());

                    if (finalI % 2 == 0) {
                        try {
                            PAActiveObject.newActive(classNames.get(finalI), null);
                        } catch (ActiveObjectCreationException e) {
                            e.printStackTrace();
                        } catch (NodeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ProActiveRuntimeImpl.getProActiveRuntime().getClassData(classNames.get(finalI));
                    }

                    return Boolean.TRUE;
                }
            });
        }

        List<Future<Boolean>> results = threadPool.invokeAll(tasks);

        for (Future<Boolean> result : results) {
            Assert.assertFalse(result.isCancelled());
            Assert.assertTrue(result.get());
        }
    }

    @After
    public void tearDown() {
        threadPool.shutdownNow();
    }

    /**
     * This method generates empty classes and adds them to the MOP.
     *
     * @param classNames
     * @throws Exception
     */
    private void loadClassesIntoMop(List<String> classNames) throws Exception {
        ArrayList<String> answer = new ArrayList();

        ClassPool pool = ClassPool.getDefault();
        CtClass serializableClass = pool.get("java.io.Serializable");
        CtClass stringClass = pool.get("java.lang.String");
        Set<Map.Entry<String, Class>> classesToLoad = new HashSet<Map.Entry<String, Class>>();

        //create new classes
        for (int i = 0; i < classNames.size(); i++) {
            String className = classNames.get(i);

            CtClass cc = pool.makeClass(className);
            cc.addInterface(serializableClass);

            //create no arg constructor
            CtConstructor cons = CtNewConstructor.defaultConstructor(cc);
            cc.addConstructor(cons);
            Random rn = new Random();
            int randomClassIndex = rn.nextInt(i + 1);

            CtMethod exec1 = CtNewMethod.make(javassist.Modifier.PUBLIC,
                                              pool.get(classNames.get(randomClassIndex)),
                                              METHOD_NAME,
                                              new CtClass[0],
                                              new CtClass[0],
                                              "return new " + classNames.get(randomClassIndex) + "();",
                                              cc);
            cc.addMethod(exec1);

            classesToLoad.add(new AbstractMap.SimpleEntry(className, pool.toClass(cc)));
            answer.add(className);
            cc.defrost();
        }

        Field loadedClassField = MOP.class.getDeclaredField("loadedClass");
        loadedClassField.setAccessible(true);
        Map<String, Class<?>> loadedClass = (Map<String, Class<?>>) loadedClassField.get(null);
        for (Map.Entry<String, Class> entry : classesToLoad) {
            loadedClass.put(entry.getKey(), entry.getValue());
        }
    }

    public static class ActiveObject implements Serializable {

        public ActiveObject() {
        }

        public boolean test() {
            return true;
        }

    }

}
