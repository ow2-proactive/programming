package org.objectweb.proactive.core.component.controller;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class MonitorControllerHelper {
    private static final String KEY_INFO_SEPARATOR = "-";

    /**
     * Generate an unique key according to the name of the server interface, the name of the method
     * and the class names of the parameters of the method.
     *
     * @param itfName Name of the server interface where the method is exposed.
     * @param methodName Name of the method.
     * @param parametersTypes Types of the parameters of the method.
     * @return Key built like this itfName-MethodName-ClassNameParam1-ClassNameParam2-...
     */
    public static String generateKey(String itfName, String methodName, Class<?>[] parametersTypes) {
        String key = itfName + KEY_INFO_SEPARATOR + methodName;

        for (int i = 0; i < parametersTypes.length; i++) {
            key += KEY_INFO_SEPARATOR + parametersTypes[i].getName();
        }

        return key;
    }
}
