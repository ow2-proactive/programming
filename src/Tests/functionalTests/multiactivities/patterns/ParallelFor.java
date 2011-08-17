package functionalTests.multiactivities.patterns;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.multiactivity.MultiActiveService;

@DefineGroups({
    @Group(name="iteration", selfCompatible=true, parameter="java.lang.Integer", condition="equals")
})
public class ParallelFor implements RunActive {
    private static int MAX_THREADS;
    private static int OUTER_LOOP;
    AtomicInteger cnt;
    ParallelFor me;
    
    public ParallelFor() {
        /*cnt = new AtomicInteger();
        cnt.set(0);*/
    }
    
    private void parallelFor(int from, int to){
        System.out.println("Starting...");
        Date t1 = new Date();
        for (int i=from; i<to; i++) {
            me.forIteration(i);
        }
        me.forBarrier();
        Date t2 = new Date();
        System.out.println("total time="+(t2.getTime()-t1.getTime()));
    }
            
    @MemberOf("iteration")
    public void forIteration(Integer i) {
        //cnt.incrementAndGet();
        //do something
        //Date t1 = new Date();
        try {
            Thread.sleep(OUTER_LOOP);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      /*  ArrayList<Integer> list = new ArrayList<Integer>();
        
        for (int x=0; x<OUTER_LOOP; x++) {
            list.add(x);
            for (int y=0; y<x; y++) {
                list.set(y, list.get(y)+list.get(x));
            }
        }*/
        
        //Date t2 = new Date();
        
        //System.out.println(i+" time="+(t2.getTime()-t1.getTime()));
        //cnt.decrementAndGet();
    }
    
    public boolean forBarrier() {
        return true;
    }
    
    public static ParallelFor getInstance() throws ActiveObjectCreationException, NodeException{
        ParallelFor pf = PAActiveObject.newActive(ParallelFor.class, null);
        pf.me = pf;
        return pf;
    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        MAX_THREADS = Integer.parseInt(args[1]);
        OUTER_LOOP = Integer.parseInt(args[2]);
        ParallelFor pf = getInstance();
        pf.parallelFor(0, Integer.parseInt(args[0]));
        System.exit(0);
    }

    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(MAX_THREADS, true, false);
        
    }

}
