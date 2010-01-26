/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.extensions.annotation.migratable;

import java.io.Serializable;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.common.ErrorMessages;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;


/**
 * CTree visitor for the Migratable annotation
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class MigratableVisitorCTree extends TreePathScanner<Void, Trees> {

    private final Messager _compilerOutput;
    private final Types _typeUtils;

    public MigratableVisitorCTree(ProcessingEnvironment processingEnv) {
        super();
        _compilerOutput = processingEnv.getMessager();
        _typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Void visitClass(ClassTree clazzDecl, Trees trees) {

        Element clazzElement = trees.getElement(getCurrentPath());

        // sanity check
        if (!((clazzElement != null) && (clazzElement instanceof TypeElement) && (clazzElement.getKind()
                .isClass())))
            return super.visitClass(clazzDecl, trees);

        TypeElement clazzDef = (TypeElement) clazzElement;

        if (clazzElement.getAnnotation(ActiveObject.class) == null) {
            _compilerOutput
                    .printMessage(Diagnostic.Kind.ERROR, ErrorMessages.MIGRATABLE_NOT_AO, clazzElement);
        }

        if (!implementsSerializable(clazzDef)) {
            _compilerOutput.printMessage(Diagnostic.Kind.ERROR, ErrorMessages.MIGRATABLE_SERIALIZABLE,
                    clazzElement);
        }

        return super.visitClass(clazzDecl, trees);
    }

    private boolean implementsSerializable(TypeElement clazzDecl) {

        // is this serializable?
        if (clazzDecl.getQualifiedName().toString().equals(Serializable.class.getCanonicalName()))
            return true;

        // test the superclass
        TypeMirror zuper = clazzDecl.getSuperclass();
        if (!zuper.getKind().equals(TypeKind.NONE)) {
            Element zuperClass = _typeUtils.asElement(zuper);
            if (zuperClass.getKind().isClass())
                if (implementsSerializable((TypeElement) zuperClass))
                    return true;
        }

        // verify the superinterfaces
        for (TypeMirror ifType : clazzDecl.getInterfaces()) {
            Element ifElem = _typeUtils.asElement(ifType);
            if (!(ifElem.getKind().isInterface()))
                continue;
            if (implementsSerializable((TypeElement) ifElem))
                return true;
        }

        return false;
    }

}
