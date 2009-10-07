package functionalTests.activeobject.request.tags;

import java.io.Serializable;
import java.util.Random;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMessageTagging;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.body.tags.Tag;


public class B implements Serializable, InitActive {

    public B() {
    }

    public void initActivity(Body body) {
        body.setImmediateService("getResult");
    }

    public void runActivity(Body body) {

        Service service = new Service(body);
        service.fifoServing();
    }

    public int getNumber() {
        return new Random().nextInt(42);
    }

    public int getResult() {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        Object data = tags.getData("TEST_TAGS_00");
        int result = 0;
        if (data instanceof Integer) {
            result = (Integer) data;
        }
        return result;
    }

    public boolean checkTag(String tagID) {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        return (tags.getTag(tagID) == null);
    }

    public boolean checkNoLocalMemory() {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        return tags.getTag("TEST_TAGS_02").getLocalMemory() == null;
    }

    public boolean localMemoryLeaseExceeded() {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        Tag t = tags.addTag(new Tag("TEST_TAGS_03-B") {
            public Tag apply() {
                return this;
            }
        });
        t.createLocalMemory(7).put("MT_08", new Integer(0));
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return t.getLocalMemory() == null;
    }

    public void exit() throws Exception {
        PAActiveObject.terminateActiveObject(true);
    }

}
