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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.chartit.data.provider.predefined;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


public class FloatArrayDataProvider implements IDataProvider {

    public static final String NAME = "arrayOfFloat";
    public static final String DESCRIPTION = "Test purpose array of float.";
    public static final String TYPE = "[D";

    /**
     * The reference on the array
     */
    private final double[] arr;

    /**
     * Builds a new instance of FloatArrayDataProvider class.
     */
    public FloatArrayDataProvider() {
        this.arr = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.arr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return TYPE;
    }
}