package org.objectweb.proactive.examples.documentation.jmx.mbeans;

import java.io.Serializable;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;


public class Hello extends NotificationBroadcasterSupport implements HelloMBean, Serializable {
    public static final String NOTIFICATION_NAME = "HelloNotification";
    public long counter = 0;

    private String message = null;

    public Hello() {
        message = "Hello there";
    }

    public Hello(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void sayHello() {
        System.out.println(message);
    }

    public void saySomething() {
        System.out.println("Something");
    }

    public String concat(String str1, String str2) {

        Notification notification = new Notification(Hello.NOTIFICATION_NAME, this, this.counter++);
        notification.setUserData(this);
        sendNotification(notification);
        return str1 + str2;
    }
}
