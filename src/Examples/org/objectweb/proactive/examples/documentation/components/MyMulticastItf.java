package org.objectweb.proactive.examples.documentation.components;

//@snippet-start component_userguide_2
//@snippet-start component_userguide_3
import java.util.List;
import org.objectweb.proactive.examples.documentation.classes.T;

//@snippet-break component_userguide_3
import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;

//@snippet-resume component_userguide_3
//@snippet-break component_userguide_2
import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;

//@snippet-resume component_userguide_2
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

//@snippet-break component_userguide_2
//@snippet-break component_userguide_3
import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.group.DispatchMode;


//@snippet-resume component_userguide_3
//@snippet-resume component_userguide_2

//@snippet-break component_userguide_3
@ClassDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
//@snippet-resume component_userguide_3
interface MyMulticastItf {

    //@snippet-break component_userguide_2
    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
    //@snippet-start component_userguide_4
    //@snippet-break component_userguide_3
    @Dispatch(mode = DispatchMode.DYNAMIC, bufferSize = 1024)
    //@snippet-resume component_userguide_2
    //@snippet-resume component_userguide_3
    public void foo(List<T> parameters);

    //@snippet-end component_userguide_4

    //@snippet-break component_userguide_2
    //@snippet-break component_userguide_3
    //@snippet-start component_userguide_5
    public void bar(@ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
    List<T> parameters);
    //@snippet-end component_userguide_5
    //@snippet-resume component_userguide_2
    //@snippet-resume component_userguide_3

}
//@snippet-end component_userguide_2
//@snippet-end component_userguide_3