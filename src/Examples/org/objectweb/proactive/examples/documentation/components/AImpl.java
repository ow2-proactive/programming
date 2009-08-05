package org.objectweb.proactive.examples.documentation.components;

import org.objectweb.proactive.examples.components.helloworld.Service;


//@snippet-start component_examples_13
//@snippet-start component_examples_2
public class AImpl implements A {

    Service service;

    public AImpl() {
    }

    //@snippet-break component_examples_13
    public String bar() {
        return "bar";
    }

    //@snippet-resume component_examples_13

    //@snippet-break component_examples_2
    // implementation of the A interface
    public void foo() {
        service.print("Hello World"); // for example
    }

    // implementation of BindingController
    public Object lookupFc(final String cItf) {
        if (cItf.equals("requiredService")) {
            return service;
        }
        return null;
    }

    // implementation of BindingController
    public void bindFc(final String cItf, final Object sItf) {
        if (cItf.equals("requiredService")) {
            service = (Service) sItf;
        }
    }

    // implementation of BindingController
    public void unbindFc(final String cItf) {
        if (cItf.equals("requiredService")) {
            service = null;
        }
    }
    //@snippet-resume component_examples_2
}
//@snippet-end component_examples_2
//@snippet-end component_examples_13
