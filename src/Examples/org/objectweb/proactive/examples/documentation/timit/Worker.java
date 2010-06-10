package org.objectweb.proactive.examples.documentation.timit;

import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.extensions.timitspmd.util.TimItStore;
import org.objectweb.proactive.extensions.timitspmd.util.Timed;
import org.objectweb.proactive.extensions.timitspmd.util.TimerCounter;
import org.objectweb.proactive.extensions.timitspmd.util.observing.EventObserver;
import org.objectweb.proactive.extensions.timitspmd.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.extensions.timitspmd.util.observing.commobserv.CommEventObserver;


//@snippet-start TimIt_Worker_Class
public class Worker extends Timed {

    /* Declaration of all TimerCounters and EventObservers */

    /**
     * 
     */
    private static final long serialVersionUID = 430L;

    /** Total time */
    public TimerCounter T_TOTAL, T_INIT, T_END;

    /** Communication Observer */
    public EventObserver E_COMM;

    private int rank, groupSize;

    // An empty no args constructor, as needed by ProActive
    public Worker() {
    }

    public void start() {
        this.rank = PASPMD.getMyRank();
        this.groupSize = PASPMD.getMySPMDGroupSize();

        // First you have to get an instance of TimerStore for this
        // active object.
        // You can create counters with this TimerStore
        // for distribute these counters to every class in this active
        // object.
        // IMPORTANT : TimerStore instance is note integrated in the active
        // object so problems may occur with Fault Tolerance and migration.
        // If you have to do that, do not use TimerStore and pass Counters
        // by another way.
        TimItStore ts = TimItStore.getInstance(this);
        // Add timer counters to the TimItStore

        // Register the TimerCounters and EventObservers
        T_TOTAL = ts.addTimerCounter(new TimerCounter("total"));
        T_INIT = ts.addTimerCounter(new TimerCounter("init"));
        T_END = ts.addTimerCounter(new TimerCounter("end"));
        E_COMM = (CommEventObserver) ts.addEventObserver(new CommEventObserver("communicationPattern",
            this.groupSize, this.rank));

        // You must activate TimItStore before using your counters and observers
        // It is also possible to activate all your counters and observer at once
        // using ts.activation() but you have to set the proactive.timit.activation
        // property. See existing timit examples to learn more about this possibility.
        super.activate(new EventObserver[] { this.E_COMM });
        super.activate(new TimerCounter[] { this.T_TOTAL, this.T_INIT, this.T_END });

        // Then, you can use your counters and observers
        T_TOTAL.start();

        T_INIT.start();
        this.sleep(251);
        T_INIT.stop();

        int destRank;
        for (int i = 0; i < 10; i++) {
            destRank = (this.rank + 1) % this.groupSize;
            super.getEventObservable().notifyObservers(new CommEvent(this.E_COMM, destRank, 1));
            if (this.rank > 3)
                this.sleep(20);
        }

        T_END.start();
        this.sleep(101);
        T_END.stop();

        T_TOTAL.stop();

        // Finally, you have to say that timing is done by using finalizeTimer()
        // method. You can specify some textual informations about this worker.
        // This information will be shown in final XML result file.
        // Take care when using it with many nodes... :)
        super.finalizeTimed(this.rank, "Worker" + this.rank + " is OK.");
    }

    //@snippet-break TimIt_Worker_Class
    public void sleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //@snippet-resume TimIt_Worker_Class
}
//@snippet-end TimIt_Worker_Class
