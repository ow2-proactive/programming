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
package org.objectweb.proactive.core.body;

import java.util.Map;

import org.objectweb.proactive.core.body.reply.ReplyReceiverFactory;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestQueueFactory;
import org.objectweb.proactive.core.body.request.RequestReceiverFactory;
import org.objectweb.proactive.core.body.tags.MessageTagsFactory;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManagerFactory;
import org.objectweb.proactive.core.util.ThreadStoreFactory;


/**
 * <p>
 * A class implementing this interface if able to provide instances of factories
 * that can create MetaObjects used in the Body.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
//@snippet-start metaobjectfactory
public interface MetaObjectFactory {

    /**
     * Creates or reuses a RequestFactory
     * @return a new or existing RequestFactory
     * @see RequestFactory
     */
    public RequestFactory newRequestFactory();

    /**
     * Creates or reuses a ReplyReceiverFactory
     * @return a new or existing ReplyReceiverFactory
     * @see ReplyReceiverFactory
     */
    public ReplyReceiverFactory newReplyReceiverFactory();

    /**
     * Creates or reuses a RequestReceiverFactory
     * @return a new or existing RequestReceiverFactory
     * @see RequestReceiverFactory
     */
    public RequestReceiverFactory newRequestReceiverFactory();

    /**
     * Creates or reuses a RequestQueueFactory
     * @return a new or existing RequestQueueFactory
     * @see RequestQueueFactory
     */
    public RequestQueueFactory newRequestQueueFactory();

    //    /**
    //     * Creates or reuses a RemoteBodyFactory
    //     * @return a new or existing RemoteBodyFactory
    //     * @see RemoteBodyFactory
    //     */
    //    public RemoteBodyFactory newRemoteBodyFactory();

    /**
     * Creates or reuses a ThreadStoreFactory
     * @return a new or existing ThreadStoreFactory
     * @see ThreadStoreFactory
     */
    public ThreadStoreFactory newThreadStoreFactory();

    // GROUP

    /**
     * Creates or reuses a ProActiveGroupManagerFactory
     * @return a new ProActiveGroupManagerFactory
     */
    public ProActiveSPMDGroupManagerFactory newProActiveSPMDGroupManagerFactory();

    public Object clone() throws CloneNotSupportedException;

    // REQUEST-TAGS
    /**
     * Create the RequestTags manager.
     * @return the RequestTags manager
     */
    public MessageTagsFactory newRequestTagsFactory();
}
//@snippet-end metaobjectfactory
