package org.objectweb.proactive.extensions.processbuilder.exception;

import java.io.Serializable;


/**
 * If this exception is throwed by the OSProcessBuilder, 
 * than it was from one of the following reasons:
 * <ul>
 *  <li>User name is incorrect</li>
 *  <li>Password is incorrect</li>
 *  <li>The OSProcessBuilder's internal launching mechanism fails
 *  under the specific user ID - access rights to scripts folder</li>
 * </ul>
 * @author Zsolt Istvan
 * 
 */
public class OSUserException extends Exception implements Serializable {

    public OSUserException(String message) {
        super(message);
    }
}
