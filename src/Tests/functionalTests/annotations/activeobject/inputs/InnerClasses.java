package functionalTests.annotations.activeobject.inputs;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class InnerClasses {

    protected javax.swing.JSplitPane verticalSplitPane;

    public InnerClasses() {
    }

    // inner class
    class Dada {
    }

    // inner class
    // ERROR
    @ActiveObject
    class AnnotatedDada {
    }

    public void localInnerClass() {
        // local inner class
        class InnerClass {
        }

        // local inner class
        // ERROR - unfortunately cannot be checked!
        @ActiveObject
        class AnnotatedInnerClass {
        }
    }

    // anonymous inner class
    public InnerClasses(String name, Integer width, Integer height) {
        verticalSplitPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
            }
        });
    }
}
