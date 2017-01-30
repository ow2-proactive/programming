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

import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * This interface centralizes all the error messages reported by the annotation processors
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */
public interface ErrorMessages {

    /*
     * Error Messages issued when checking classes annotated with the ActiveObject annotation
     * errors related to ActiveObject conforming rules
     */

    // @ActiveObject
    // the class doesn't have a no-arg constructor
    public static final String NO_NOARG_CONSTRUCTOR_ERROR_MESSAGE = "This object does not define a no-arg empty constructor.";

    public static final String EMPTY_CONSTRUCTOR = "An active object non-args constructor should be empty";

    // the class's  no-arg constructor is private
    public static final String NO_NOARG_CONSTRUCTOR_CANNOT_BE_PRIVATE_MESSAGE = "An active object cannot have private non-args constructor";

    // the class is final
    public static final String IS_FINAL_ERROR_MESSAGE = "An active object must be subclassable, and therefore cannot be final.\n";

    // the class has final methods
    public static final String HAS_FINAL_METHOD_ERROR_MESSAGE = "An active object cannot have final methods.\n";

    // the class has final field
    public static final String HAS_FINAL_FIELD_ERROR_MESSAGE = "An active object cannot have final fileds.\n";

    // the class has volatile members
    public static final String HAS_SYNCHRONIZED_MEMBER_ERORR_MESSAGE = "An active object already has an implicit synchronisation mechanism, wait-by-necessity. The synchronized/volatile keywords are therefore useless for a member of an active object.\n";

    // the class is not public
    public static final String IS_NOT_PUBLIC_ERROR_MESSAGE = "An active object must be public.\n";

    // the return type of a method of the class is not reifiable
    public static final String RETURN_TYPE_NOT_REIFIABLE_ERROR_MESSAGE = " is not a reifiable type. The return type must be reifiable in order to have asynchronous method calls.\n";

    // there is a field without getters/setters
    public static final String NO_GETTERS_SETTERS_ERROR_MESSAGE = "A field of an active object cannot be accessed directly, but only through getter/setter methods.\n";

    // a method of the class returns null
    public static final String NO_NULL_RETURN_ERROR_MSG = "A method of an active object should not return null," +
                                                          " as the caller cannot check the future value against the null literal";

    // @VirtualNodeIsReadyCallback and @NodeAttachmentCallback
    public static final String INCORRECT_METHOD_SIGNATURE_FOR_ISREADY_CALLBACK = "Incorrect method signature. \nIsReady callback method must have the following signature: void method(String)";

    public static final String INCORRECT_METHOD_SIGNATURE_FOR_NODE_ATTACHEMENT_CALLBACK = "Incorrect method signature. \nNode attachement callback method must have the following signature: void method(Node, String)";
}
