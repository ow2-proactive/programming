package org.objectweb.proactive.examples.documentation.exceptions;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.examples.documentation.classes.A;


/**
 * @author The ProActive Team
 *
 */
public class ExceptionHandling {

    public static void main(String[] args) {
        //@snippet-start basic_exception
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        try {
            A a = (A) PAActiveObject.newActive(A.class.getName(), null);
            a.throwsException(true); //Synchronous method due to the potential exception
            System.out.println("Hello");
            //...
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        //@snippet-end basic_exception

        //@snippet-start tryWithCatch_exception
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = (A) PAActiveObject.newActive(A.class.getName(), null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            System.out.println("Hello");
            //...
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        //@snippet-end tryWithCatch_exception

        //@snippet-start tryWithCatch_throwArrivedException
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = (A) PAActiveObject.newActive(A.class.getName(), null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            //...
            // Throws exceptions which has been already raised by active object
            PAException.throwArrivedException();

            // Should appear since the exception did not have the time
            // to be thrown (due to the sleep(5000))
            System.out.println("Hello");
            //...
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Throws exceptions which has been already raised by active object
            PAException.throwArrivedException();

            // Should not appear since the exception had the time to
            // be thrown.
            System.out.println("Hello");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        //@snippet-end tryWithCatch_throwArrivedException

        //@snippet-start tryWithCatch_waitForPotentialException
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        PAException.tryWithCatch(Exception.class);
        try {
            A a = (A) PAActiveObject.newActive(A.class.getName(), null);
            a.throwsException(true); // Asynchronous method call that can throw an exception
            //...
            // At that moment, we want to be sure that no exception has been
            // raised by the code before.
            PAException.waitForPotentialException();
            //...
            // Should not appear since waitForPotentialException
            // is blocking.
            System.out.println("Hello");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        //@snippet-end tryWithCatch_waitForPotentialException

    }
}
