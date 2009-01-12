/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
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
