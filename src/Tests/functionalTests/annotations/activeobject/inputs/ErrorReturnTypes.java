/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.annotations.activeobject.inputs;

import java.io.Serializable;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class ErrorReturnTypes implements Serializable {

    public int _counter;

    public void setCounter(int counter) {
        _counter = counter;
    }

    // error - int is not reifiable
    public int getCounter() {
        return _counter;
    }

    // error - String is not reifiable; use StringWrapper
    public String getMyName() {
        return "BIG_DADDY";
    }

    // error - array not reifiable
    public Object[] whatYouKnow() {
        return new Object[] { /*nothing*/};
    }

    enum Truth {
        TRUE, FALSE, MAYBE
    };

    // error - enums not reifiable
    public Truth politics() {
        return Truth.MAYBE;
    }

    // OK, this object is reifiable
    public ErrorReturnTypes getInstance() {
        return this;
    }
}
