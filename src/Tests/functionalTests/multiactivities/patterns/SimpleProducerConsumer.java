package functionalTests.multiactivities.patterns;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;

public class SimpleProducerConsumer {

    Queue<Integer> queue = new LinkedBlockingQueue<Integer>();

    public void produce() {
        boolean go = true;
        int x = 0;
        while (go) {
            synchronized (queue) {
                queue.add((int) Math.random());
                if (queue.size() == 1) {

                    queue.notifyAll();
                }
            }
            x++;
            if (x==20000) {
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

    public int consume() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        synchronized (queue) {
            if (queue.size() > 0) {
                return queue.remove();
            } else {

                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return queue.remove();
            }
        }

    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        final SimpleProducerConsumer pc = new SimpleProducerConsumer(); 
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                pc.produce();
                
            }
        }).start();
        
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
