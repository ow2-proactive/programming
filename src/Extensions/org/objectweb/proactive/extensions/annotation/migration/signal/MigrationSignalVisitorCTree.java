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
package org.objectweb.proactive.extensions.annotation.migration.signal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;


/**
 * <p>This class implements a visitor for the ProActiveProcessor, according to the Pluggable Annotation Processing API(jsr269) specification</p>
 * <p>It implements the checks for the
 * {@link org.objectweb.proactive.annotation.migration.MigratableSignal}
 * annotation. See the annotation javadoc for the description of the checks performed.
 * </p>
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */
public class MigrationSignalVisitorCTree extends TreePathScanner<Void, Trees> {

    // error messages
    protected String ERROR_PREFIX_STATIC = " method is annotated using the " +
        MigrationSignal.class.getSimpleName() + " annotation.\n";
    protected final String ERROR_SUFFIX = "Please refer to the ProActive manual for further help on creating Active Objects.\n";
    protected String ERROR_PREFIX;

    // where we should signal the errors
    private final Messager _compilerOutput;
    private final Elements _elemUtils;

    public MigrationSignalVisitorCTree(ProcessingEnvironment procEnv) {
        _compilerOutput = procEnv.getMessager();
        _elemUtils = procEnv.getElementUtils();
    }

    // marks the position of the method declaration inside the source code
    // also a marker for the moment of finding a method declaration
    private Element _methodPosition = null;
    private ClassTree _containingClass;
    private CompilationUnitTree _containingCompilationUnit;
    private MethodTree _containingMethod;

    private Map<BlockTree, Boolean> visitedBlocks = new HashMap<BlockTree, Boolean>();

    @Override
    public Void visitMethod(MethodTree methodNode, Trees trees) {

        ERROR_PREFIX = methodNode.getName() + ERROR_PREFIX_STATIC;
        _methodPosition = trees.getElement(getCurrentPath());

        Element clazzElement = trees.getElement(getCurrentPath().getParentPath());
        if (!((clazzElement instanceof TypeElement) && (clazzElement.getKind().isClass()))) {
            // actually, this will be caught by javac. but nevertheless, we can also have a say...
            _compilerOutput.printMessage(Diagnostic.Kind.ERROR,
                    "Found a method declaration outside a class declaration. Interesting!", _methodPosition);
            return super.visitMethod(methodNode, trees);
        }

        _containingCompilationUnit = getCurrentPath().getCompilationUnit();
        _containingClass = (ClassTree) trees.getTree(clazzElement);
        _containingMethod = methodNode;

        // the method must be within a class that is tagged with the ActiveObject annotation
        // sometimes, @MigrationSignal annotation can make sense outside an active object. See
        // examples/chat/ChatGUI.java/MigrateAction.actionPerfomed()
        //        if (clazzElement.getAnnotation(ActiveObject.class) == null) {
        //            reportError(_methodPosition, ErrorMessages.NOT_IN_ACTIVE_OBJECT_ERROR_MESSAGE);
        //            return super.visitMethod(methodNode, trees);
        //        }

        // the method must be public
        //		if( !testModifiers(methodNode.getModifiers()) ){
        //			reportWarning( _methodPosition, ErrorMessages.NOT_PUBLIC_MIGRATION_SIGNAL_ERROR_MESSAGE);
        //		}

        // we go by the default visit pattern, and do the checking when visiting enclosing blocks
        visitedBlocks.clear();
        Void ret = super.visitMethod(methodNode, trees);

        BlockVisitInfo bvi = _visitedBlocks.get(methodNode.getBody());
        if (!bvi.hasMigrateTo) {
            reportError(_methodPosition, ErrorMessages.MIGRATE_TO_NOT_FOUND_ERROR_MESSAGE);
        } else if (!bvi.migrationUsedCorrectly) {
            reportError(_methodPosition, ErrorMessages.MIGRATE_TO_NOT_FINAL_STATEMENT_ERROR_MESSAGE);
        }

        _methodPosition = null;
        return ret;

    }

    public boolean testModifiers(ModifiersTree modifiersNode) {
        boolean foundPublic = false;

        for (Modifier methodModifier : modifiersNode.getFlags()) {
            if (methodModifier.equals(Modifier.PUBLIC)) {
                foundPublic = true;
                break;
            }
        }

        return foundPublic;
    }

    /*
     * In every block, check whether any migrateTo call is the last one
     */

    class BlockVisitInfo {
        public boolean hasMigrateTo;
        public boolean migrationUsedCorrectly;
        public StatementTree migrationStatement;

        public BlockVisitInfo() {
            hasMigrateTo = false;
            migrationUsedCorrectly = false;
            migrationStatement = null;
        }

        public BlockVisitInfo(boolean hasMT, boolean isLast) {
            hasMigrateTo = hasMT;
            migrationUsedCorrectly = isLast;
        }

        public BlockVisitInfo(boolean hasMT, boolean isLast, StatementTree migrst) {
            hasMigrateTo = hasMT;
            migrationUsedCorrectly = isLast;
            migrationStatement = migrst;
        }

        @Override
        public boolean equals(Object obj) {
            BlockVisitInfo bvi = (BlockVisitInfo) obj;
            return hasMigrateTo == bvi.hasMigrateTo && migrationUsedCorrectly == bvi.migrationUsedCorrectly;
        }

        @Override
        public String toString() {
            return "Has a migrateTo call:" + hasMigrateTo + ";is used correctly:" + migrationUsedCorrectly;
        }
    }

    private BlockVisitInfo visitBlockStatements(List<? extends StatementTree> statements, Trees trees) {

        boolean migrationUsedCorrectlyInSubBlocks = true;
        boolean hasInSubBlocks = false;
        StatementTree migrateToStatement = null;

        for (StatementTree statement : statements) {

            // is a migrateTo statement?
            if (isMigrationCall(statement, trees))
                if (migrateToStatement == null)
                    migrateToStatement = statement;

            // is a switch statement?
            if (statement.getKind().equals(Kind.SWITCH)) {
                // check all underlying group of statements
                BlockVisitInfo bviCase;
                for (CaseTree someCase : ((SwitchTree) statement).getCases()) {
                    bviCase = visitBlockStatements(someCase.getStatements(), trees);
                    bviCase = checkMigrationCorrectness(bviCase, someCase.getStatements());
                    hasInSubBlocks = hasInSubBlocks | bviCase.hasMigrateTo;
                    migrationUsedCorrectlyInSubBlocks = migrationUsedCorrectlyInSubBlocks &
                        bviCase.migrationUsedCorrectly;
                }
            }

            // is a statement with underlying statements?
            List<UnderlyingStatementsInfo> underlyingBlocks = getSubstatementsInfo(statement);
            //            boolean isLoopStmt = isLoop(statement);

            // is it a statement that contains sub-blocks/sub-statements?
            if (!underlyingBlocks.isEmpty()) {
                for (UnderlyingStatementsInfo underInfo : underlyingBlocks) {
                    BlockVisitInfo bvi;
                    if (underInfo.underlyingKind.equals(Kind.BLOCK)) {
                        // we have underlying block
                        bvi = _visitedBlocks.get(underInfo.underlyingBlock);
                    } else {
                        // we have underlying statement. it is always the last one in the sub-context of the super-construct
                        bvi = new BlockVisitInfo(isMigrationCall(underInfo.underlyingStatement, trees), true);
                    }
                    hasInSubBlocks = hasInSubBlocks | bvi.hasMigrateTo;
                    // NOTA decided to give up this feature.
                    //migrationUsedCorrectlyInSubBlocks = isLoopStmt & !bvi.hasMigrateTo || // if it is a loop statement, it must not contain migrateTo calls
                    migrationUsedCorrectlyInSubBlocks = migrationUsedCorrectlyInSubBlocks &
                        bvi.migrationUsedCorrectly; //(!isLoopStmt & bvi.migrationUsedCorrectly); // if it's not a loop statement
                }
            }
        }

        return new BlockVisitInfo(hasInSubBlocks, migrationUsedCorrectlyInSubBlocks, migrateToStatement);
    }

    public BlockVisitInfo checkMigrationCorrectness(BlockVisitInfo bvi,
            List<? extends StatementTree> statements) {

        boolean hasInSubBlocks = bvi.hasMigrateTo;
        boolean migrationUsedCorrectlyInSubBlocks = bvi.migrationUsedCorrectly;

        // case 1 - migrateTo not in this block
        if (bvi.migrationStatement == null) {
            // NOT NECESSARY ANYMORE
            //            if (!hasInSubBlocks)
            //                return new BlockVisitInfo(false, false);
            //
            //            if (!migrationUsedCorrectlyInSubBlocks)
            //                return new BlockVisitInfo(true, false);
            //            else
            //                return new BlockVisitInfo(true, true);
            return new BlockVisitInfo(hasInSubBlocks, migrationUsedCorrectlyInSubBlocks);
        }

        // case 2 - migrateTo call in this block
        // must not also be in sub-blocks
        if (hasInSubBlocks)
            return new BlockVisitInfo(true, false);

        int migrateToPos = statements.indexOf(bvi.migrationStatement);
        int statementsNo = statements.size();

        // programmers count starting from 0
        if (statementsNo - 1 == migrateToPos) {
            // perfekt!
            return new BlockVisitInfo(true, true);
        }

        // definitely not the last statement
        return new BlockVisitInfo(true, false);

        /*      NOTA This code used to allow for return statements as last statements, if the return
         * 		expression was not a method invocation. Specification changed, now migrateTo should *really* be the last statement.
         if (statementsNo - 1 == migrateToPos + 1) {
         // get info on statements
         StatementTree lastStatement = statements.get(statementsNo - 1);

         if (!checkReturnStatement(lastStatement))
         return new BlockVisitInfo(true, false);
         } else {
         // definitely not the last statement
         return new BlockVisitInfo(true, false);
         }

         // migrateTo in this block, and used correctly
         return new BlockVisitInfo(true, true);
         */
    }

    private Map<BlockTree, BlockVisitInfo> _visitedBlocks = new HashMap<BlockTree, BlockVisitInfo>();

    @Override
    public Void visitBlock(BlockTree blockNode, Trees trees) {

        // visit the descendants first
        Void ret = super.visitBlock(blockNode, trees);

        if (_methodPosition == null) {
            // not in a method
            return ret;
        }

        List<? extends StatementTree> statements = blockNode.getStatements();

        // verify the statements of this block
        BlockVisitInfo bvi = visitBlockStatements(statements, trees);

        // according to the info for the sub-blocks, determine if migration is used correctly
        BlockVisitInfo bvRet = checkMigrationCorrectness(bvi, statements);

        _visitedBlocks.put(blockNode, bvRet);
        return ret;

    }

    class UnderlyingStatementsInfo {
        Kind underlyingKind;
        BlockTree underlyingBlock = null;
        StatementTree underlyingStatement = null;

        public UnderlyingStatementsInfo() {
            this.underlyingBlock = null;
            this.underlyingKind = Kind.BLOCK;
            this.underlyingStatement = null;
        }

        public UnderlyingStatementsInfo(BlockTree underlyingBlock) {
            super();
            this.underlyingBlock = underlyingBlock;
            this.underlyingKind = Kind.BLOCK;
        }

        public UnderlyingStatementsInfo(StatementTree underlyingStatement) {
            super();
            this.underlyingStatement = underlyingStatement;
            this.underlyingKind = Kind.EXPRESSION_STATEMENT;
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();

            if (underlyingKind.equals(Kind.BLOCK)) {
                ret.append("block:" + underlyingBlock);
            } else {
                ret.append("statement:" + underlyingStatement);
            }

            return ret.toString();
        }

    }

    /** Get the needed information about the sub-components(sub-blocks or sub-statements)
     *  of the given statement, according to the type of the statement
     * @param statement
     * @return A list of information, with an entry for each sub-component. The list is empty
     * 		if the statement is not of a type which has sub-components
     */
    private List<UnderlyingStatementsInfo> getSubstatementsInfo(StatementTree statement) {

        List<UnderlyingStatementsInfo> statementInfo = new ArrayList<UnderlyingStatementsInfo>();
        Kind statementKind = statement.getKind();

        if (statementKind.equals(Kind.BLOCK)) {
            statementInfo.add(new UnderlyingStatementsInfo((BlockTree) statement));
        } else if (statementKind.equals(Kind.TRY)) {
            TryTree tryTree = (TryTree) statement;
            if (tryTree.getFinallyBlock() != null) {
                // in finally migrateTo should be the last
                statementInfo.add(new UnderlyingStatementsInfo(tryTree.getFinallyBlock()));
                return statementInfo;
            }
            if (tryTree.getBlock() != null)
                statementInfo.add(new UnderlyingStatementsInfo(tryTree.getBlock()));
            for (CatchTree catchTree : tryTree.getCatches()) {
                if (!catchesMigrationException(catchTree))
                    statementInfo.add(new UnderlyingStatementsInfo(catchTree.getBlock()));
            }
        } else if (statementKind.equals(Kind.IF)) {
            IfTree fi = (IfTree) statement;
            StatementTree thenStatement = fi.getThenStatement();
            if (thenStatement.getKind().equals(Kind.BLOCK))
                statementInfo.add(new UnderlyingStatementsInfo((BlockTree) thenStatement));
            else
                statementInfo.add(new UnderlyingStatementsInfo(thenStatement));
            StatementTree esle = fi.getElseStatement();
            if (esle != null)
                if (esle.getKind().equals(Kind.BLOCK))
                    statementInfo.add(new UnderlyingStatementsInfo((BlockTree) esle));
                else
                    statementInfo.add(new UnderlyingStatementsInfo(esle));
        } else if (statementKind.equals(Kind.DO_WHILE_LOOP)) {
            StatementTree loopStatement = ((DoWhileLoopTree) statement).getStatement();
            if (loopStatement.getKind().equals(Kind.BLOCK))
                statementInfo.add(new UnderlyingStatementsInfo((BlockTree) loopStatement));
            else
                statementInfo.add(new UnderlyingStatementsInfo(loopStatement));
        } else if (statementKind.equals(Kind.WHILE_LOOP)) {
            StatementTree loopStatement = ((WhileLoopTree) statement).getStatement();
            if (loopStatement.getKind().equals(Kind.BLOCK))
                statementInfo.add(new UnderlyingStatementsInfo((BlockTree) loopStatement));
            else
                statementInfo.add(new UnderlyingStatementsInfo(loopStatement));
        } else if (statementKind.equals(Kind.FOR_LOOP)) {
            StatementTree loopStatement = ((ForLoopTree) statement).getStatement();
            if (loopStatement.getKind().equals(Kind.BLOCK))
                statementInfo.add(new UnderlyingStatementsInfo((BlockTree) loopStatement));
            else
                statementInfo.add(new UnderlyingStatementsInfo(loopStatement));
        } else if (statementKind.equals(Kind.ENHANCED_FOR_LOOP)) {
            StatementTree loopStatement = ((EnhancedForLoopTree) statement).getStatement();
            if (loopStatement.getKind().equals(Kind.BLOCK))
                statementInfo.add(new UnderlyingStatementsInfo((BlockTree) loopStatement));
            else
                statementInfo.add(new UnderlyingStatementsInfo(loopStatement));
        } else if (statementKind.equals(Kind.SYNCHRONIZED)) {
            statementInfo.add(new UnderlyingStatementsInfo(((SynchronizedTree) statement).getBlock()));
        }

        return statementInfo;
    }

    private boolean catchesMigrationException(CatchTree catchTree) {
        Tree type = catchTree.getParameter().getType();
        if (type.getKind().equals(Kind.IDENTIFIER))
            return isSimpleNameMigrationException(((IdentifierTree) type).getName().toString(),
                    MigrationException.class);
        else if (type.getKind().equals(Kind.MEMBER_SELECT))
            return isNameMigrationException(type.toString(), MigrationException.class);

        return false;
    }

    private boolean isSimpleNameMigrationException(String clazzName, Class<?> excpClass) {
        if (excpClass.getSimpleName().equals(clazzName))
            return true;
        if (excpClass.getSuperclass() == null)
            return false;
        return isSimpleNameMigrationException(clazzName, excpClass.getSuperclass());
    }

    private boolean isNameMigrationException(String clazzName, Class<?> excpClass) {
        if (excpClass.getName().equals(clazzName))
            return true;
        if (excpClass.getSuperclass() == null)
            return false;
        return isNameMigrationException(clazzName, excpClass.getSuperclass());
    }

    /**
     * Verify if a statement is a loop type statement,
     * and if it is, return the enclosing block - if it exists...
     * The problem with loops is that if you call PAMobile.migrateTo() inside
     * the loop block, then it means that you want to call it multiple times.
     * This is a logical error - migrateTo should only be called once.
     * @param statement
     * @return true - statement is of loop kind
     */
    private final boolean isLoop(StatementTree statement) {
        Kind kind = statement.getKind();
        return kind.equals(Kind.DO_WHILE_LOOP) || kind.equals(Kind.FOR_LOOP) ||
            kind.equals(Kind.ENHANCED_FOR_LOOP) || kind.equals(Kind.WHILE_LOOP);
    }

    /**
     * this method check whether the given statement is a return statement, and the
     * expression of the return statement is not another method call ,
     * ie it does not generate another stack frame. This is done using the visitor pattern
     * actually - a precondition is that before calling checkReturnStatement, the enclosing block
     * has already been visited by the current visitor.
     * @return: true, if it is a "simple" return statement.
     */
    private boolean checkReturnStatement(StatementTree lastStatement) {

        if (lastStatement.getKind().equals(Kind.BREAK)) {
            return true;
        }

        if (!(lastStatement.getKind().equals(Kind.RETURN))) {
            return false;
        }

        ReturnTree returnStatement = (ReturnTree) lastStatement;

        ExpressionTree returnExpression = returnStatement.getExpression();

        return !((returnExpression.getKind().equals(Kind.METHOD_INVOCATION)) ||
            (returnExpression.getKind().equals(Kind.NEW_ARRAY)) || (returnExpression.getKind()
                .equals(Kind.NEW_CLASS)));
    }

    /** Testing whether a statement is either a migrateTo call, or a call to a
     * migration signal - ie a method already annotated with @MigrationSignal
     * @param statement
     * @return
     */
    private boolean isMigrationCall(StatementTree statement, Trees trees) {

        if (!(statement.getKind().equals(Kind.EXPRESSION_STATEMENT)))
            return false;

        ExpressionStatementTree lastStatementExpr = (ExpressionStatementTree) statement;

        // the method call is an expression ...
        ExpressionTree lastExpression = lastStatementExpr.getExpression();
        // ... of type MethodInvocationTree ...
        if (!lastExpression.getKind().equals(Kind.METHOD_INVOCATION)) {
            return false;
        }

        MethodInvocationTree methodCall = (MethodInvocationTree) lastExpression;

        return isMigrationSignalCall(methodCall, trees) || isMigrateToCall(methodCall, trees);
    }

    private boolean isMigrationSignalCall(MethodInvocationTree methodCall, Trees trees) {
        ExpressionTree methodSelectionExpression = methodCall.getMethodSelect();
        // case 1 - identifier
        if (methodSelectionExpression.getKind().equals(Kind.IDENTIFIER)) {
            String methodName = ((IdentifierTree) methodSelectionExpression).getName().toString();
            return classContainsMigrationSignal(methodName, trees);
        }
        return false;
    }

    /**
     * Verify if the containing class has a method with a given name
     * and that is annotated with the @MigrationSignal annotation
     * @param methodName - the name of the searched method
     * @param trees - helper class
     * @return true if the containing class has a @MIgrationSignal with the given name
     */
    private boolean classContainsMigrationSignal(String methodName, Trees trees) {
        for (Tree classMember : _containingClass.getMembers()) {
            if (classMember.getKind().equals(Kind.METHOD)) {
                MethodTree method = (MethodTree) classMember;
                if (methodName.equals(method.getName().toString())) {
                    Element methodElem = trees.getElement(trees.getPath(_containingCompilationUnit, method));
                    if (methodElem.getAnnotation(MigrationSignal.class) != null)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * This method tests whether the Java statement represented
     * by the given StatementTree parameter, is a method call statement,
     * which represents a call to the static method ${PAMobileAgent}.${MIGRATE_TO}.
     * see JLS sect. 15.12 for the format of a Java method call. The following two forms
     * of method call statements are considered as a potential match for the call ${PAMobileAgent}.${MIGRATE_TO}:
     * <ul>
     * 	<li> MethodName ( ArgumentList_opt )</li>
     * 	<li> TypeName . Identifier ( ArgumentList_opt ) </li>
     * </ul>
     * where MethodName/Identifier must be ${MIGRATE_TO}, and TypeName must be
     * the identifier [${package.name}]${PAMobileAgent}
     * The ${package.name} and ${PAMobileAgent} names can be changed by refactoring without modifying
     * 	the source code of this visitor, but the ${MIGRATE_TO} method name is hardcoded
     * - see the declaration below - and it must be changed if the API changes. This is because I don't know yet
     * how to do it otherwise.
     */
    private static final String MIGRATE_TO = "migrateTo"; // i dunno how to do it elseway

    private boolean isMigrateToCall(MethodInvocationTree methodCall, Trees trees) {

        // .. and the selection expression must be ...
        ExpressionTree methodSelectionExpression = methodCall.getMethodSelect();
        // ... either an identifier(can be a static import for example) ...
        if (methodSelectionExpression.getKind().equals(Kind.IDENTIFIER)) {
            String methodName = ((IdentifierTree) methodSelectionExpression).getName().toString();
            return methodName.equals(MIGRATE_TO);
        }
        // ... or a member selection expression(most common) ...
        else if (methodSelectionExpression.getKind().equals(Kind.MEMBER_SELECT)) {
            MemberSelectTree memberSelectionExpression = (MemberSelectTree) methodSelectionExpression;
            String methodName = memberSelectionExpression.getIdentifier().toString();
            ExpressionTree typeNameExpression = memberSelectionExpression.getExpression();
            // ... which can only be of the form ${calleeName}.migrateTo
            // calleeName can be ...
            String calleeName = null;
            // ... either unqualified name, ie PAMobileAgent ...
            if (typeNameExpression.getKind().equals(Kind.IDENTIFIER)) {
                calleeName = ((IdentifierTree) typeNameExpression).getName().toString();
            }
            // ... or qualified name, ie ${package.name}.PAMobileAgent.
            else if (typeNameExpression.getKind().equals(Kind.MEMBER_SELECT)) {
                calleeName = ((MemberSelectTree) typeNameExpression).getIdentifier().toString();
            } else
                return false;
            return isClassField(calleeName, methodName, trees) || // the call can also be made against members...
                isLocalVariable(calleeName, methodName) || //... or against local variables
                methodName.equals(MIGRATE_TO) && calleeName.equals(PAMobileAgent.class.getSimpleName());
        } else {
            // is not a migrateTo call!
            return false;
        }

    }

    /**
     * Tests if the given name represents the name of a local variable
     * defined within the containing method
     * TODO test if the local variable is accessible from this scope! i.e. containig block, superblocks etc
     */
    private boolean isLocalVariable(String calleeName, String methodName) {
        for (Tree statement : _containingMethod.getBody().getStatements()) {
            if (statement.getKind().equals(Kind.VARIABLE)) {
                VariableTree varDecl = (VariableTree) statement;
                if (varDecl.getName().toString().equals(calleeName)) {
                    String calleeClassName;
                    if (varDecl.getType().getKind().equals(Kind.IDENTIFIER)) {
                        IdentifierTree varType = (IdentifierTree) varDecl.getType();
                        // try directly
                        calleeClassName = varType.toString();
                        TypeElement calleeType = _elemUtils.getTypeElement(varType.getName());
                        if (calleeType == null) {
                            // no luck? try this then...
                            calleeClassName = getCanonicalName(calleeClassName);
                            if (calleeClassName == null) {
                                // cannot do the check
                                return true;
                            }
                        }
                    } else if (varDecl.getType().getKind().equals(Kind.MEMBER_SELECT)) {
                        calleeClassName = varDecl.getType().toString();
                    } else {
                        // does something else exist? :D
                        calleeClassName = null;
                    }
                    TypeElement calleeType = _elemUtils.getTypeElement(calleeClassName);
                    if (calleeType == null) {
                        // cannot do the check.
                        return true;
                    }

                    // loaded
                    return typeDeclaresMigrationMethod(calleeType, methodName);
                }
            }
        }
        return false;
    }

    /**
     * Try to (re)create the canonincal name
     * Two approaches are used:
     *  - use the package name of the current compilation unit. Maybe the class is in the same source file?
     *  - use the imports list. If the name is used just like that, then probably the package is imported
     * @param className the (simple) name of the class
     * @return the canonical name. Null if it cannot be guessed
     */
    private String getCanonicalName(String className) {
        String ret;
        // try with the package of the current compilation unit
        CompilationUnitTree cu = getCurrentPath().getCompilationUnit();
        ExpressionTree packageExpr = cu.getPackageName();
        ret = packageExpr.toString() + "." + className;
        TypeElement calleeType = _elemUtils.getTypeElement(ret);
        if (calleeType != null) {
            return ret;
        } else {
            // no luck. try the imports list. that ought to do it!
            for (ImportTree imp : cu.getImports()) {

                String impName = imp.toString();
                int pos = impName.lastIndexOf('.');
                String name = impName.substring(pos + 1, impName.length() - 2);
                String pkg = impName.substring(impName.indexOf(' ') + 1, pos);

                if (name.equals("*")) {
                    ret = pkg + "." + className;
                } else if (name.equals(className)) {
                    ret = imp.getQualifiedIdentifier().toString();
                } else
                    continue;

                // try it!
                calleeType = _elemUtils.getTypeElement(ret);
                if (calleeType != null) {
                    return ret;
                }
            }
        }
        // really no luck!
        return null;
    }

    /**
     * Tests if the given name represents the name of a field of the containing class
     * If it is a field, also check if the type of the field has a method with the
     * name ${methodName} and which is annotated with @MigrationSignal  
     */
    private boolean isClassField(String calleeName, String methodName, Trees trees) {
        Element element = trees.getElement(trees.getPath(_containingCompilationUnit, _containingClass));

        // call the real thing
        return isClassField(element, calleeName, methodName);
    }

    /**
     * The real algorithm is recursive
     */
    private boolean isClassField(Element clazz, String calleeName, String methodName) {

        if (clazz.getKind().equals(ElementKind.CLASS)) {
            TypeElement clazzElement = (TypeElement) clazz;
            // search in the current class
            for (Element member : clazzElement.getEnclosedElements()) {
                if (member.getKind().isField()) {
                    VariableElement field = (VariableElement) member;
                    if (field.getSimpleName().toString().equals(calleeName)) {
                        // check if the field type has a method ${methodName} which is annotated with @MigrationSignal
                        if (field.asType().getKind().equals(TypeKind.DECLARED)) {
                            DeclaredType decl = (DeclaredType) field.asType();
                            TypeElement calleeType = (TypeElement) decl.asElement();
                            return typeDeclaresMigrationMethod(calleeType, methodName);
                        }
                    }
                }
            }

            // search in the enclosing class, if any...
            Element enclosingElem = clazzElement.getEnclosingElement();
            if (enclosingElem != null && enclosingElem.getKind().equals(ElementKind.CLASS)) {
                return isClassField(enclosingElem, calleeName, methodName);
            }
        }

        return false;

    }

    /**
     * Utility method used to check if a class has a method with the given name and
     * that is annotated with the MigrationSignal annotation 
     * @param calleeType the class to be checked
     * @param methodName the method we are looking for
     * @return true, if the method exists within the class body and is @MIgrationSignal
     */
    private boolean typeDeclaresMigrationMethod(TypeElement calleeType, String methodName) {
        for (Element calleeElement : _elemUtils.getAllMembers(calleeType)) {
            if (calleeElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement calleeMethod = (ExecutableElement) calleeElement;
                if (methodName.equals(calleeMethod.getSimpleName().toString()) &&
                    (calleeMethod.getAnnotation(MigrationSignal.class) != null)) {
                    // OK
                    return true;
                }
            }
        }

        // If we're here it means we have no migration signal
        return false;
    }

    // error reporting methods
    private void reportError(Element position, String errorMsg) {
        _compilerOutput.printMessage(Diagnostic.Kind.ERROR, ERROR_PREFIX + errorMsg + ERROR_SUFFIX, position);
    }

    private void reportWarning(Element position, String errorMsg) {
        _compilerOutput.printMessage(Diagnostic.Kind.WARNING, ERROR_PREFIX + errorMsg + ERROR_SUFFIX,
                position);
    }

}
