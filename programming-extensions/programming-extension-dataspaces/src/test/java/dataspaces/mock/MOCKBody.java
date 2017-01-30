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
package dataspaces.mock;

import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.mop.MethodCall;


public class MOCKBody implements Body {

    private UniqueID uid;

    public MOCKBody() {
        uid = new UniqueID();
    }

    public void acceptCommunication() {

    }

    public void blockCommunication() {

    }

    public boolean checkMethod(String methodName, Class<?>[] parametersTypes) {

        return false;
    }

    public boolean checkMethod(String methodName) {

        return false;
    }

    public UniversalBody checkNewLocation(UniqueID uniqueID) {

        return null;
    }

    public void enterInThreadStore() {

    }

    public void exitFromThreadStore() {

    }

    public BodyWrapperMBean getMBean() {

        return null;
    }

    public UniqueID getParentUID() {

        return null;
    }

    public boolean isActive() {

        return false;
    }

    public boolean isAlive() {

        return false;
    }

    public void registerIncomingFutures() {
    }

    public void removeImmediateService(String methodName) {

    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {

    }

    @Deprecated
    public void setImmediateService(String methodName) {

    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes) {

    }

    public void terminate() {

    }

    public void terminate(boolean completeACs) {

    }

    public void updateNodeURL(String newNodeURL) {

    }

    public FuturePool getFuturePool() {

        return null;
    }

    public long getNextSequenceID() {

        return 0;
    }

    public Object getReifiedObject() {

        return null;
    }

    public BlockingRequestQueue getRequestQueue() {

        return null;
    }

    public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody) throws IOException {
    }

    public void serve(Request request) {

    }

    public void serveWithException(Request request, Throwable exception) {

    }

    public void disableAC() throws IOException {

    }

    public void enableAC() throws IOException {

    }

    public UniqueID getID() {
        return uid;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    public String getNodeURL() {

        return null;
    }

    public String getReifiedClassName() {

        return null;
    }

    public UniversalBody getRemoteAdapter() {

        return null;
    }

    public Object receiveHeartbeat() throws IOException {

        return null;
    }

    public void receiveReply(Reply r) throws IOException {
    }

    public void receiveRequest(Request request) throws IOException {
    }

    @Deprecated
    public void register(String url) throws ProActiveException {

    }

    public String registerByName(String name, boolean rebind) throws IOException {

        return null;
    }

    @Override
    public void interruptService() throws IllegalStateException {

    }

    public String registerByName(String name, boolean rebind, String protocol) throws IOException {

        return null;
    }

    public void setRegistered(boolean registered) throws IOException {

    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {

    }

    public String getJobID() {

        return null;
    }

    public String getUrl() {
        return null;
    }

    public String[] getUrls() {
        return null;
    }

    public void setImmediateService(String methodName, boolean uniqueThread) {
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
    }
}
