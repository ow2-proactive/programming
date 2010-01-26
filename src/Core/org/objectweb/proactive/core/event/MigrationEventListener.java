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
package org.objectweb.proactive.core.event;

/**
 * <p>
 * A class implementating this interface is listener of <code>MigrationEvent</code>
 * that occurs in the process of the migration of a body associated to an active object.
 * </p>
 *
 * @see MigrationEvent
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface MigrationEventListener extends ProActiveListener {

    /**
     * Signals that a migration is about to start
     * @param event the event that details the migration
     */
    public void migrationAboutToStart(MigrationEvent event);

    /**
     * Signals that the migration is finished on the originating host side
     * @param event the event that details the migration
     */
    public void migrationFinished(MigrationEvent event);

    /**
     * Signals that the migration failed with a exception detailed in the event.
     * @param event the event that details the exception occured in the migration
     */
    public void migrationExceptionThrown(MigrationEvent event);

    /**
     * Signals that the migrated body has restarted of the destination host side
     * @param event the event that details the migration
     */
    public void migratedBodyRestarted(MigrationEvent event);
}
