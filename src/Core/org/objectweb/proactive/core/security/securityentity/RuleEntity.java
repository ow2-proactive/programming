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
package org.objectweb.proactive.core.security.securityentity;

import java.io.Serializable;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public abstract class RuleEntity implements Serializable {
    public enum Match {
        OK, DEFAULT, FAILED;
    }

    public static final int UNDEFINED_LEVEL = 0;

    /**
     * Level of the entity, equals the depth of its certificate in the
     * certificate tree (UNDEFINED_LEVEL is the root, above the self signed
     * certificates)
     */
    protected final int level;
    protected final EntityType type;

    protected RuleEntity(EntityType type, int level) {
        this.type = type;
        this.level = level + levelIncrement();
    }

    protected int getLevel() {
        return this.level;
    }

    public EntityType getType() {
        return this.type;
    }

    protected Match match(Entities e) {
        for (Entity entity : e) {
            if (match(entity) == Match.FAILED) {
                return Match.FAILED;
            }
        }
        return Match.OK;
    }

    // returns the number of levels of the entity above the application
    // level
    private int levelIncrement() {
        switch (this.type) {
            case RUNTIME:
            case ENTITY:
                return 1;
            case NODE:
                return 2;
            case OBJECT:
                return 3;
            default:
                return 0;
        }
    }

    abstract protected Match match(Entity e);

    abstract public String getName();

    @Override
    public String toString() {
        return "RuleEntity: depth=" + this.level + ",";
    }
}
