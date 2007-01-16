/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.core.gc;

import java.lang.ref.WeakReference;

import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


public class Proxy {
    private final WeakReference<UniversalBodyProxy> weak;
    private UniversalBodyProxy strong;
    private int strongCount;

    public Proxy(UniversalBodyProxy proxy) {
        this.weak = new WeakReference<UniversalBodyProxy>(proxy);
        this.strong = null;
        this.strongCount = 0;
    }

    /**
     * Make the proxy a strong reference. If the call was successfull, it is
     * mandatory to call setWeak() when the reference is no more needed,
     * otherwise the proxy would never be garbage collected.
     */
    public boolean setStrong() {
        this.strong = this.weak.get();
        if (this.strong != null) {
            this.strongCount++;
        } else if (this.strongCount != 0) {
            throw new IllegalStateException("A strong proxy was GCed");
        }
        return this.strong != null;
    }

    /**
     * Drop a strong reference to the proxy.
     */
    public void setWeak() {
        if (this.strong == null) {
            throw new IllegalStateException("A strong proxy was GCed");
        }
        if (this.strongCount <= 0) {
            throw new IllegalStateException("Proxy reference was not strong");
        }
        if (--this.strongCount == 0) {
            this.strong = null;
        }
    }

    public UniversalBodyProxy getStrong() {
        return this.strong;
    }
}
