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
package org.objectweb.proactive.extensions.timitspmd.util;

/**
 * Class used only for performances
 *
 * @author The ProActive Team
 *
 */
public class FakeTimer extends HierarchicalTimer {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
    /**
     *
     */
    private static HierarchicalTimer timer = new FakeTimer();

    @Override
    public void start(int n) {
    }

    @Override
    public void stop(int n) {
    }

    public void resetTimer(int n) {
    }

    @Override
    public void setValue(int n, int t) {
    }

    @Override
    public void addValue(int n, int t) {
    }

    @Override
    public boolean isStarted(int n) {
        return false;
    }

    public int getElapsedTime() {
        return 0;
    }

    public int getHierarchicalTime() {
        return 0;
    }

    public int getTotalTime() {
        return 0;
    }

    public static HierarchicalTimer getInstance() {
        return FakeTimer.timer;
    }
}
