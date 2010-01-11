/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.annotation.migration.strategy;

import org.objectweb.proactive.extensions.annotation.OnDeparture;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;


/**
 * The visitor that implements the checks for the @OnDeparture annotation
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class OnDepartureVisitorAPT extends SimpleDeclarationVisitor {

    private final Messager _compilerOutput;

    // error messages
    protected String ERROR_PREFIX_STATIC = " is annotated using the @" + OnDeparture.class.getSimpleName() +
        " annotation.\n";

    protected final String ERROR_SUFFIX = "\nPlease refer to the ProActive manual for further help on implementing migration strategies.\n";

    protected transient String ERROR_PREFIX;

    public OnDepartureVisitorAPT(final Messager messager) {
        super();
        _compilerOutput = messager;
    }

    @Override
    public void visitMethodDeclaration(MethodDeclaration methodDeclaration) {

        ERROR_PREFIX = methodDeclaration.getSimpleName() + ERROR_PREFIX_STATIC;

        // return type must be void
        if (!(methodDeclaration.getReturnType() instanceof VoidType))
            reportError(methodDeclaration,
                    "the method shouldn't have any return value, but instead returns " +
                        methodDeclaration.getReturnType().toString());

        // method must not have arguments
        if (!methodDeclaration.getParameters().isEmpty())
            reportError(methodDeclaration, "the method accepts parameters");

    }

    protected void reportError(Declaration declaration, String msg) {
        SourcePosition sourceCodePos = declaration.getPosition();
        _compilerOutput.printError(sourceCodePos, "[ERROR]" + ERROR_PREFIX +
            ErrorMessages.INVALID_MIGRATION_STRATEGY_METHOD + ": " + msg + ERROR_SUFFIX);
    }

    protected void reportWarning(Declaration declaration, String msg) {
        SourcePosition sourceCodePos = declaration.getPosition();
        _compilerOutput.printWarning(sourceCodePos, "[WARNING]" + ERROR_PREFIX + msg + ERROR_SUFFIX);
    }

}