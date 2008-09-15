package org.objectweb.proactive.core.component.group;

import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.group.TaskFactory;
import org.objectweb.proactive.core.group.TaskFactoryFactory;


/**
 * A factory for component task factories. 
 * 
 * Indeed, groups dispatch parameters in configurable non-broadcast modes if and only if: 
 * they are instances of groups, and group parameters are tagged as "scatter" ( @link org.objectweb.proactive.core.group.TaskFactoryFactory ). 
 *
 * On the contrary, the component framework simply interprets annotations on the signatures of
 * classes / methods / arguments.
 * 
 * Hence the two distinct factories.
 * 
 * @author Matthieu Morel
 *
 */
public class ComponentTaskFactoryFactory {

    public static TaskFactory getTaskFactory(ProxyForGroup<?> groupProxy) {
        if (groupProxy instanceof ProxyForComponentInterfaceGroup) {
            return new CollectiveItfsTaskFactory(groupProxy);
        } else {
            return TaskFactoryFactory.getTaskFactory(groupProxy);
        }
    }

}
