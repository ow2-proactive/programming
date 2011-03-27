package functionalTests.multiactivities.patterns;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingController;

@DefineGroups({
    @Group(name="queue", selfCompatible=true)
})
public class ProducerConsumer implements RunActive {
    Queue<Integer> queue = new LinkedBlockingQueue<Integer>();
    ServingController control;
    
    @MemberOf("queue")
    public void produce(){
        boolean go = true;
        int x = 0;
        while (go) {
            queue.add((int) Math.random());
            x++;
            if (x==20000) {
                control.incrementNumberOfConcurrent(20000);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                x=0;
            } 
        }
    }
    
    @MemberOf("queue")
    public int consume(){
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        control.decrementNumberOfConcurrent();
        return queue.remove();
    }

    @Override
    public void runActivity(Body body) {
        MultiActiveService mas = new MultiActiveService(body);
        control = mas.getServingController();
        mas.multiActiveServing(1);
    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        ProducerConsumer pc = PAActiveObject.newActive(ProducerConsumer.class, null);
        pc.produce();
        boolean ok = true;
        Date s = new Date();
        int cnt = 0;
        int bigCnt = 0;
        while (ok) {
            pc.consume();
            cnt++;
            if (cnt==10) {
                bigCnt++;
                cnt=0;
            }
            
            if (bigCnt%10==0 && cnt==0) {
                System.out.println("Avg. Speed = "+ ((bigCnt*10.0)/(((new Date().getTime()-s.getTime()))/1000.0))+" read/s");
                //s = new Date();
            }
        }
    }
    
    

}
