/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
//@snippet-start class_CustomMetaObjectFactory
package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * @author ProActive Team
 *
 * Customized Meta-Object Factory
 */
public class CustomMetaObjectFactory extends ProActiveMetaObjectFactory {

    private static final MetaObjectFactory instance = new CustomMetaObjectFactory();

    //return a new factory instance
    public static MetaObjectFactory newInstance() {
        return instance;
    }

    private CustomMetaObjectFactory() {
        super();
    }

    protected RequestFactory newRequestFactorySingleton() {
        System.out.println("Creating the  custom metaobject factory...");
        return new CustomRequestFactory();
    }

    protected class CustomRequestFactory extends RequestFactoryImpl implements Serializable {

        public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay,
                long sequenceID) {
            System.out.println("Received a new request...");
            return new CustomRequest(methodCall, sourceBody, isOneWay, sequenceID);
        }

        protected class CustomRequest extends RequestImpl {
            public CustomRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay,
                    long sequenceID) {
                super(methodCall, sourceBody, isOneWay, sequenceID);
                System.out.println("I am a custom request handler");
            }
        }
    }
}
//@snippet-end class_CustomMetaObjectFactory
