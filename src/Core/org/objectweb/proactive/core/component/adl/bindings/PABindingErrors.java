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
package org.objectweb.proactive.core.component.adl.bindings;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;


/**
 * {@link ErrorTemplate} group for the bindings package.
 *
 * @author The ProActive Team
 */
public enum PABindingErrors implements ErrorTemplate {

    /** */
    INVALID_SIGNATURE(
            "Invalid binding: client interface signature is not compatible with server interface signature, and none of them is collective.\nClient interface defined at \"%s\".\nServer interface defined at \"%s\".",
            "fromlocation", "tolocation"),

    /** */
    INVALID_FROM_INTERNAL(
            "Invalid binding: client interface \"%s\" of enclosing component is not a server interface, nor it is internal-client. Interface defined at %s.",
            "itfName", "location"),

    /** */
    INVALID_TO_INTERNAL(
            "Invalid binding: server interface \"%s\" of enclosing component is not a client interface, nor it is internal-server. Interface defined at %s.",
            "itfName", "location"),

    /** */
    INVALID_COLLECTIVE_SIGNATURE(
            "Invalid binding: incompatible binding involving a multicast or gathercast interface. Client interface defined at \"%s\" and server interface defined at \"%s\" do not have the same number of methods.",
            "fromlocation", "tolocation");

    /** The groupId of ErrorTemplates defined in this enumeration. */
    public static final String GROUP_ID = "PABDG";

    private int id;
    private String format;

    private PABindingErrors(final String format, final Object... args) {
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
