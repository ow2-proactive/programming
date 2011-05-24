package functionalTests.multiactivities.microbenchmark;

import java.awt.Color;
import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;


/**
 * Example from:
 * 
 * http://cedric.cnam.fr/PUBLIS/RC474.pdf
 * 
 * @author Jan Schäfer
 *
 */
public class ChamenosRedux {
    static final boolean DEBUG = false;
    
    public static Mall mall;

    public static void main(String[] args) {
        if (DEBUG)
            System.out.println("ChamenosRedux started");

        long start = System.nanoTime();

        int meetCount = 6000;
        if (args.length > 0) {
            meetCount = Integer.parseInt(args[0]);
        }

        new Run().run(meetCount, new Color[] { Color.BLUE, Color.RED, Color.YELLOW });

        new Run().run(meetCount, new Color[] { Color.BLUE, Color.RED, Color.YELLOW, Color.RED, Color.YELLOW,
                Color.BLUE, Color.RED, Color.YELLOW, Color.RED, Color.BLUE });

        // seriell: 19921
        // parallel: 23124

        long stop = System.nanoTime();
        System.out.println("TOTAL: " + ((stop - start) / 1000000) + " ms");
        System.exit(0);

    }

    public static class Run implements Serializable {

        public void run(int meetCount, Color... colors) {
            Object[] pa = { new Integer(meetCount) };
            //Mall mall;
            Creature[] creatures = new Creature[0];
            /*try {
                mall = PAActiveObject.newActive(Mall.class, pa);
                creatures = createCreatures(mall, colors);
            } catch (ActiveObjectCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            
            mall = new Mall(meetCount);
            
            try {
                creatures = createCreatures(mall, colors);
            } catch (Exception e) {
                // :-"
            }

            List<IntWrapper> meets = new LinkedList<IntWrapper>();
            for (Creature c : creatures) {
                meets.add(c.run());
            }

            int total = 0;
            for (IntWrapper i : meets) {
                total += i.getIntValue();
            }

            System.out.println("Total meets = " + total);
            if (total != (meetCount * 2))
                System.out.println("ERROR: total meet count wrong!");

        }
    }

    public static Creature[] createCreatures(Mall mall, Color... colors) throws ActiveObjectCreationException, NodeException {
        Creature[] result = new Creature[colors.length];
        int i = 0;
        for (Color c : colors) {
            Object[] pa = { i, null, c };
            result[i] = PAActiveObject.newActive(Creature.class, pa);
            System.out.println(" " + c);
            i++;
        }
        System.out.println();
        return result;
    }

    public enum Color {
        BLUE("blue"), RED("red"), YELLOW("yellow");

        String name;

        Color(String name) {
            this.name = name;
        }

        Color complement(Color c2) {
            Color c1 = this;
            if (c1 == c2)
                return c1;

            if (c1 == Color.BLUE) {
                return c2 == Color.RED ? Color.YELLOW : Color.RED;
            } else if (c1 == Color.RED) {
                return c2 == Color.BLUE ? Color.YELLOW : Color.BLUE;
            } else {
                return c2 == Color.BLUE ? Color.RED : Color.BLUE;
            }
        }

        public String toString() {
            return name;
        }
    }

    //@CoBox
    @DefineGroups({@Group(name = "defgr", selfCompatible = true)})
    public static class Creature implements RunActive, Serializable {
        private Color color;
        //private Mall mall;
        private int meetCount = -1;
        private int id;
        AtomicBoolean finished = new AtomicBoolean(false);

        public Creature() {
            // TODO Auto-generated constructor stub
        }

        public Creature(int id, Mall mall, Color c) {
            this.id = id;
            this.color = c;
            //this.mall = mall;
            if (DEBUG)
                System.out.println(id + " created with color " + c);
        }

        @MemberOf("defgr")
        public IntWrapper run() {
            this.meetNext(color);
            synchronized (finished) {
                try {
                    finished.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (DEBUG)
                System.out.println(id + " finished with " + meetCount + " meetings");
            return new IntWrapper(meetCount);
        }

        @MemberOf("defgr")
        public void meetNext(Color color) {
            if (color == null)
                return;

            meetCount++;
            mall.meet((Creature) PAActiveObject.getStubOnThis(), color);
        }

        @MemberOf("defgr")
        public void finish() {
            synchronized (finished) {
                finished.notifyAll();
            }
        }
        
        @Override
        public void runActivity(Body body) {
            //new Service(body).fifoServing();
            new MultiActiveService(body).multiActiveServing();
        }
    }

    //@CoBox
    public static class Mall implements RunActive, Serializable {
        int meetCounter;

        public Mall() {
            // TODO Auto-generated constructor stub
        }
        
        public Mall(Integer meetCount) {
            meetCounter = meetCount;
        }

        Color waitingColor;
        Creature waitingCreature;

        synchronized public void meet(Creature creature, Color c) {
            if (meetCounter == 0) {
                creature.finish();
                return;
            }

            if (waitingCreature != null) {
                Color complement = c.complement(waitingColor);
                creature.meetNext(complement);
                waitingCreature.meetNext(complement);
                meetCounter--;
                waitingCreature = null;
            } else {
                waitingCreature = creature;
                waitingColor = c;
            }
        }

        @Override
        public void runActivity(Body body) {
            new Service(body).fifoServing();
        }
    }

}
