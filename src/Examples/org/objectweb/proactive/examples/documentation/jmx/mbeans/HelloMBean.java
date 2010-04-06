package org.objectweb.proactive.examples.documentation.jmx.mbeans;

public interface HelloMBean {

    public void setMessage(String message);

    public String getMessage();

    public void sayHello();

    public void saySomething();

    public String concat(String str1, String str2);
}
