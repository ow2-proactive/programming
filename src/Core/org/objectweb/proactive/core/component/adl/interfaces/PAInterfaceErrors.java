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
package org.objectweb.proactive.core.component.adl.interfaces;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;


/**
 * {@link ErrorTemplate} group for the interfaces package.
 * 
 * @author The ProActive Team
 */
public enum PAInterfaceErrors implements ErrorTemplate {
    WRONG_INTERCEPTOR_NAME("Wrong interceptor name \"%s\" for interface \"%s\"", "interfaceName",
            "interceptorName");

    /** The groupId of ErrorTemplates defined in this enumeration. */
    public static final String GROUP_ID = InterfaceErrors.GROUP_ID;

    private int id;
    private String format;

    private PAInterfaceErrors(final String format, final Object... args) {
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