package org.objectweb.proactive.extensions.processbuilder;

/**
 * Factory class for {@link OSProcessBuilder} which will produce a process
 * builder that is compatible with the underlying operating system.
 * 
 * @author Zsolt Istvan
 * 
 */
public class OSProcessBuilderFactory {
    private static final String OS_WINDOWS = "win";
    private static final String OS_LINUX = "linux";

    /**
     * Creates a new instance of {@link OSProcessBuilder} based on the operating
     * system.<br>
     * 
     * @return instance of {@link OSProcessBuilder}
     */
    public static OSProcessBuilder getBuilder() {
        return getBuilder(System.getProperty("os.name"));
    }

    /**
     * Internal method for choosing an implementation of
     * {@link OSProcessBuilder}.<br>
     * The decision is based on the name given as argument.
     * 
     * @param osName
     * @return
     */
    private static OSProcessBuilder getBuilder(String osName) {
        String lcOsName = osName.toLowerCase();

        // will not test for equality because versions may not be important
        if (lcOsName.startsWith(OS_LINUX)) {
            return new LinuxProcessBuilder();

        } else if (lcOsName.startsWith(OS_WINDOWS)) {
            return new WindowsProcessBuilder();

        } else {
            return new BasicProcessBuilder();
        }
    }

}
