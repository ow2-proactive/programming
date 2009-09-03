package unitTests.dataspaces.mock;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class MOCKBody implements Body {

    private static final long serialVersionUID = -7767946700765885303L;
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

    public UniversalBody getShortcutTargetBody(ItfID functionalItfID) {

        return null;
    }

    public boolean isActive() {

        return false;
    }

    public boolean isAlive() {

        return false;
    }

    public boolean isSterile() {

        return false;
    }

    public void registerIncomingFutures() {

    }

    public void removeForgetOnSendRequest(Object activeObject, String methodName) {

    }

    public void removeImmediateService(String methodName) {

    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {

    }

    public void setForgetOnSendRequest(Object activeObject, String methodName) {

    }

    public void setImmediateService(String methodName) {

    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes) {

    }

    public void setSterility(boolean isSterile, UniqueID parentUID) {

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

    public String getName() {

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

    public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
            throws IOException, RenegotiateSessionException, CommunicationForbiddenException {

    }

    public void serve(Request request) {

    }

    public void createShortcut(Shortcut shortcut) throws IOException {

    }

    public void disableAC() throws IOException {

    }

    public void enableAC() throws IOException {

    }

    public UniqueID getID() {
        return uid;
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

    public Object receiveFTMessage(FTMessage ev) throws IOException {

        return null;
    }

    public GCResponse receiveGCMessage(GCMessage toSend) throws IOException {

        return null;
    }

    public int receiveReply(Reply r) throws IOException {

        return 0;
    }

    public int receiveRequest(Request request) throws IOException, RenegotiateSessionException {

        return 0;
    }

    public void register(String url) throws ProActiveException {

    }

    public String registerByName(String name) throws IOException {

        return null;
    }

    public void setRegistered(boolean registered) throws IOException {

    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {

    }

    public String getJobID() {

        return null;
    }

    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {

        return null;
    }

    public Entities getEntities() throws SecurityNotAvailableException, IOException {

        return null;
    }

    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException {

        return null;
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException {

        return null;
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {

        return null;
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {

        return null;
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {

        return null;
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {

        return null;
    }

    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException {

    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
            IOException {

        return 0;
    }

    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {

    }

}
