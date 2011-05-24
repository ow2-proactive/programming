package functionalTests.multiactivities.quicksort;

import java.util.Date;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;

public class ThreadQS implements Runnable {
    static NormalServe server;
    private int[] arr;
    private int left;
    private int right;

    public ThreadQS(int arr[], int left, int right){
        this.arr = arr;
        this.left = left;
        this.right = right;
    }
    
    public void quickSort(int arr[], int left, int right) {
        int index = server.partition(arr, left, right);
        Thread t = null;
        if (left < index - 1) {
            t = new Thread(new ThreadQS(arr, left, index - 1));
            t.start();
        }
        if (index < right) {
            quickSort(arr, index, right);
        }
        
        if (t!=null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        if (args.length!=1) {
            System.out.println("<array size>");
        }
        int LENGTH = Integer.parseInt(args[0]);
        int[] array = new int[LENGTH];
        for (int x=0; x<LENGTH; x++) {
            array[x]=(int) Math.round(Math.random()*1000);
        }
//        if (args[1].equals("MA")) {
//            server = PAActiveObject.newActive(MAServe.class, null);
//        } else {
            server = new NormalServe();
        //}
        Date start = new Date();
        new ThreadQS(array, 0, LENGTH-1).run();
        System.err.println("\n"+(new Date().getTime()-start.getTime()));
        
        boolean ok = true;
        
        for (int x=0; x<LENGTH; x++) {
            if (x>0 && array[x-1]>array[x]) {
                ok = false;
                System.err.println("\nFAIL @ "+x);
                return;
            }
            //System.out.print(array[x]+", ");
        }
        
        System.exit(0);
    }

    @Override
    public void run() {
       quickSort(arr, left, right);
        
    }
    
    public static class NormalServe {
        
        Integer partition(int arr[], int left, int right) {
            int i = left, j = right;
            int tmp;
            int pivot = arr[(left + right) / 2];
            while (i <= j) {
                while (arr[i] < pivot)
                    i++;
                while (arr[j] > pivot)
                    j--;
                if (i <= j) {
                    tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                    i++;
                    j--;
                }
            }
            return new Integer(i);
        }
        
    }
    
    /*@DefineGroups({
        @Group(name="part", selfCompatible=true)
    })       
    public static class MAServe extends NormalServe implements RunActive {
        
        @MemberOf("part")
        public IntWrapper partition(int arr[], int left, int right) {
            return super.partition(arr, left, right);
        }

        @Override
        public void runActivity(Body body) {
            new MultiActiveService(body).multiActiveServing();            
        }
        
    }
*/
}
