package functionalTests.activeobject.initialization;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;


/**
 * Test for highlighting the issue reported for PALIGHT-73.
 *
 * The idea of the functional tests that are defined below is to test several
 * behaviours depending on whether an ActiveObject throws an exception during
 * its initialization and whether a synchronous, one-way or method call with
 * future and forced synchronization is performed.
 *
 * @author The ProActive Team
 */
@RunWith(Parameterized.class)
public final class InitActiveTest {

    @Parameterized.Parameters(name = "{1}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Tests that should work even before applying the patch for PALIGHT-73
                { new AsynchronousMethodCallBehaviour(DefaultInitActiveFunction.class, false),
                        "testInitActiveWithAoPerformingAsyncMethodCall" },
                { new SynchronousMethodCallBehaviour(DefaultInitActiveFunction.class, false),
                        "testInitActiveWithAoPerformingSyncMethodCall"

                },
                { new OneWayMethodCallBehaviour(DefaultInitActiveFunction.class, false),
                        "testInitActiveWithAoPerformingSyncOneWayMethodCall"

                },
                {
                        // For this test the method call is one-way so the caller does not
                        // wait for a result. Consequently, no exception is expected
                        new OneWayMethodCallBehaviour(NastyInitActiveFunction.class, false),
                        "testInitActiveThrowingExceptionWithAoPerformingOneWayMethodCall" },
                // Tests that should work only when PALIGHT-73 is applied
                { new AsynchronousMethodCallBehaviour(NastyInitActiveFunction.class, true),
                        "testInitActiveThrowingExceptionWithAoPerformingAsyncMethodCall"

                },
                { new SynchronousMethodCallBehaviour(NastyInitActiveFunction.class, true),
                        "testInitActiveThrowingExceptionWithAoPerformingSyncMethodCall" }, });
    }

    private ActiveObject activeObject;

    public Behaviour testBehaviour;

    public InitActiveTest(Behaviour testBehaviour, String name) throws ActiveObjectCreationException,
            NodeException, InstantiationException, IllegalAccessException {
        this.testBehaviour = testBehaviour;
        this.activeObject = createActiveObject(testBehaviour.initActiveFunctionClass);
    }

    @Test(timeout = 60000)
    public void test() throws Throwable {
        try {
            testBehaviour.execute(activeObject);
        } catch (Throwable t) {
            // Ignores some exceptions since it is a correct test behaviour for tests involving patch PALIGHT-73
            // Indeed, the callers receive an exception describing the exception thrown by initActive
            if (!testBehaviour.exceptionExpected) {
                throw t;
            }
        }
    }

    private ActiveObject createActiveObject(Class<? extends InitActiveFunction> clazz)
            throws ActiveObjectCreationException, NodeException, IllegalAccessException,
            InstantiationException {
        return PAActiveObject.newActive(ActiveObject.class, new Object[] { clazz.newInstance() });
    }

    public static class ActiveObject implements InitActive {

        private InitActiveFunction initActiveFunction;

        public ActiveObject() {

        }

        public ActiveObject(InitActiveFunction initActiveFunction) {
            this.initActiveFunction = initActiveFunction;
        }

        @Override
        public void initActivity(Body body) {
            this.initActiveFunction.execute();
        }

        public BooleanWrapper methodCallWithFuture() {
            return new BooleanWrapper(true);
        }

        public void oneWayMethodCall() {
            int[] array = new int[42];

            // dummy computation
            for (int i = 0; i < array.length; i++) {
                array[i] = i;
                if (i > 0) {
                    array[i] = array[i] * array[i - 1];
                }
            }
        }

        public int synchronousMethodCall() {
            return 42;
        }

    }

    public static final class AsynchronousMethodCallBehaviour extends Behaviour {

        public AsynchronousMethodCallBehaviour(Class<? extends InitActiveFunction> initActiveFunctionClass,
                boolean exceptionExpected) {
            super(initActiveFunctionClass, exceptionExpected);
        }

        @Override
        public void execute(ActiveObject activeObject) {
            PAFuture.getFutureValue(activeObject.methodCallWithFuture());
        }

    }

    public static final class OneWayMethodCallBehaviour extends Behaviour {

        public OneWayMethodCallBehaviour(Class<? extends InitActiveFunction> initActiveFunctionClass,
                boolean exceptionExpected) {
            super(initActiveFunctionClass, exceptionExpected);
        }

        @Override
        public void execute(ActiveObject activeObject) {
            activeObject.oneWayMethodCall();
        }

    }

    public static final class SynchronousMethodCallBehaviour extends Behaviour {

        public SynchronousMethodCallBehaviour(Class<? extends InitActiveFunction> initActiveFunctionClass,
                boolean exceptionExpected) {
            super(initActiveFunctionClass, exceptionExpected);
        }

        @Override
        public void execute(ActiveObject activeObject) {
            activeObject.synchronousMethodCall();
        }

    }

    public abstract static class Behaviour {

        private final Class<? extends InitActiveFunction> initActiveFunctionClass;

        private boolean exceptionExpected;

        public Behaviour(Class<? extends InitActiveFunction> initActiveFunctionClass,
                boolean exceptionExpected) {
            this.initActiveFunctionClass = initActiveFunctionClass;
            this.exceptionExpected = exceptionExpected;
        }

        public abstract void execute(ActiveObject activeObject);

    }

    public final static class NastyInitActiveFunction extends DefaultInitActiveFunction {

        @Override
        public void execute() {
            super.execute();

            throw new RuntimeException("Nasty exception causing an issue in bad days");
        }

    }

    public static class DefaultInitActiveFunction implements InitActiveFunction {

        @Override
        public void execute() {
            // sleep timeout to ensure that the exception is not thrown before
            // to receive a method call on the active object
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static interface InitActiveFunction extends Serializable {

        void execute();

    }

}