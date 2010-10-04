package org.objectweb.proactive.extensions.processbuilder;

/**
 * This class represents a user of the operating system, that is identified by one
 * of the following options:
 * <ol>
 *  <li>username</li>
 *  <li>username and password</li>
 *  <li>username and private key</li>
 * </ol>
 * 
 * @author zistan
 * 
 */
public class OSUser {
    private final String userName;
    private final String password;
    private final char[] privateKey;

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
     * @param password
     */
    public OSUser(String userName, char[] keyContent) {
        this.userName = userName;

        // empty key is still no key
        if (!keyContent.equals("")) {
            this.privateKey = keyContent;
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
    protected char[] getPrivateKey() {
        return privateKey;
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