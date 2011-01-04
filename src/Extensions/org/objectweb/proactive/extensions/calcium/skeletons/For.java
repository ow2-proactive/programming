/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
import org.objectweb.proactive.extensions.calcium.muscle.Execute;


/**
 * The <code>For</code> {@link Skeleton} represents iteration. It is used
 * to execute a nested {@link Skeleton} a specific number of times.
 */
@PublicAPI
public class For<P extends java.io.Serializable> implements Skeleton<P, P> {
    Skeleton<P, P> child;
    int times;

    /**
     * This is the main constructor.
     *
     * @param times The number of times to execute the nested {@link Skeleton}
     * @param nested The nested {@link Skeleton}.
     */
    public For(int times, Skeleton<P, P> nested) {
        this.child = nested;
        this.times = times;
    }

    /**
     * This constructor wraps the {@link Execute} parameter in a {@link Seq}
     * skeleton and invokes the main constructor: {@link For#For(int, Skeleton)}.
     *
     * @param muscle The muscle to wrap in a {@link Seq} {@link Skeleton}.
     */
    public For(int times, Execute<P, P> muscle) {
        this.child = new Seq<P, P>(muscle);
        this.times = times;
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
