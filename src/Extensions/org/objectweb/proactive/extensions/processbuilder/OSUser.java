/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.processbuilder;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class represents a user of the operating system, that is identified by one
 * of the following options:
 * <ol>
 *  <li>username</li>
 *  <li>username and password</li>
 *  <li>username and private key</li>
 * </ol>
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
@PublicAPI
public class OSUser {
    private final String userName;
    private final String password;
    private final byte[] privateKey;
    // Windows domain name, optional
    private String domain;

    /**
     * Constructor for a user which has no password specified. (This does not
     * necessarily mean that the OS user does not have a password)
     * 
     * @param userName
     */
    public OSUser(String userName) {
        this.userName = userName;
        this.password = null;
        this.privateKey = null;
    }

    /**
     * Constructor for a user with password.
     * 
     * @param userName
     * @param password
     */
    public OSUser(String userName, String password) {
        this.userName = userName;

        // empty password is still no password
        if (!password.equals("")) {
            this.password = password;
        } else {
            this.password = null;
        }

        this.privateKey = null;
    }

    /**
     * Constructor for a user with private key;
     * 
     * @param userName
     * @param privateKey a SSH private key as byte array (String encoded with the default charset)
     */
    public OSUser(String userName, byte[] privateKey) {
        this.userName = userName;

        // empty key is still no key
        if (!privateKey.equals("")) {
            this.privateKey = privateKey;
        } else {
            this.privateKey = null;
        }

        this.password = null;
    }

    /**
     * Returns the user-name
     * 
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the stored password.
     * 
     * @return Plain-text password, or <i>null</i> in case no password was
     *         specified.
     */
    /*
     * This method is protected as no external classes should be able to have access to it.
     */
    protected String getPassword() {
        return password;
    }

    /**
     * This method returns true if a user's password is set.
     * 
     * @return
     */
    public boolean hasPassword() {
        return (password != null);
    }

    /**
     * Returns the stored key contents.
     * 
     * @return Content of the private key, or <i>null</i> in case no key was
     *         specified.
     */
    /*
     * This method is protected as no external classes should be able to have access to it.
     */
    protected byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the Windows domain name associated to this user, null if any
     * @return the Windows domain name associated to this user, null if any
     * @since ProActive 5.0.1
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set a Windows domain name for the user.
     * @param domain the domain to set
     * @since ProActive 5.0.1
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /** Returns true if a Windows domain is specified for this user.
     * @return true if a Windows domain is specified for this user.
     * @since ProActive 5.0.1
     */
    public boolean hasDomain() {
        return (this.domain != null);
    }

    /**
     * This method returns true if a user's private key is set.
     * 
     * @return
     */
    public boolean hasPrivateKey() {
        return (privateKey != null);
    }

}