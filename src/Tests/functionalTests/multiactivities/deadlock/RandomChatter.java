package functionalTests.multiactivities.deadlock;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

@DefineGroups(
        {
            @Group(name = "chat", selfCompatible = true),
            @Group(name = "one", selfCompatible = true),
            @Group(name = "two", selfCompatible = true)
        }
)
@DefineRules(
        {
            @Compatible({ "chat", "one" }),
            @Compatible({ "chat", "two" })
        }
)
public class RandomChatter implements RunActive {
    private RandomChatter[] group;
    
    public RandomChatter() {
    }
    
    public void setGroup(RandomChatter[] group) {
        this.group = group;
    }

    @MemberOf("chat")
    public void chatForever(){
        int TIMES = group.length*500;
        while (true) {
            List<IntWrapper> results = new LinkedList<IntWrapper>();
            for (int i=0; i<TIMES; i++) {
                int dest = ((int)(Math.random()*1000))%group.length;
                if (Math.random()<0.5) {
                    results.add(group[dest].speakToOne());
                } else {
                    results.add(group[dest].speakToTwo());
                }
            }
            
            for (int i=0; i<TIMES; i++) {
                results.get(i).getIntValue();
            }
        }        
    }
    
    @MemberOf("chat")
    public Integer chatFor(Integer count){
        Date s = new Date();
        List<IntWrapper> results = new LinkedList<IntWrapper>();
        for (int i=0; i<count; i++) {
            results.add(group[1].speakToOne());
        }        
        for (int i=0; i<count; i++) {
            results.get(i).getIntValue();
        }
        
        Date e = new Date();
        System.out.println(e.getTime()-s.getTime());
        return 0;
    }
    
    @MemberOf("chat")
    public BooleanWrapper isAlive(){
        return new BooleanWrapper(true);
    }
    
    @MemberOf("one")
    public IntWrapper speakToOne() {
        int k;
        for (int i=0; i<2000; i++) {
            for (int j=0; j<1000; j++) {
                k = i*j*j;
            }
        }
        //System.out.println(new Date());
        return new IntWrapper(0);
    }

    
    @MemberOf("two")
    public IntWrapper speakToTwo() {
        int time = (int)(Math.random()*500);
        long start = new Date().getTime();
        while (new Date().getTime() - start < time) {
            //:-" busy wait
        }
        //System.out.println(new Date());
        return new IntWrapper(0);
    }
    
    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(2, MultiActiveService.LIMIT_ALL_THREADS, MultiActiveService.REENTRANT_SAME_THREAD);
        //new Service(body).fifoServing();
        
    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException, InterruptedException {
        int COUNT = Integer.parseInt(args[0]);
        RandomChatter[] group = new RandomChatter[COUNT];
        
        for (int i=0; i<COUNT; i++){
            group[i] = PAActiveObject.newActive(RandomChatter.class, null);
        }
        
        for (int i=0; i<COUNT; i++){
            group[i].setGroup(group);
        }
        
        if (COUNT>2) {
        
            for (int i=0; i<COUNT; i++){
                group[i].chatForever();
            }
            
            while (true) {
                for (int i=0; i<COUNT; i++){
                    if (group[i].isAlive().getBooleanValue()==false) {
                        System.out.println("ERROR!");
                    }
                }
                System.out.println("Everyone computing at "+new Date());
                
                Thread.sleep(5000);
            }
        
        } else {
            System.out.println("Single test:");
            group[0].chatFor(Integer.parseInt(args[1]));
            
            for (int i=0; i<COUNT; i++){
                PAActiveObject.terminateActiveObject(group[i], true);
            }
            
            System.exit(0);
        }
        
        
    }

}
