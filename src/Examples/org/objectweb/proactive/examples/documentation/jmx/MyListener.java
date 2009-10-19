package org.objectweb.proactive.examples.documentation.jmx;

//@snippet-start jmx_MyListener
import javax.management.Notification;
import javax.management.NotificationListener;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;


public class MyListener implements NotificationListener {

    public void handleNotification(Notification notification, Object handback) {
        // Get the type of the notification
        String type = notification.getType();
        // Get the data of the notification
        Object data = notification.getUserData();

        if (type.equals(NotificationType.bodyCreated)) {
            BodyNotificationData notificationData = (BodyNotificationData) data;
            UniqueID id = notificationData.getId();
            System.out.println("Active Object created with id:" + id);
        }
    }
}
//@snippet-end jmx_MyListener