/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.extensions.annotation.activeobject;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

import org.objectweb.proactive.extensions.annotation.MigrationSignal;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;


/**
 * <p>This class implements a visitor for the ProActiveProcessor, according to the Pluggable Annotation Processing API(jsr269) specification</p>
 * <p> It verifies whether a class declaration annotated with {@link org.objectweb.proactive.extensions.annotation.ActiveObject}</p>
 * <ul>
 *	<li>
 * has no methods that return null.
 * This is because null cannot be checked on the caller-side - the caller will have a reference to a future, which most probably will not be null.
 *  </li>
 * </ul>
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */
public class ActiveObjectVisitorCTree extends TreePathScanner<Void, Trees> {

    private Messager compilerOutput;
    private boolean insideClass = false;
    private TreePath curMethod;
    private boolean methodReturnsNull = false;

    public ActiveObjectVisitorCTree(ProcessingEnvironment procEnv) {
        compilerOutput = procEnv.getMessager();
    }

    @Override
    public Void visitClass(ClassTree clazzTree, Trees trees) {

        // ignore all internal classes
        if (!insideClass) {
            insideClass = true;
        } else {
            return null;
        }

        testClassModifiers(clazzTree, trees);
        testClassConstructors(clazzTree, trees);

        // we have to do something in order not to visit the inner classes twice
        Void ret = null;
        List<? extends Tree> clazzMembers = clazzTree.getMembers();
        for (Tree clazzMember : clazzMembers) {
            // it's not clear how to visit class fields
            // so do it from here
            // TODO change it!
            if (clazzMember.getKind().equals(Kind.VARIABLE)) {
                VariableTree fieldNode = (VariableTree) clazzMember;

                if (accessedFromOutside(fieldNode) &&
                    !fieldNode.getModifiers().getFlags().contains(Modifier.FINAL) &&
                    !hasAccessors(fieldNode.getName().toString(), clazzMembers)) {
                    reportWarning("The class declares the public field " + fieldNode.getName() + ".\n" +
                        ErrorMessages.NO_GETTERS_SETTERS_ERROR_MESSAGE, trees.getElement(getCurrentPath()));
                }

            } else {

                ret = scan(clazzMember, trees);

            }
        }

        insideClass = false;
        return ret;
    }

    private final boolean accessedFromOutside(VariableTree fieldNode) {

        return !fieldNode.getModifiers().getFlags().contains(Modifier.PROTECTED) &&
            !fieldNode.getModifiers().getFlags().contains(Modifier.PRIVATE);

    }

    private boolean hasAccessors(String fieldName, List<? extends Tree> clazzMembers) {
        boolean hasSetter = false;
        boolean hasGetter = false;
        String getterName = GenerateGettersSetters.getterPattern(fieldName);
        String setterName = GenerateGettersSetters.setterPattern(fieldName);

        for (Tree member : clazzMembers) {
            if (member.getKind().equals(Kind.METHOD)) {
                if (((MethodTree) member).getName().toString().matches(getterName)) {
                    hasGetter = true;
                }
                if (((MethodTree) member).getName().toString().matches(setterName)) {
                    hasSetter = true;
                }

                if (hasGetter && hasSetter) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Void visitMethod(MethodTree methodNode, Trees trees) {

        curMethod = getCurrentPath();

        // test modifiers
        if (methodNode.getModifiers().getFlags().contains(Modifier.FINAL) &&
            !methodNode.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            reportError(" The class declares the final method " + methodNode.getName() + ".\n" +
                ErrorMessages.HAS_FINAL_METHOD_ERROR_MESSAGE, trees.getElement(getCurrentPath()));
        }

        // test serializable args
        Element methodElement = trees.getElement(getCurrentPath());
        if (methodElement instanceof ExecutableElement) {
            ExecutableElement methodElem = (ExecutableElement) methodElement;

            // a migration signal can have non-serializable parameters - for instance, the ProActive Node!
            if (methodElem.getAnnotation(MigrationSignal.class) != null) {
                return super.visitMethod(methodNode, trees);
            }
        }

        methodReturnsNull = false;
        return super.visitMethod(methodNode, trees);
    }

    /*
     * Test for a MethodTree to see if it is a constructor or not
     */
    private final boolean isConstructor(MethodTree executable) {
        // a constructor is a method that has returns nothing
        return executable.getReturnType() == null;
    }

    @Override
    public Void visitReturn(ReturnTree returnNode, Trees trees) {

        if (methodReturnsNull == true) {
            // already reported the error. carry on!
            return super.visitReturn(returnNode, trees);
        }

        ExpressionTree returnExpression = returnNode.getExpression();

        if (returnExpression == null) {
            // no return value. good.
            return super.visitReturn(returnNode, trees);
        }

        if (returnExpression.getKind().equals(Tree.Kind.NULL_LITERAL)) {
            // for private methods it's ok to return null

            MethodTree method = (MethodTree) trees.getTree(trees.getElement(curMethod));
            if (!method.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                reportWarning(ErrorMessages.NO_NULL_RETURN_ERROR_MSG, trees.getElement(curMethod));
                // mark that we've reported the error for this method
                methodReturnsNull = true;
            }
        }
        return super.visitReturn(returnNode, trees);
    }

    private void testClassModifiers(ClassTree clazzTree, Trees trees) {
        ModifiersTree modifiers = clazzTree.getModifiers();

        boolean isPublic = false;
        for (Modifier modifier : modifiers.getFlags()) {
            if (modifier.equals(Modifier.FINAL)) {

                reportError(ErrorMessages.IS_FINAL_ERROR_MESSAGE, trees.getElement(getCurrentPath()));
            }

            if (modifier.equals(Modifier.PUBLIC)) {
                isPublic = true;
            }
        }

        if (!isPublic) {
            reportError(ErrorMessages.IS_NOT_PUBLIC_ERROR_MESSAGE, trees.getElement(getCurrentPath()));
        }
    }

    /**
     *
     * Checks that an empty no-argument constructor is defined
     *
     */
    private void testClassConstructors(ClassTree clazzTree, Trees trees) {

        boolean hasNonArgsPublicConstructor = false;
        for (Tree member : clazzTree.getMembers()) {
            if (member.getKind().equals(Kind.METHOD)) {
                MethodTree constructor = (MethodTree) member;
                if (isConstructor(constructor)) {
                    // it is constructor
                    if (constructor.getParameters().size() == 0) {
                        hasNonArgsPublicConstructor = true;

                        if (constructor.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                            reportError(ErrorMessages.NO_NOARG_CONSTRUCTOR_CANNOT_BE_PRIVATE_MESSAGE, trees
                                    .getElement(getCurrentPath()));
                            return;
                        }

                        if (constructor.getBody().getStatements().size() > 0) {

                            // process gracefully "super" statement in the
                            // constructor
                            boolean onlySuperInside = false;
                            if (constructor.getBody().getStatements().size() == 1) {
                                StatementTree statement = constructor.getBody().getStatements().get(0);

                                // TODO check it using Compiler Tree API
                                if (statement.toString().startsWith("super")) {
                                    onlySuperInside = true;
                                }
                            }

                            if (!onlySuperInside) {
                                reportWarning(ErrorMessages.EMPTY_CONSTRUCTOR, trees
                                        .getElement(getCurrentPath()));
                            }
                        }

                    }
                }
            }
        }

        if (!hasNonArgsPublicConstructor) {
            reportError(ErrorMessages.NO_NOARG_CONSTRUCTOR_ERROR_MESSAGE, trees.getElement(getCurrentPath()));
        }

    }

    protected void reportError(String msg, Element element) {
        compilerOutput.printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

    protected void reportWarning(String msg, Element element) {
        compilerOutput.printMessage(Diagnostic.Kind.WARNING, msg, element);
    }

}
