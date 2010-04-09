package functionalTests.configuration;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;


public class TestRegisterAProperty {

    @Test
    public void test() {
        PAProperties.getAllProperties();

        PAProperties.register(MyRepository.class);

        Map<Class<?>, List<PAProperty>> map = PAProperties.getAllProperties();

        List<PAProperty> list = map.get(MyRepository.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
    }

    static class MyRepository {
        static PAPropertyBoolean myBool = new PAPropertyBoolean("myBool", false);
        static public PAPropertyString myString = new PAPropertyString("myString", false);
        static public PAPropertyInteger myInt = new PAPropertyInteger("myInt", false);
        PAPropertyBoolean nonStatic = new PAPropertyBoolean("nonStatic", false);
    }
}
