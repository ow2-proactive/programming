package functionalTests.multiactivities.microbenchmark;

import java.io.*;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;

@DefineGroups(
        {
            @Group(name = "default", selfCompatible = true)
        }
)
public class Circle implements RunActive{

public static void main(String[] args) throws IOException, ActiveObjectCreationException, NodeException {
      int NUM = 100;
      
      Circle[] workers = new Circle[NUM];
      for (int i=0; i<NUM; i++) {
         workers[i] = PAActiveObject.newActive(Circle.class, null);
      }      
      workers[0].setNeighbour(workers[NUM-1]);
      for (int i=1; i<NUM; i++) {
         workers[i].setNeighbour(workers[(i-1)]);
      }
      
      
      workers[0].passFirstMessage();
   }

   public Circle() {
    // TODO Auto-generated constructor stub
   }

   private long startTime =  0;
   private int LIMIT = 100;
   private int messageCount = 0;
   private Circle neighbour;
   
   public void setNeighbour(Circle cw) {
      this.neighbour = cw;
   }
   
   @MemberOf("default")
   public void passFirstMessage() {
      startTime = System.currentTimeMillis();
      passMessage();
   }
   
   @MemberOf("default")
   public BooleanWrapper passMessage() {
      //System.out.println(this.toString());
      messageCount++;
      if (messageCount<LIMIT) {
         BooleanWrapper bw = neighbour.passMessage();
         /*if (!bw.getBooleanValue()){
             System.out.println("XXX");
         }*/
         /*while(true) {
            try {
               Thread.sleep(10);
            } catch (Exception e) {
               //
            }
         }*/
      } else {
         System.out.println(System.currentTimeMillis()-startTime);
      }
      
      return new BooleanWrapper(true);
   }

@Override
public void runActivity(Body body) {
    //new Service(body).fifoServing();
    new MultiActiveService(body).multiActiveServing(1, false, false);
    
}
   
}
