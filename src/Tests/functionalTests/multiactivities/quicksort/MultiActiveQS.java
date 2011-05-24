package functionalTests.multiactivities.quicksort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;

@DefineGroups({
    @Group(name="part", selfCompatible=true)//, parameter = "functionalTests.multiactivities.quicksort.Region", comparator="equals")
}) 
public class MultiActiveQS implements RunActive, Serializable {
    private MultiActiveQS instance;
    private ArrayList<Integer> arr;

    private int partition(int left, int right) {
        int i = left, j = right;
        int tmp;
        int pivot = arr.get((left + right) / 2);
        while (i <= j) {
            while (arr.get(i) < pivot)
                i++;
            while (arr.get(j) > pivot)
                j--;
            if (i <= j) {
                tmp = arr.get(i);
                arr.set(i,arr.get(j));
                arr.set(j,tmp);
                i++;
                j--;
            }
        }
        return i;
    }
    
    public MultiActiveQS(){
        // for PA
    }
    
    @MemberOf("part")
    public BooleanWrapper quickSort(){
        return instance.quickSort(new Region(0, arr.size()-1));
    }
    
    @MemberOf("part")
    public BooleanWrapper quickSort(Region r) {
        //System.out.println(r);
        int index = partition(r.getFrom(), r.getTo());
        BooleanWrapper leftWait = null, rightWait = null;
        if (r.getFrom() < index - 1) {
            leftWait = instance.quickSort(new Region(r.getFrom(), index - 1));
        }
        if (index <  r.getTo()) {
            rightWait = instance.quickSort(new Region(index,  r.getTo()));
        }
       
        leftWait = new BooleanWrapper(leftWait!=null ? leftWait.getBooleanValue() : true);
        rightWait = new BooleanWrapper(rightWait!=null ? rightWait.getBooleanValue() : true);
        return new BooleanWrapper(leftWait.getBooleanValue() || rightWait.getBooleanValue());
    }
    
    public BooleanWrapper setArray(ArrayList<Integer> vals) {
        this.arr = vals;
        return new BooleanWrapper(true);
    }
    
    public ArrayList<Integer> getArray() {
        return arr;
    }

    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(2, true, true);
    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        int LENGTH = args.length>0 ? Integer.parseInt(args[0]) : 2000;
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int x=0; x<LENGTH; x++) {
            array.add(new Integer((int) Math.round(Math.random()*1000)));
        }
        MultiActiveQS qs = new MultiActiveQS();
        qs.instance = PAActiveObject.turnActive(qs);
        
        BooleanWrapper wait = qs.setArray(array);
        wait.getBooleanValue();
        System.out.println("started");
        Date start = new Date();
        
        Region r = new Region(0, array.size()-1);
        if (qs.quickSort(r).getBooleanValue()==true) {
            System.out.println(new Date().getTime()-start.getTime());
            array = qs.getArray();
        }
        
        if (array.isEmpty()) {
            System.err.println("\nFAIL");
            System.exit(0);
        }
        
        boolean ok = true;
        for (int x=0; x<LENGTH; x++) {
            if (x>0 && array.get(x-1)>array.get(x)) {
                ok = false;
                System.err.println("\nFAIL @ "+x);
                System.exit(0);
            }
            //System.out.print(array.get(x)+", ");
        }
                
        System.exit(0);
    }
    
        

}
