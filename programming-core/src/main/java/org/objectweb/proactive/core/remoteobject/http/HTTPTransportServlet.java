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
package org.objectweb.proactive.core.remoteobject.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;
import org.objectweb.proactive.core.remoteobject.http.util.HttpUtils;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** HTTP Communication protocol endpoint
 *
 * This servlet enables the HTTP Communication protocol.
 */
public class HTTPTransportServlet extends HttpServlet {

    private static final long serialVersionUID = 60L;
    final static public String NS = "/httpTransport";
    final static public String MAPPING = NS;
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.HTTP_TRANSPORT);

    static HTTPTransportServlet servlet = null;

    static public synchronized HTTPTransportServlet get() {
        if (servlet == null) {
            HTTPServer server = HTTPServer.get();
            servlet = new HTTPTransportServlet();
            server.registerServlet(new ServletHolder(servlet), HTTPTransportServlet.MAPPING);
        }

        return servlet;
    }

    public HTTPTransportServlet() {

    }

    /** Get the URL of the HTTP Transport endpoint for this runtime */
    public String getURL() {
        URI uri = URIBuilder
                .buildURI(URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()), NS,
                        Constants.XMLHTTP_PROTOCOL_IDENTIFIER, CentralPAPropertyRepository.PA_XMLHTTP_PORT
                                .getValue());

        return uri.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        // Read the payload
        final byte[] data = new byte[req.getContentLength()];
        DataInputStream input = new DataInputStream(req.getInputStream());
        input.readFully(data);

        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            // Handle the message
            HttpMessage message;
            try {
                message = (HttpMessage) HttpMarshaller.unmarshallObject(data);
            } catch (Throwable t) {
                Throwable t1 = new IOException6("Failed to unmarshall incoming message", t);
                SynchronousReplyImpl sr = new SynchronousReplyImpl(new MethodCallResult(null, t1));
                byte[] buf = HttpMarshaller.marshallObject(sr);
                DataOutputStream ouput = new DataOutputStream(resp.getOutputStream());
                ouput.write(buf, 0, buf.length);
                resp.setContentType(HttpUtils.SERVICE_REQUEST_CONTENT_TYPE);
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            final Object result = message.processMessage();
            final byte[] resultBytes;

            try {
                resultBytes = HttpMarshaller.marshallObject(result);
            } catch (Throwable t) {
                Throwable t1 = new IOException6("Failed to marshall the result bytes", t);
                SynchronousReplyImpl sr = new SynchronousReplyImpl(new MethodCallResult(null, t1));
                byte[] buf = HttpMarshaller.marshallObject(sr);
                DataOutputStream ouput = new DataOutputStream(resp.getOutputStream());
                ouput.write(buf, 0, buf.length);
                resp.setContentType(HttpUtils.SERVICE_REQUEST_CONTENT_TYPE);
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            try {
                DataOutputStream ouput = new DataOutputStream(resp.getOutputStream());
                ouput.write(resultBytes, 0, resultBytes.length);
                resp.setContentType(HttpUtils.SERVICE_REQUEST_CONTENT_TYPE);
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (Throwable t) {
                logger.info("Failed to send the HTTP reply to " + message +
                    ". The client side should discover the disconnection and unlock the caller", t);
                return;
            }

            // Send the response
            DataOutputStream ouput = new DataOutputStream(resp.getOutputStream());
            ouput.write(resultBytes, 0, resultBytes.length);
            resp.setContentType(HttpUtils.SERVICE_REQUEST_CONTENT_TYPE);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            ProActiveLogger.getLogger(Loggers.HTTP_TRANSPORT).warn("HTTP Failed to serve a message", e);
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_OK);
            Writer output = resp.getWriter();
            output.write(ProActiveLogger.getStackTraceAsString(e));
        } finally {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }
    }
}
