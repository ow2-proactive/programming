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
package org.objectweb.proactive.extensions.calcium.skeletons;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Skeleton structure visitors must implement this interface.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface SkeletonVisitor {
    public <P extends Serializable, R extends Serializable> void visit(Farm<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Pipe<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Seq<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(If<P, R> skeleton);

    public <P extends Serializable> void visit(For<P> skeleton);

    public <P extends Serializable> void visit(While<P> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Map<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Fork<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(DaC<P, R> skeleton);
}
