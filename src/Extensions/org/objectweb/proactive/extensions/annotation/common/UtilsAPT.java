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
package org.objectweb.proactive.extensions.annotation.common;

import java.lang.annotation.ElementType;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;


/**
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class UtilsAPT {
    // hack
    public static boolean applicableOnDeclaration(ElementType applicableType, Declaration typeDeclaration) {

        if (typeDeclaration instanceof ClassDeclaration)
            return applicableType.equals(ElementType.TYPE);
        if (typeDeclaration instanceof MethodDeclaration)
            return applicableType.equals(ElementType.METHOD);
        if (typeDeclaration instanceof FieldDeclaration)
            return applicableType.equals(ElementType.FIELD);
        if (typeDeclaration instanceof ConstructorDeclaration)
            return applicableType.equals(ElementType.CONSTRUCTOR);
        // TODO add others when needed

        return false;
    }
}
