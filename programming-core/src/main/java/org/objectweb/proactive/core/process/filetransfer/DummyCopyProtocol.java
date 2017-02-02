/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.process.filetransfer;

/**
 * DummyProtocol for unkown default protocols.
 *
 * @author The ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public class DummyCopyProtocol extends AbstractCopyProtocol {
    public DummyCopyProtocol(String name) {
        super(name);
    }

    /**
     * @see org.objectweb.proactive.core.process.filetransfer.CopyProtocol#startFileTransfer()
     */
    public boolean startFileTransfer() {
        //DummyCopyProtocol is always unsuccessful
        return false;
    }

    /**
     * @see org.objectweb.proactive.core.process.filetransfer.CopyProtocol#checkProtocol()
     */
    public boolean checkProtocol() {
        return true;
    }

    /**
     * Always returns true for DummyProtocol.
     * Overrides the parent abstract method definition.
     */
    @Override
    public boolean isDummyProtocol() {
        return true;
    }
}
