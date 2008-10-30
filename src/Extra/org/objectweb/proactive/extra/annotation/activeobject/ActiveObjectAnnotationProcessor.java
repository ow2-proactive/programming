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
package org.objectweb.proactive.extra.annotation.activeobject;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

/**
 * The AnnotationProcessor that processes the ActiveObject annotation.
 * It processes only objects.
 * For every object encountered, the ActiveObjectVisitor is used to
 * visit the declaration.
 *
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */

public class ActiveObjectAnnotationProcessor implements AnnotationProcessor {

	private final AnnotationProcessorEnvironment _aoEnvironment;
	private final ActiveObjectVisitor _aoVisitor;
	private final AnnotationTypeDeclaration _aoDeclaration;

	public ActiveObjectAnnotationProcessor(AnnotationProcessorEnvironment env) {

		_aoEnvironment = env;
		_aoDeclaration = (AnnotationTypeDeclaration) _aoEnvironment
				.getTypeDeclaration(ActiveObject.class.getName());
		_aoVisitor = new ActiveObjectVisitor(_aoEnvironment.getMessager());

	}

	@Override
	public void process() {
		for( Declaration typeDeclaration : _aoEnvironment.getDeclarationsAnnotatedWith(_aoDeclaration) ) {
			typeDeclaration.accept(_aoVisitor);
		}
	}

}
