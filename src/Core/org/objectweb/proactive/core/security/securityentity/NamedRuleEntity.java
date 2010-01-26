/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.core.security.securityentity;

import java.security.KeyStore;
import java.security.KeyStoreException;

import org.objectweb.proactive.core.security.KeyStoreTools;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public class NamedRuleEntity extends RuleEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    /**
     *
     */
    private final String name;

    public NamedRuleEntity(EntityType type, KeyStore keystore, String name) throws KeyStoreException {
        super(type, KeyStoreTools.getApplicationLevel(keystore));
        this.name = name;
    }

    @Override
    protected Match match(Entities e) {
        for (Entity entity : e) {
            if (match(entity) == Match.OK) {
                return Match.OK;
            }
        }
        return Match.FAILED;
    }

    @Override
    protected Match match(Entity e) {
        if ((e.getType() == this.getType()) && e.getName().equals(this.name)) {
            return Match.OK;
        }

        return Match.FAILED;
    }

    @Override
    public String toString() {
        return super.toString() + "," + this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
