package functionalTests.multiactivities.microbenchmark;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.multiactivity.MultiActiveService;


/**
 * BigPingPong example to measure performance.
 * In this example n coboxes communicate with each 
 * other by sending every other cobox a single message.
 * So n*n messages are sent in total.
 * 
 * This example is due to Sriram Srinivasan and translated
 * from his Kilim implementation to this JCoBox implementation.
 * 
 * The Kilim implementation is copyright by Sriram Srinivasan
 * under the MIT license defined as follows:
 *  
 * ======================================================================
 * This license is a copy of the MIT license.
 * - Sriram Srinivasan (kilim@malhar.net)
 * ======================================================================
 * Copyright (c) 2006 Sriram Srinivasan 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *   
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * The Kilim implementation can be downloaded from:
 * http://www.malhar.net/sriram/kilim/
 * 
 * @author Jan Schäfer
 *
 */

public class BigPingPong {
    public static final boolean DEBUG = false;
    public static final boolean STATS = false;

    public static final AtomicInteger msgSend = new AtomicInteger();
    
    public static Counter counter;

    public static void main(String args[]) throws Exception {
        int ncoboxes = 100;
        if (args.length > 0) {
            ncoboxes = Integer.parseInt(args[0]);
        }

        final BigCoBox[] coboxes = new BigCoBox[ncoboxes];
        //Object[] pars = { ncoboxes };
        //final Counter counter = PAActiveObject.newActive(Counter.class, pars);
        counter = new Counter(ncoboxes);

        for (int i = 0; i < ncoboxes; i++) {
            Object[] pars = new Object[2];
            pars[0] = i;
            pars[1] = null;//counter;
            coboxes[i] = PAActiveObject.newActive(BigCoBox.class, pars);
        }

        for (BigCoBox b : coboxes) {
            b.setOthers(coboxes);
        }
        
        long startTime = System.currentTimeMillis();
        
        for (BigCoBox b : coboxes) {
            b.start();
        }

        if (DEBUG)
            System.out.println("All CoBoxes started, waiting...");
        
        counter.waitUntilZero();
        long endtime = System.currentTimeMillis();
        System.out.println(endtime - startTime);
        
        if (STATS) {
            System.out.println("sent " + msgSend.get() + " msgs");
            System.out.println(msgSend.get() / ncoboxes + " msgs/cobox");
        }
        System.exit(0);
    }

    public static class Counter {// implements RunActive {
        AtomicInteger count;

        public Counter() {
            // TODO Auto-generated constructor stub
        }

        public Counter(int count) {
            this.count = new AtomicInteger(count);
        }

        public void dec() {
            count.decrementAndGet();
            if (DEBUG)
                System.out.println("Counter: dec=" + count);
            if (count.get() == 0) {
                synchronized (count) {
                    count.notifyAll();
                }

            }
        }

        public void waitUntilZero() {
            if (count.get() != 0)
                synchronized (count) {

                    try {
                        count.wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
        }

      /*  @Override
        public void runActivity(Body body) {
            new Service(body).fifoServing();
        }*/
    }

    @DefineGroups(value = { @Group(name = "default", selfCompatible = true) } )
    public static class BigCoBox implements RunActive {
        private AtomicInteger nrecvd = new AtomicInteger(0);
        private int nexpected;
        private int id;
        //private Counter counter;
        private BigCoBox[] coboxes;

        public BigCoBox() {
            // TODO Auto-generated constructor stub
        }

        public BigCoBox(int id, Counter counter) {
            this.id = id;
            //this.counter = counter;
        }

        public void setOthers(BigCoBox[] coboxes) {
            this.coboxes = coboxes;
            nexpected = coboxes.length - 1;
        }

        public void start() {
            if (STATS)
                msgSend.incrementAndGet();

            if (DEBUG)
                System.out.println("CoBox " + id + " started.");
            for (BigCoBox c : coboxes) {
                if (c != this) {
                    c.ping();
                }
            }
        }

        @MemberOf("default")
        public void ping() {
            if (STATS)
                msgSend.incrementAndGet();

            nrecvd.incrementAndGet();
            if (DEBUG)
                System.out.println("CoBox " + id + " ping " + (nexpected-nrecvd.get()) + " left");
            if (nrecvd.get() == nexpected) {
                counter.dec();
            }
        }

        @Override
        public void runActivity(Body body) {
            //new Service(body).fifoServing();
            new MultiActiveService(body).multiActiveServing(2, false, false);
        }
    }
}
