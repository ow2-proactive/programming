package org.objectweb.proactive.utils;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test added for checking the behaviour of ProActiveRandom.
 *
 * @author The ProActive Team
 */
public final class ProActiveRandomTest {

    private static final int NB_OPERATIONS = (int) 1e3;

    /**
     * Check that ProActiveSecureRandom methods can be called concurrently without
     * causing any deadlock or exception.
     *
     * @throws InterruptedException
     */
    @Test
    public void testProActiveRandomMethodsAgainstDeadlocks() throws InterruptedException {
        ExecutorService threadPool = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

        final CountDownLatch doneSignal = new CountDownLatch(NB_OPERATIONS);

        // the number of methods to use from ProActiveRandom
        // it is used to assign a code to each method
        final int nbOpsTested = 10;
        final AtomicInteger sequence = new AtomicInteger(nbOpsTested);

        for (int i = 0; i < NB_OPERATIONS; i++) {
            final int finalI = i;
            threadPool.execute(new Operation(finalI) {
                @Override
                public void run() {
                    int opCode;

                    // assign opCode to threads in a round robin manner
                    if (!sequence.compareAndSet(nbOpsTested, 1)) {
                        opCode = sequence.incrementAndGet();
                    } else {
                        opCode = 1;
                    }

                    // System.out.println("ProActiveRandomTest.run operationIndex=" + finalI + ", opCode=" + opCode);

                    switch (opCode) {
                        case 1:
                            ProActiveRandom.nextFloat();
                            break;
                        case 2:
                            ProActiveRandom.nextBoolean();
                            break;
                        case 3:
                            ProActiveRandom.nextBytes(new byte[8]);
                            break;
                        case 4:
                            ProActiveRandom.nextDouble();
                            break;
                        case 5:
                            ProActiveRandom.nextInt();
                            break;
                        case 6:
                            ProActiveRandom.nextInt();
                            break;
                        case 7:
                            ProActiveRandom.nextLong();
                            break;
                        case 8:
                            ProActiveRandom.nextPosInt();
                            break;
                        case 9:
                            ProActiveRandom.nextPosLong();
                            break;
                        case 10:
                            ProActiveRandom.nextString(100);
                            break;
                    }

                    // delay some threads in order to increase threads interleaving
                    if (opCode % 2 == 0) {
                        try {
                            Thread.sleep(new Random().nextInt(50));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    doneSignal.countDown();
                }
            });
        }

        // waits for the termination of all threads
        doneSignal.await();
        threadPool.shutdownNow();
    }

    /**
     * Check that a given number of String generation
     * produces Strings that contain only characters from
     * the set of characters that are allowed.
     */
    @Test
    public void testNextString() {
        // set containing allowed characters for quick test in O(1) instead of
        // O(n) when an array is used (n being the number of characters allowed)
        Set<Character> allowedCharacters = new HashSet<Character>(ProActiveRandom.SYMBOLS.length);

        for (int i = 0; i < ProActiveRandom.SYMBOLS.length; i++) {
            allowedCharacters.add(ProActiveRandom.SYMBOLS[i]);
        }

        for (int i = 0; i < NB_OPERATIONS; i++) {
            assertValidString(allowedCharacters, ProActiveRandom.nextString(100));
        }
    }

    private void assertValidString(Set<Character> allowedCharacters, String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            Assert.assertTrue("Invalid character detected: '" + c + "' is not contained in " +
                Arrays.toString(ProActiveRandom.SYMBOLS), allowedCharacters.contains(s.charAt(i)));
        }
    }

    private static abstract class Operation implements Runnable {

        public int index;

        public Operation(int index) {
            this.index = index;
        }

    }

}