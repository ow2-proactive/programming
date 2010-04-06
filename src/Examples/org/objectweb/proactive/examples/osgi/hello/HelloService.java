package org.objectweb.proactive.examples.osgi.hello;

import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;


public interface HelloService {

    public void sayHello();

    public void saySomething(String something);
}
