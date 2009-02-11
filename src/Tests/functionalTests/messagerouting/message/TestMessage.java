package functionalTests.messagerouting.message;

import org.junit.Test;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;

import functionalTests.FunctionalTest;


public class TestMessage extends FunctionalTest {

    @Test
    public void testOffset() {
        for (Message.Field field : Message.Field.values()) {
            System.out.println(field.ordinal());
            System.out.println(field.getLength());
            System.out.println(field.getOffset());
        }

        System.out.println(Message.Field.getTotalOffset());
    }
}
