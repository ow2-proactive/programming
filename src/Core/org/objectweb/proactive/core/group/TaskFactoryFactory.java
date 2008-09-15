package org.objectweb.proactive.core.group;

/**
 * A factory for task factories. 
 * 
 * Indeed, with the default group TaskFactory, groups dispatch parameters in configurable non-broadcast modes if and only if: 
 * they are instances of groups, and group parameters are tagged as "scatter".
 * 
 * @author Matthieu Morel
 *
 */
public class TaskFactoryFactory {

    public static TaskFactory getTaskFactory(ProxyForGroup<?> groupProxy) {
        return new BasicTaskFactory(groupProxy);
    }

}
