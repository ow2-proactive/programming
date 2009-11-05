/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>
 * ComponentActive is the root of the all interfaces related to the activity of
 * a component.</p>
 * <p>
 * In this implementation of the Fractal model, every component is an active
 * object. The (non-functional) activity of the component can be customized by implementing
 * the interfaces {@link org.objectweb.proactive.core.component.body.ComponentInitActive}, {@link org.objectweb.proactive.core.component.body.ComponentRunActive},
 * {@link org.objectweb.proactive.core.component.body.ComponentEndActive}.</p>
 * <p> The non-functional activity of the component, if redefined, should use a request filter on component requests to distinguish
 * non-functional component requests from functional component requests. The default policy is FIFO.</p>
 * The functional activity can also be defined in a primitive component as usually through
 * the {@link org.objectweb.proactive.InitActive}, {@link org.objectweb.proactive.RunActive}
 * and {@link org.objectweb.proactive.EndActive} interfaces.</p>
 * <p>The functional activity is initiated when the lifecycle of the component starts.</p>
 * <p>The functional activity is terminated when the lifecycle of the component ends, provided the implementation
 * of the {@link org.objectweb.proactive.RunActive#runActivity(Body)} method uses a loop <pre>while (isActive())/<pre>.</p>
 * <p>
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface ComponentActive extends Active {
}
