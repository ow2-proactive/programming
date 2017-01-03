package org.objectweb.proactive.extensions.processbuilder;

import org.junit.BeforeClass;

import static org.junit.Assume.assumeTrue;

public class ProcessBuilderTest {
    public static final String PROCESSBUILDER_USERNAME_PROPNAME = "runasme.user";
    public static final String PROCESSBUILDER_PASSWORD_PROPNAME = "runasme.pwd";
    protected static String username;
    protected static String password;

    @BeforeClass
    public static void beforeClass() {
        username = System.getProperty(PROCESSBUILDER_USERNAME_PROPNAME);
        assumeTrue(username != null);
        password = System.getProperty(PROCESSBUILDER_PASSWORD_PROPNAME);
        assumeTrue(password != null);
    }
}
