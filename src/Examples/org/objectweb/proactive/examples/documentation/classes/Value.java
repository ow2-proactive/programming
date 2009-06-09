package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * @author The ProActive Team
 *
 * Used to explain asynchronous behaviour.
 */
public class Value implements Serializable {
    IntWrapper age;
    String name;

    public Value() {
    }

    public Value(IntWrapper age, String name) {
        this.age = age;
        this.name = name;
    }

    public void bar() {
        this.age = new IntWrapper(0);
        this.name = "Anonymous";
    }

    //@snippet-start Continuation_3
    public Value bar(Value v, int nbYears) {
        v.setAge(new IntWrapper(v.getAge().intValue() + nbYears));
        return this;
    }

    //@snippet-end Continuation_3

    public void display() {
        System.out.println(this.name + " is " + this.age);
    }

    /**
     *
     *  GETTERS AND SETTERS
     *
     */

    public IntWrapper getAge() {
        return age;
    }

    public void setAge(IntWrapper age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
