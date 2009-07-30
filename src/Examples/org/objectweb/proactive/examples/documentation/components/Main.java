package org.objectweb.proactive.examples.documentation.components;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;


public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            //@snippet-start programing_with_components_1
            Factory factory = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();
            Component c = (Component) factory.newComponent("myADLDefinition", context);
            //@snippet-end programing_with_components_1
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

}
