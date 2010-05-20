package org.objectweb.proactive.examples.documentation.security;

public class A {

    private B b;

    public A() {
    }

    public A(B b) {
        this.b = b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public void displayB() {
        System.out.println(b);
    }
}
