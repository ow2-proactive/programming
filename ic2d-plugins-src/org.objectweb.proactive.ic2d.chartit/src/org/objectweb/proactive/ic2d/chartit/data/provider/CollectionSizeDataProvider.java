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
package org.objectweb.proactive.ic2d.chartit.data.provider;

import java.util.Collection;

import org.objectweb.proactive.ic2d.chartit.util.Utils;


/**
 * This class provides a generic way to get the size of a collection. The
 * <code>type</code> of the provided value is <code>int</code>.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class CollectionSizeDataProvider implements IDataProvider {

    /**
     * The name of this provider
     */
    private final String name;

    /**
     * The description of the provided value
     */
    private final String description;

    /**
     * The collection that will provide its size
     */
    private final Collection<?> collection;

    /**
     * Creates a new instance of <code>CollectionSizeDataProvider</code>
     * class.
     * 
     * @param name
     *            The name of this provider
     * @param description
     *            The description of the provided value
     * @param collection
     *            The collection that will provide its size
     */
    public CollectionSizeDataProvider(final String name, final String description,
            final Collection<?> collection) {
        this.name = name;
        this.description = description;
        this.collection = collection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return this.description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return Utils.PRIMITIVE_TYPE_INT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.collection.size();
    }
}
