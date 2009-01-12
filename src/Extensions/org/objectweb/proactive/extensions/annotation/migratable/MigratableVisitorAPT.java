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
package org.objectweb.proactive.extensions.annotation.migratable;

import java.io.Serializable;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;


/**
 * APT visitor for the Migratable annotation
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class MigratableVisitorAPT extends SimpleDeclarationVisitor {

    private final Messager _compilerOutput;
    // 	error messages
    private static final String ERROR_SUFFIX = "Please refer to the ProActive manual for further help on creating Active Objects.";

    public MigratableVisitorAPT(Messager messager) {
        super();
        _compilerOutput = messager;
    }

    @Override
    public void visitClassDeclaration(ClassDeclaration clazzDecl) {

        if (clazzDecl.getAnnotation(ActiveObject.class) == null) {
            reportError(clazzDecl, ErrorMessages.MIGRATABLE_NOT_AO);
        }
        if (!implementsSerializable(clazzDecl)) {
            reportError(clazzDecl, ErrorMessages.MIGRATABLE_SERIALIZABLE);
        }

        super.visitClassDeclaration(clazzDecl);
    }

    private boolean implementsSerializable(TypeDeclaration clazzDecl) {

        // is this serializable?
        if (clazzDecl.getQualifiedName().toString().equals(Serializable.class.getCanonicalName()))
            return true;

        // verify the superinterfaces
        for (InterfaceType ifType : clazzDecl.getSuperinterfaces()) {
            if (implementsSerializable(ifType.getDeclaration()))
                return true;
        }

        return false;
    }

    protected void reportError(Declaration declaration, String msg) {
        SourcePosition classPos = declaration.getPosition();
        _compilerOutput.printError(classPos, "[ERROR]" + msg + ERROR_SUFFIX);
    }

    protected void reportWarning(Declaration declaration, String msg) {
        SourcePosition classPos = declaration.getPosition();
        _compilerOutput.printWarning(classPos, "[WARNING]" + msg + ERROR_SUFFIX);
    }

}
