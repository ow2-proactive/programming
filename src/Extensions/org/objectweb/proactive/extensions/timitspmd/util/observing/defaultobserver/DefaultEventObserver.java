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
package org.objectweb.proactive.extensions.timitspmd.util.observing.defaultobserver;

import org.objectweb.proactive.extensions.timitspmd.util.observing.Event;
import org.objectweb.proactive.extensions.timitspmd.util.observing.EventData;
import org.objectweb.proactive.extensions.timitspmd.util.observing.EventObservable;
import org.objectweb.proactive.extensions.timitspmd.util.observing.EventObserver;


/**
 *
 * @author The ProActive Team
 */
public class DefaultEventObserver implements EventObserver {

    private static final long serialVersionUID = 52;

    /**
     *
     */
    private DefaultEventData eventData;
    private String name;

    /** Creates a new instance of DefaultEventObserver */
    public DefaultEventObserver(String name) {
        this(name, DefaultEventData.SUM, DefaultEventData.SUM);
    }

    public DefaultEventObserver(String name, int collapseOperation, int notifyOperation) {
        this.eventData = new DefaultEventData(name, collapseOperation, notifyOperation);
        this.name = name;
    }

    public void update(EventObservable o, Object arg) {
        if (arg instanceof Event && (((Event) arg).getObserver() == this)) {
            this.eventData.performNotifyOperation(((Event) arg).getValue());
        }
    }

    public EventData getEventData() {
        return this.eventData;
    }

    /**
     * Return the name of this event observer
     */
    public String getName() {
        return this.name;
    }
}
