package org.objectweb.proactive.extensions.processbuilder.exception;

import java.io.Serializable;


/**
 * This exception is thrown if the process builder's scripts were not able to bind the 
 * to-be-launched process to the given subset of the cores.
 * @author Zsolt Istvan
 * 
 */
public class CoreBindingException extends Exception implements Serializable {

    public CoreBindingException(String descr) {
        super(descr);
    }

}
