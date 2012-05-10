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
package org.objectweb.proactive.extensions.calcium.skeletons;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.muscle.Condition;


/**
 * This class provides a conditional branching {@link Skeleton}.
 *
 * Depending on the result of the {@link Condition}, either
 * one nested {@link Skeleton} is executed or the other.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class If<P extends java.io.Serializable, R extends java.io.Serializable> implements Skeleton<P, R> {
    Condition<P> cond;
    Skeleton<P, ?> ifChild;
    Skeleton<P, ?> elseChild;

    /**
     * This is the main constructor.
     *
     * @param cond The {@link Condition} to evaluate.
     * @param ifChild The <code>true</code> case {@link Skeleton}.
     * @param elseChild The <code>false</code> case {@link Skeleton}.
     */
    public If(Condition<P> cond, Skeleton<P, R> ifChild, Skeleton<P, R> elseChild) {
        this.cond = cond;
        this.ifChild = ifChild;
        this.elseChild = elseChild;
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
