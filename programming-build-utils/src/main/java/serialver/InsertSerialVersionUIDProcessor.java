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
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;


/**
 * Processor inserting serialVersionUID field with given value in 
 * all Serializable classes. If Serializable class doesn't declare serialVersionUID field then this field is added,
 * if field already exists then processor modifies field initialization expression.
 * <p/>
 * Note: value of serialVersionUID should be specified as system property 'serialver.insert.serialVersionUID'. 
 * 
 * @author ProActive team
 *
 */
public class InsertSerialVersionUIDProcessor extends BaseSerialVersionUIDProcessor {

    private static final String INSERTED_VERSION_PROPERTY = "serialver.insert.serialVersionUID";

    private String expressionString;

    private int modifiedClassesCount;

    @Override
    public void init() {
        resetState();
    }

    private void resetState() {
        expressionString = System.getProperty(INSERTED_VERSION_PROPERTY);
        if (expressionString == null || expressionString.trim().length() == 0) {
            String message = "System property '" + INSERTED_VERSION_PROPERTY + "' isn't set";
            System.out.println("serialVersionUID error: " + message);
            throw new IllegalArgumentException(message);
        }
        modifiedClassesCount = 0;
    }

    @Override
    public void processingDone() {
        System.out.println(String.format("serialVersionUID: Processing is completed, %d classes were modified",
                modifiedClassesCount));
    }

    @Override
    public void process(CtClass<?> klass) {
        if (!(isSerializable(klass))) {
            return;
        }

        modifiedClassesCount++;

        CompilationUnit compilationUnit = klass.getPosition().getCompilationUnit();
        CtField<?> field = klass.getField(SERIAL_VERSION_UID_FIELD_NAME);
        if (field == null) {
            System.out.println(String.format("serialVersionUID: Inserting \"%s\" in the \"%s\"", SERIAL_VERSION_UID_FIELD_NAME,
                    klass.getQualifiedName()));

            SourceCodeFragment fragment = new SourceCodeFragment();
            fragment.position = findStartOfClassDeclaration(klass);
            fragment.code = String.format("\n\n    private static final long %s = %s;",
                    SERIAL_VERSION_UID_FIELD_NAME, expressionString);
            compilationUnit.addSourceCodeFragment(fragment);
        } else {
            System.out.println(String.format("serialVersionUID: Modifying \"%s\" in the \"%s\"", SERIAL_VERSION_UID_FIELD_NAME,
                    klass.getQualifiedName()));

            CtExpression<?> expression = field.getDefaultExpression();
            if (expression != null) {
                SourcePosition expressionPosition = expression.getPosition();
                SourceCodeFragment fragment = new SourceCodeFragment();
                fragment.position = expressionPosition.getSourceStart();
                fragment.replacementLength = expressionPosition.getSourceEnd() -
                    expressionPosition.getSourceStart() + 1;
                fragment.code = expressionString;
                compilationUnit.addSourceCodeFragment(fragment);
            } else {
                SourcePosition fieldPosition = field.getPosition();
                SourceCodeFragment fragment = new SourceCodeFragment();
                fragment.position = fieldPosition.getSourceEnd() + 1;
                fragment.code = String.format(" = %s", expressionString);
                compilationUnit.addSourceCodeFragment(fragment);
            }
        }
    }

    private int findStartOfClassDeclaration(CtClass<?> klass) {
        String sourceCode = klass.getPosition().getCompilationUnit().getOriginalSourceCode();
        // this is position of the end of the class name, search first '{' after it
        int position = klass.getPosition().getSourceEnd();
        while (position < sourceCode.length() && sourceCode.charAt(position) != '{') {
            position++;
        }
        return position + 1;
    }

}
