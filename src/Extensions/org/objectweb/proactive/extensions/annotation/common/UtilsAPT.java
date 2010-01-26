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
