package functionalTests.activeobject.internalclass;

public class RemoteAgent {

    public int doCallOnMemberClassInstance(TestInternalClassAO.MemberClass target) {
        return target.incrementEnclosingPrivateValue();
    }

}
