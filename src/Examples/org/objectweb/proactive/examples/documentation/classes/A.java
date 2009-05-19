package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;


/**
 * @author ffonteno
 *
 */
//@snippet-start class_A
public class A implements Serializable {
    protected String str = "Hello";

    /**
     * Empty no-arg constructor
     */
    public A() {
    }

    /**
     * Constructor which initializes str
     *
     * @param str
     */
    public A(String str) {
        this.str = str;
    }

    /**
     * setStrWrapper
     *
     * @param strWrapper StringWrapper to set
     *      StringWrapper is used since this method gives an example
     *      of parameter scatterring. Parameter need to be serializable.
     * @return this
     */
    public A setStrWrapper(StringWrapper strWrapper) {
        this.str = strWrapper.stringValue();
        return this;
    }

    /**
     * getB
     *
     * @return a B object corresponding to the current Object
     */
    public B getB() {
        if (this instanceof B) {
            return (B) this;
        } else {
            return new B(str);
        }
    }

    /**
     * display str on the standard output
     */
    public void display() {
        System.out.println("A display =====> " + str);
    }
}
// @snippet-end class_A