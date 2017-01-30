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
package org.objectweb.proactive.extensions.annotation.common;

import java.lang.annotation.ElementType;

import javax.lang.model.element.ElementKind;

import org.objectweb.proactive.core.ProActiveRuntimeException;


/**
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class UtilsCTree {
    public static ElementType convertToElementType(ElementKind kind) {

        switch (kind) {
            case ANNOTATION_TYPE:
                return ElementType.ANNOTATION_TYPE;
            case CLASS:
                return ElementType.TYPE;
            case CONSTRUCTOR:
                return ElementType.CONSTRUCTOR;
            case FIELD:
                return ElementType.FIELD;
            case INTERFACE:
                return ElementType.TYPE;
            case LOCAL_VARIABLE:
                return ElementType.LOCAL_VARIABLE;
            case METHOD:
                return ElementType.METHOD;
            case PACKAGE:
                return ElementType.PACKAGE;
            case PARAMETER:
                return ElementType.PARAMETER;
            // no match for the following fields
            case INSTANCE_INIT:
            case ENUM:
            case ENUM_CONSTANT:
            case EXCEPTION_PARAMETER:
            case STATIC_INIT:
            case TYPE_PARAMETER:
            case OTHER:
        }

        throw new ProActiveRuntimeException("Cannot match from java.lang.annotation.ElementType." + kind +
                                            " to java.lang.annotation.ElementType");
    }
}
