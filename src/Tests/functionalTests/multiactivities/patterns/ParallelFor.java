package functionalTests.multiactivities.patterns;

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
    @Group(name="iteration", selfCompatible=true)
})
public class ParallelFor implements RunActive {
    AtomicInteger cnt;
    ParallelFor me;
    
    public ParallelFor() {
        cnt = new AtomicInteger();
        cnt.set(0);
    }
    
    private void parallelFor(int from, int to){
        
        Date t1 = new Date();
        for (int i=from; i<to; i++) {
            me.forIteration(i);
        }
        me.forBarrier();
        Date t2 = new Date();
        System.out.println("total time="+(t2.getTime()-t1.getTime()));
    }
            
    @MemberOf("iteration")
    public void forIteration(int i) {
        cnt.incrementAndGet();
        //do something
        Date t1 = new Date();
        long d;
        for (int x=0; x<2000; x++) {
            for (int y=0; y<1000; y++) {
                d = (long) Math.random()/1000;
                d = d*10;
                if (cnt.get()>2) {
                    System.err.println("XXXXXXXXXXXXXXXXXXXXXX");
                }
            }
        }
        
        Date t2 = new Date();
        
        System.out.println(i+" time="+(t2.getTime()-t1.getTime()));
        cnt.decrementAndGet();
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
        ParallelFor pf = getInstance();
        pf.parallelFor(0, 50);
        System.exit(0);
    }

    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing();
        
    }

}
