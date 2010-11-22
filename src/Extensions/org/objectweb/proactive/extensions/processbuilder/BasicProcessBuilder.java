/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.extensions.processbuilder;

import java.io.IOException;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that wraps around {@link java.lang.ProcessBuilder}.<br>
 * It has no additional capabilities.
 * 
 * @author Zsolt Istvan
 * @since ProActive 4.4.0
 */
public class BasicProcessBuilder extends OSProcessBuilder {

    @Override
    public Boolean canExecuteAsUser(OSUser user) {
        return false;
    }

    @Override
    public Boolean isCoreBindingSupported() {
        return false;
    }

    @Override
    protected String[] wrapCommand() {
        return command().toArray(new String[0]);
    }

    @Override
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        if (user() != null) {
            throw new OSUserException("Executing as given user is not supported for this operating system!");
        }
        if (cores() != null) {
            throw new CoreBindingException("Core binding is not supported for this operating system!");
        }

        return super.start();
    }
}
