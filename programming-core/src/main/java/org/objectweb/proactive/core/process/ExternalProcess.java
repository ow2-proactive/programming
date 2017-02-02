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
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * A class implementing this interface is able to start a UniversalProcess and to connect
 * its input/output to handlers or to close all streams.
 */
public interface ExternalProcess extends UniversalProcess {

    /**
     * Closes Input, Output, Error stream just after forking this process
     */
    public void closeStream();

    /**
     * Returns the RemoteProcessMessageLogger handling the input stream of the process
     * @return the RemoteProcessMessageLogger handling the input stream of the process
     */
    public RemoteProcessMessageLogger getInputMessageLogger();

    /**
     * Returns the RemoteProcessMessageLogger handling the error stream of the process
     * @return the RemoteProcessMessageLogger handling the error stream of the process
     */
    public RemoteProcessMessageLogger getErrorMessageLogger();

    /**
     * Returns the MessageSink handling the output stream of the process
     * @return the MessageSink handling the output stream of the process
     */
    public MessageSink getOutputMessageSink();

    /**
     * sets the RemoteProcessMessageLogger handling the input stream of the process
     * @param inputMessageLogger the handler of the input stream of the process
     */
    public void setInputMessageLogger(RemoteProcessMessageLogger inputMessageLogger);

    /**
     * sets the RemoteProcessMessageLogger handling the error stream of the process
     * @param errorMessageLogger the handler of the error stream of the process
     */
    public void setErrorMessageLogger(RemoteProcessMessageLogger errorMessageLogger);

    /**
     * sets the MessageSink handling the output stream of the process
     * @param outputMessageSink the handler of the output stream of the process
     */
    public void setOutputMessageSink(MessageSink outputMessageSink);

    /**
     * This method returns a single FileTransferStructure instance
     * for this process. If many invocations to this method are done,
     * the same instance of FileTransferStructure will be returned.
     * This means, that changes made to the structure will be reflected
     * on all the references obtained through this method for this process.
     * Note that different process do not share a FileTransferStructure,
     * and thus changes made to one will not reflect on the other.
     */
    public FileTransferWorkShop getFileTransferWorkShopDeploy();

    /**
     * This method returns a single FileTransferStructure instance
     * for this process. If many invocations to this method are done,
     * the same instance of FileTransferStructure will be returned.
     * This means, that changes made to the structure will be reflected
     * on all the references obtained through this method for this process.
     * Note that different process do not share a FileTransferStructure,
     * and thus changes made to one will not reflect on the other.
     */
    public FileTransferWorkShop getFileTransferWorkShopRetrieve();

    /**
     * Returns the type of this process.
     * For processes that does not reference another process, the type is NO_COMPOSITION
     * For other types are APPEND_TO_COMMAND_COMPOSITION or SEND_TO_OUTPUT_STREAM_COMPOSITION or
     * GIVE_COMMAND_AS_PARAMETER or COPY_FILE_AND_APPEND_COMMAND.
     * @return the type this process.
     */
    public int getCompositionType();

    /**
     * This method will return true when a process requires that
     * a File Transfer Deploy takes place to a node deployed
     * from this process.
     * @return Returns the requiresFileTransferDeployOnNodeCreation.
     */
    public boolean isRequiredFileTransferDeployOnNodeCreation();
}
