/*
 * Created on Jun 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.examples.robustarith;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import java.math.BigInteger;


/**
 * @author gchazara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Main {
    public static void main(String[] args) {
        Formula f = new Formula() {
                private final Ratio ONE_64 = new Ratio(BigInteger.ONE,
                        new BigInteger("64"));

                private BigInteger i2b(int i) {
                    return new BigInteger("" + i);
                }

                private Ratio ratio(int x, int a, int b, int c) {
                    int denum = (b * x) + c;
                    return new Ratio(i2b(a), i2b(denum));
                }

                public Ratio eval(int x) throws OverflowException {
                    boolean even = (x & 1) == 0;
                    BigInteger firstNum = even ? BigInteger.ONE : Int.MINUS_ONE;
                    Ratio first = new Ratio(firstNum, Int.pow2(10 * x));
                    Ratio r = new Ratio(BigInteger.ZERO, BigInteger.ONE);
                    r.add(ratio(x, -32, 4, 1));
                    r.add(ratio(x, -1, 4, 3));
                    r.add(ratio(x, 256, 10, 1));
                    r.add(ratio(x, -64, 10, 3));
                    r.add(ratio(x, -4, 10, 5));
                    r.add(ratio(x, -4, 10, 7));
                    r.add(ratio(x, 1, 10, 9));
                    r.mul(first);
                    r.mul(ONE_64);

                    return r;
                }
            };

        //ProActive.tryWithCatch(java.io.IOException.class);
        ProActive.tryWithCatch(Exception.class);
        try {
        	String path = (args.length == 0) ? "descriptors/Matrix.xml" : args[0];
            ProActiveDescriptor pad = ProActive.getProactiveDescriptor("file:" + path);
            VirtualNode dispatcher = pad.getVirtualNode("matrixNode");
            dispatcher.activate();
            Node[] nodes = dispatcher.getNodes();
            Sum s = new Sum(nodes);
            Ratio r = s.eval(f, 0, 40);
            System.out.println(r);
            ProActive.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ProActive.removeTryWithCatch();
        }
        System.exit(0);
    }
}
