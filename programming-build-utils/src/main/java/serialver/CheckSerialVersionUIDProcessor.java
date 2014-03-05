/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package serialver;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;


/**
 * Processor checking that all Serializable classes declare serialVersionUID
 * fields and value of serialVersionUID is the same for all Serializable classes.  
 * <p/>
 * Check fails in following cases:
 * <ul>
 * <li>Serializable class doesn't declare serialVersionUID field 
 * <li>Serializable class declares serialVersionUID field without default value
 * <li>there are Serializable classes declaring different expressions for serialVersionUID initialization  
 * </ul>
 * 
 * @author ProActive team
 *
 */
public class CheckSerialVersionUIDProcessor extends BaseSerialVersionUIDProcessor {

    private CtField<?> firstUidField;

    private int classCounter;

    private boolean failed;

    @Override
    public void init() {
        resetState();
    }

    private void resetState() {
        firstUidField = null;
        failed = false;
        classCounter = 0;
    }

    @Override
    public void process(CtClass<?> klass) {
        if (!(isSerializable(klass))) {
            return;
        }
        classCounter++;

        CtField<?> field = klass.getField(SERIAL_VERSION_UID_FIELD_NAME);
        if (field == null) {
            foundError(String.format("Serializable class \"%s\" must declare field \"%s\"",
                    klass.getQualifiedName(), SERIAL_VERSION_UID_FIELD_NAME));
            return;
        }

        CtExpression<?> expression = field.getDefaultExpression();
        if (expression == null) {
            foundError(String.format("Serializable class \"%s\" doesn\'t declare value for the field \"%s\"",
                    klass.getQualifiedName(), SERIAL_VERSION_UID_FIELD_NAME));
            return;
        }

        if (firstUidField == null) {
            firstUidField = field;
        } else {
            if (firstUidField.getDefaultExpression().compareTo(expression) != 0) {
                foundError(String
                        .format("Serializable classes \"%s\" and \"%s\" declare different expressions for the \"%s\": \"%s\" and \"%s\"",
                                firstUidField.getDeclaringType().getQualifiedName(),
                                klass.getQualifiedName(), SERIAL_VERSION_UID_FIELD_NAME, firstUidField
                                        .getDefaultExpression().toString(), expression.toString()));
            }
        }
    }

    @Override
    public void processingDone() {
        if (failed) {
            System.out.println("serialVersionUID error: check for 'serialVersionUID' failed");
            throw new RuntimeException(
                "Check for 'serialVersionUID' failed, see log messages for more details");
        } else {
            if (classCounter == 0) {
                System.out.println("serialVersionUID warning: Check completed, no Serializable classes were found");
            } else {
                System.out.println(String.format("serialVersionUID: Check is ok, found %d classes with %s = %s", classCounter,
                        SERIAL_VERSION_UID_FIELD_NAME, firstUidField.getDefaultExpression().toString()));
            }
        }
    }

    private void foundError(String message) {
        failed = true;
        System.out.println("serialVersionUID error: " + message);
    }

}
