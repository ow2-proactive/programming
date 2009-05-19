package org.objectweb.proactive.examples.documentation.classes;

/**
 * @author ffonteno
 *
 */
//@snippet-start class_B
public class B extends A {
    /**
     * Empty no-arg constructor
     */
    public B() {
    }

    /**
     * Constructor which initializes str
     *
     * @param str
     */
    public B(String str) {
        super(str);
    }

    /**
     * display str on the standard output
     */
    public void display() {
        System.out.println("B display =====> " + str);
    }
}
//@snippet-end class_B