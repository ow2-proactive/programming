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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.annotation.migration.strategy;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import org.objectweb.proactive.extensions.annotation.OnDeparture;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;


/**
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class OnDepartureVisitorCTree extends TreePathScanner<Void, Trees> {
    // error messages
    protected String ERROR_PREFIX_STATIC = " is annotated using the " + OnDeparture.class.getSimpleName() +
        " annotation.\n";

    protected final String ERROR_SUFFIX = "\nPlease refer to the ProActive manual for further help on implementing migration strategies.\n";

    protected transient String ERROR_PREFIX;

    private Messager compilerOutput;

    public OnDepartureVisitorCTree(ProcessingEnvironment procEnv) {
        compilerOutput = procEnv.getMessager();
    }

    @Override
    public Void visitMethod(MethodTree methodNode, Trees trees) {

        ERROR_PREFIX = methodNode.getName() + ERROR_PREFIX_STATIC;

        if (!returnsVoid(methodNode))
            reportError("the method shouldn't have any return value," + " but instead returns " +
                methodNode.getReturnType().toString(), trees.getElement(getCurrentPath()));

        if (!methodNode.getParameters().isEmpty())
            reportError("the method accepts parameters", trees.getElement(getCurrentPath()));

        return super.visitMethod(methodNode, trees);
    }

    private boolean returnsVoid(MethodTree methodNode) {
        if (methodNode.getReturnType().getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
            PrimitiveTypeTree retType = (PrimitiveTypeTree) methodNode.getReturnType();
            return retType.getPrimitiveTypeKind().equals(TypeKind.VOID);
        }
        return false;
    }

    protected void reportError(String msg, Element element) {
        compilerOutput.printMessage(Diagnostic.Kind.ERROR, "[ERROR]" + ERROR_PREFIX +
            ErrorMessages.INVALID_MIGRATION_STRATEGY_METHOD + ": " + msg + ERROR_SUFFIX, element);
    }

    protected void reportWarning(String msg, Element element) {
        compilerOutput.printMessage(Diagnostic.Kind.WARNING, "[ERROR]" + ERROR_PREFIX +
            ErrorMessages.INVALID_MIGRATION_STRATEGY_METHOD + ": " + msg + ERROR_SUFFIX, element);
    }
}
