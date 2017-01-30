/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.annotation.callbacks.isready;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;


public class VirtualNodeIsReadyCallbackVisitorCTree extends TreePathScanner<Void, Trees> {

    private Messager compilerOutput;

    public VirtualNodeIsReadyCallbackVisitorCTree(ProcessingEnvironment procEnv) {
        compilerOutput = procEnv.getMessager();
    }

    @Override
    public Void visitMethod(MethodTree methodNode, Trees trees) {

        boolean correctSignature = false;

        if (returnsVoid(methodNode) && methodNode.getParameters().size() == 1) {
            if (methodNode.getParameters().get(0).getType().toString().equals(String.class.getSimpleName())) {
                correctSignature = true;
            }
        }

        if (!correctSignature) {
            compilerOutput.printMessage(Diagnostic.Kind.ERROR,
                                        ErrorMessages.INCORRECT_METHOD_SIGNATURE_FOR_ISREADY_CALLBACK,
                                        trees.getElement(getCurrentPath()));
        }

        return super.visitMethod(methodNode, trees);
    }

    private boolean returnsVoid(MethodTree methodNode) {
        if (methodNode.getReturnType().getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
            PrimitiveTypeTree retType = (PrimitiveTypeTree) methodNode.getReturnType();
            return retType.getPrimitiveTypeKind().equals(TypeKind.VOID);
        }
        return false;
    }

}
