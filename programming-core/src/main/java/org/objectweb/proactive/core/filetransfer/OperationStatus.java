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
package org.objectweb.proactive.core.filetransfer;

import java.io.IOException;
import java.io.Serializable;


/**
 * This class is used to determine if an operation finished successfully or with
 * an exception.
 *
 * @author The ProActive Team
 */
public class OperationStatus implements Serializable {
    private IOException e = null;

    /**
     * ProActive empty constructor.
     * Can also be used if the operation finished successfully
     */
    public OperationStatus() { //default is successful operation
    }

    /**
     * This constructor can be used if the operation encountered problems
     * while processing.
     * @param e The exception that was encountered
     */
    public OperationStatus(IOException e) { //operation encountered problems
        this.e = e;
    }

    /**
     * Determines if the operation had problems
     * @return true if problems where encountered
     */
    public boolean hasException() {
        return e != null;
    }

    /**
     * Returns the exception that was encountered
     * @return The exception or null if no exception took place.
     */
    public IOException getException() {
        return e;
    }
}
