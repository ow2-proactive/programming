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
package org.objectweb.proactive.core.component.adl.implementations;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;


/**
 * {@link ErrorTemplate} group for the implementations package.
 *
 * @author The ProActive Team
 */
public enum PAImplementationErrors implements ErrorTemplate {
    /** */
    VIRTUAL_NODE_NOT_FOUND("Cannot find virtual node \"%s\" in the deployment descriptor.", "vnName"),

    /** */
    INCOMPATIBLE_VIRTUAL_NODE_CARDINALITY(
            "Cannot deploy on a single virtual node when the cardinality of this virtual node named \"%s\" in the ADL is set to multiple.",
            "vnName"),

    /** */
    EMPTY_CONTROLLER_NODE(
            "The <controller> node is empty. It should contain at least one component or interface; otherwise it must contain a controller descriptor file.");

    /** The groupId of ErrorTemplates defined in this enumeration. */
    public static final String GROUP_ID = "PAIMP";

    private int id;
    private String format;

    private PAImplementationErrors(final String format, final Object... args) {
        this.id = ordinal();
        this.format = format;
        assert validErrorTemplate(this, args);
    }

    public int getErrorId() {
        return id;
    }

    public String getGroupId() {
        return GROUP_ID;
    }

    public String getFormatedMessage(final Object... args) {
        return String.format(format, args);
    }

    public String getFormat() {
        return format;
    }
}
