package org.objectweb.proactive.extensions.processbuilder;

/**
 * Factory class for {@link OSProcessBuilder} which will produce a process
 * builder that is compatible with the underlying operating system.
 * 
 * @author Zsolt Istvan
 * 
 */
public interface OSProcessBuilderFactory {
    /**
     * Creates a new instance of {@link OSProcessBuilder}
     */
    public OSProcessBuilder getBuilder();
}
