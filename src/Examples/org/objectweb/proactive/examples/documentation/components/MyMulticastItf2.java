package org.objectweb.proactive.examples.documentation.components;

//@snippet-start component_userguide_7
import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.Reduce;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceMode;
import org.objectweb.proactive.examples.documentation.classes.T;

import org.objectweb.proactive.examples.documentation.components.GetLastReduction;


public interface MyMulticastItf2 {

    //@snippet-break component_userguide_7
    //@snippet-start component_userguide_6
    public List<T> foo();

    public void bar();

    //@snippet-end component_userguide_6

    //@snippet-start component_userguide_8
    @Reduce(reductionMode = ReduceMode.CUSTOM, customReductionMode = GetLastReduction.class)
    public T foobar();

    //@snippet-end component_userguide_8

    //@snippet-resume component_userguide_7
    @Reduce(reductionMode = ReduceMode.SELECT_UNIQUE_VALUE)
    public T baz();
}
//@snippet-end component_userguide_7
