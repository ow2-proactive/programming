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
package org.objectweb.proactive.examples.jmx.remote.management.client.entities;

import java.io.Serializable;

import javax.management.ObjectName;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.examples.jmx.remote.management.events.EntitiesEventManager;


public class RemoteTransactionGroup extends ManageableEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    /**
     *
     */
    private String name;
    private ManageableEntity entities;
    private Group<ManageableEntity> gEntities;

    public RemoteTransactionGroup(String name) {
        this.name = name;
        try {
            this.entities = (ManageableEntity) PAGroup.newGroup(ManageableEntity.class.getName());
            this.gEntities = PAGroup.getGroup(this.entities);
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEntity(ManageableEntity entity) {
        this.gEntities.add(entity);
        EntitiesEventManager.getInstance().newEvent(this, EntitiesEventManager.ENTITY_ADDED);
    }

    @Override
    public Object[] getChildren() {
        return this.gEntities.toArray();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ManageableEntity getParent() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return gEntities.size() > 0;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeEntity(ManageableEntity entity) {
        this.gEntities.remove(entity);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public ProActiveConnection getConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectName getObjectName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }
}
