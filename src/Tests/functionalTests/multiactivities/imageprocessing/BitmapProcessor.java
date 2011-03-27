package functionalTests.multiactivities.imageprocessing;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.multiactivity.MultiActiveService;

@DefineGroups( 
        { 
        @Group(name = "add_remove", selfCompatible = true, parameter = "java.lang.String", comparator="equals"),
        @Group(name = "info", selfCompatible = true),
        @Group(name = "work", selfCompatible = true, parameter = "functionalTests.multiactivities.imageprocessing.BitmapRegion", comparator="overlaps")
        }
)
@DefineRules( 
        { 
        @Compatible( value={ "add_remove", "work" }, comparator="functionalTests.multiactivities.imageprocessing.BitmapRegion.sameName"),
        @Compatible( value={ "info", "work" })
        }
)
public class BitmapProcessor implements RunActive, Serializable {
    public static final int OP_BLUR = 0;
    public static final int OP_GRAYSCALE = 0;
    public static final int OP_INVERT = 0;
    
    private ConcurrentMap<String, Bitmap> bitmaps;
    
    public BitmapProcessor() {
        bitmaps = new ConcurrentHashMap<String, Bitmap>();
    }
    
    @MemberOf("add_remove")
    public void add(String name, Bitmap data){
        System.out.println("added "+name);
        bitmaps.put(name, data);
    }
    
    @MemberOf("add_remove")
    public Boolean remove(String name) {
        System.out.println("removed "+name);
        bitmaps.remove(name);
        return true;
    }
    
    @MemberOf("add_remove")
    public Bitmap get(String name) {
        return bitmaps.get(name);
    }
    
    @MemberOf("info")
    public Integer getBitmaps(){
        return bitmaps.keySet().size();
    }
    
    @MemberOf("add_remove")
    public BitmapRegion getFullRegion(String name) {
        if (bitmaps.containsKey(name)) {
            return new BitmapRegion(name, 0, 0, bitmaps.get(name).getWidth(), bitmaps.get(name).getHeight());
        } else {
            return null;
        }
        
    }
    
    @MemberOf("work")
    public void applyOperation(BitmapRegion region, int operationType) {
        if (!bitmaps.containsKey(region.getBitmapName())) {
            System.out.println("Bitmap "+region+" not found!");
            return;
        }
        
        if (operationType == OP_GRAYSCALE) {
            internalGrayscale(region, bitmaps.get(region.getBitmapName()));
        }
    }

    private void internalGrayscale(BitmapRegion region, Bitmap data) {
        for (int i = region.getX(); i<region.getX()+region.getWidth(); i++) {
            for (int j = region.getY(); j<region.getY()+region.getHeight(); j++) {
                byte[] pixel = data.getRGB(i, j);
                byte avg = (byte) (((int) (pixel[0]+pixel[1]+pixel[2]))/3);
                pixel[0] = avg;
                pixel[1] = avg;
                pixel[2] = avg;
                data.setRGB(i, j, pixel);
            }
        }
    }

    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(2, MultiActiveService.LIMIT_ALL_THREADS, MultiActiveService.REENTRANT_SEPARATE_THREAD);
    }
    
    public static BitmapProcessor newBitmapProcessorAO(){
        try {
            return PAActiveObject.newActive(BitmapProcessor.class, null);
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) throws InterruptedException {
        final BitmapProcessor bmp = BitmapProcessor.newBitmapProcessorAO();
        final AtomicInteger cnt = new AtomicInteger();
        cnt.set(0);
        
        Runnable r = new Runnable() {
            
            @Override
            public void run() {
                int SIZE = 50;
                Bitmap data = new Bitmap(SIZE, SIZE);
                String name = "Bitmap"+Math.random();
                bmp.add(name, data);

                for (int i=0; i<1000; i++) {
                    int x = (int) (Math.random()*SIZE);
                    int y = (int) (Math.random()*SIZE);
                    int w = (int) (Math.random()*(SIZE-x));
                    int h = (int) (Math.random()*(SIZE-y));
                    bmp.applyOperation(new BitmapRegion(name, x, y, w, h), BitmapProcessor.OP_GRAYSCALE);
                }
                bmp.remove(name);
                cnt.getAndDecrement();
            }
        };
        
        for (int i=0; i<10; i++) {
            cnt.getAndIncrement();
            (new Thread(r)).start();
        }
        
        while (cnt.get()>0) {
            Thread.sleep(500);
        }
        
        System.exit(0);
    }
}
