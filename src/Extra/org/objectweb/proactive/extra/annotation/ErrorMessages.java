/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.annotation;

import org.objectweb.proactive.extra.annotation.activeobject.ActiveObject;
import org.objectweb.proactive.extra.annotation.migration.MigrationSignal;

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
	public static final String NO_NOARG_CONSTRUCTOR_ERROR_MESSAGE = "This object does not define a no-arg constructor. " +
	"An active object must have a no-arg constructor.\n";
	// the class doesn't implement the serializable interface
	public static final String NO_SERIALIZABLE_ERROR_MESSAGE = "An active object should implement the Serializable interface.\n";
	// the class is final
	public static final String IS_FINAL_ERROR_MESSAGE = "An active object must be subclassable, and therefore cannot be final.\n";
	// the class has final members
	public static final String HAS_FINAL_MEMBER_ERROR_MESSAGE = "An active object must be subclassable, and therefore cannot have final members.\n";
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

	// @MigrationSignal
	// the annotation is not used inside an Active Object class declaration
	public static final String NOT_IN_ACTIVE_OBJECT_ERROR_MESSAGE = "The " + MigrationSignal.class.getName() + " annotation has no sense outside an active object.\n" +
			"If you think about using the enclosing class as an active object, maybe you should annotate it with " + ActiveObject.class.getName() + "\n";
	// the migration method is not public
	public static final String NOT_PUBLIC_MIGRATION_SIGNAL_ERROR_MESSAGE = " The method is not public. It does not make sense to have a migration signal that cannot be used from outside the class definition.\n";
	// the migrateTo call is not the last one in the method
	public static final String MIGRATE_TO_NOT_FINAL_STATEMENT_ERROR_MESSAGE = " The migrateTo call is not the last method call in the body of the method.\nThis can cause undefined behaviour. You should put your migrateTo call at the end of the method.\n";
	// the migrateTo call can not be found in the migrateTo method
	public static final String MIGRATE_TO_NOT_FOUND_ERROR_MESSAGE = " The migrateTo call could not be found inside the body of the method.\n ";
}
