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
package functionalTests.component.nonfunctional.adl.factory.nf;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class ClientWrapperNF1 implements BindingController, NFService {

    final String CLIENT_ITF_1 = "ref";
    final String CLIENT_ITF_2 = "refb";
    NFService next1 = null;
    NFService next2 = null;
    final String[] itfList = new String[] { CLIENT_ITF_1, CLIENT_ITF_2 };
    final String name = "[client-wrapper-nf1]";
    int turn = 0;

    @Override
    public StringWrapper walk() {

        StringWrapper ret;
        //		if(turn == 0) {
        ret = new StringWrapper(name + next1.walk());
        //		}
        //		else {
        //			ret = new StringWrapper(name + next2.walk());
        //		}
        turn = (turn + 1) % 2;
        return ret;
    }

    @Override
    public void print(String msg) {

        if (turn == 0) {
            next1.print(msg + name);
        } else {
            next2.print(msg + name);
        }
        turn = (turn + 1) % 2;

    }

    @Override
    public void bindFc(String clientItf, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (CLIENT_ITF_1.equals(clientItf)) {
            next1 = (NFService) serverItf;
        } else if (CLIENT_ITF_2.equals(clientItf)) {
            next2 = (NFService) serverItf;
        } else
            throw new NoSuchInterfaceException(clientItf);

    }

    @Override
    public String[] listFc() {
        return itfList;
    }

    @Override
    public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
        if (CLIENT_ITF_1.equals(clientItf)) {
            return next1;
        } else if (CLIENT_ITF_2.equals(clientItf)) {
            return next2;
        }
        throw new NoSuchInterfaceException(clientItf);
    }

    @Override
    public void unbindFc(String clientItf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (CLIENT_ITF_1.equals(clientItf)) {
            next1 = null;
        } else if (CLIENT_ITF_2.equals(clientItf)) {
            next2 = null;
        }
        throw new NoSuchInterfaceException(clientItf);
    }

}
