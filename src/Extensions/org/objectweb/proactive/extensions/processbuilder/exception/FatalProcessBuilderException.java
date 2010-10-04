package org.objectweb.proactive.extensions.processbuilder.exception;

import java.io.Serializable;


/**
 * This exception is used to signal an internal error encountered by the OSProcessBuilder while
 * preparing for the command to run. By internal we mean a problem related to the scripts and
 * other resources used by the process builder.
 * <p>
 * This error comes in several flavors:
 * <ul>
 *  <li>Lack of output from scripts - This error can have different causes on different 
 *  operating systems, however it will usually be related to stream redirection, or piping
 *  issues.
 *  </li>
 *  <li>Corruption of scripts - Which means that the scripts are not respecting the messaging
 *  interface. Typos and version mismatches between scripts and class files can cause this failure.
 *  </li>
 *  <li>Inability to launch - Other reasons</li>
 * </ul>
 * </p>
 * @author Zsolt Istvan
 * 
 */
public class FatalProcessBuilderException extends Exception implements Serializable {

    public FatalProcessBuilderException(String descr) {
        super(descr);
    }

    public FatalProcessBuilderException(String descr, Throwable cause) {
        super(descr, cause);
    }

}
