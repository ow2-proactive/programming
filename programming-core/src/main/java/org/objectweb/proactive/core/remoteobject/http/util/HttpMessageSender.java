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
package org.objectweb.proactive.core.remoteobject.http.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.remoteobject.http.HTTPTransportServlet;
import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpMessageSender {
    final static private Logger logger = ProActiveLogger.getLogger(Loggers.HTTP_CLIENT);

    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";

    private String url;

    /**
     *
     * @param url
     */
    public HttpMessageSender(String url) {
        this.url = url;
    }

    /**
     *
     * @param message
     */
    public Object sendMessage(HttpMessage message) throws HTTPRemoteException {
        byte[] bytes = HttpMarshaller.marshallObject(message);

        String url_;

        //        try {
        //            url_ = UrlBuilder.checkUrl(url);
        //            System.out.println("************* " + url_);
        //        } catch (UnknownHostException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        try {
            String nodename = null;
            if (!url.startsWith(Constants.XMLHTTP_PROTOCOL_IDENTIFIER)) {
                url = Constants.XMLHTTP_PROTOCOL_IDENTIFIER + url;
            }
            int lastslash = url.lastIndexOf('/');
            if (lastslash > 6) {
                URI u;
                u = new URI(url);
                nodename = u.getPath();
                url = URIBuilder.getProtocol(u) + "://" + u.getHost() + ":" + u.getPort();
            }
            int lastIndex = url.lastIndexOf(":");

            URL u;
            if (!HTTPServer.SERVER_CONTEXT.equals("/")) {
                u = new URL(url + HTTPServer.SERVER_CONTEXT + HTTPTransportServlet.NS);
            } else {
                u = new URL(url + HTTPTransportServlet.NS);
            }

            //connection to the specified url
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            if (CentralPAPropertyRepository.PA_HTTP_CONNECT_TIMEOUT.isSet()) {
                connection.setConnectTimeout(CentralPAPropertyRepository.PA_HTTP_CONNECT_TIMEOUT.getValue());
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Length", "" + bytes.length);
            connection.setRequestProperty("Content-Type", SERVICE_REQUEST_CONTENT_TYPE);
            connection.setUseCaches(false);
            connection.connect();

            //write data in the stream
            BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());

            out.write(bytes);
            out.flush();
            out.close();

            //Get data returned in the connection
            DataInputStream in = null;
            in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

            byte[] buf = new byte[0];
            byte[] acc = new byte[1024];
            int acc_i = 0;
            boolean cont = true;
            while (cont) {
                try {
                    if (acc_i == acc.length) {
                        // acc is full flush it into buf
                        byte[] old = buf;
                        buf = new byte[old.length + acc.length];
                        System.arraycopy(old, 0, buf, 0, old.length);
                        System.arraycopy(acc, 0, buf, old.length, acc.length);

                        acc_i = 0;
                    }

                    acc[acc_i] = in.readByte();
                    acc_i++;
                } catch (EOFException e) {
                    cont = false;
                }
            }

            // flush acc into buf
            byte[] old = buf;
            buf = new byte[old.length + acc_i];
            System.arraycopy(old, 0, buf, 0, old.length);
            System.arraycopy(acc, 0, buf, old.length, acc_i);

            Object returnedObject = HttpMarshaller.unmarshallObject(buf);
            return returnedObject;

            //            if (returnedObject instanceof Exception)
            //                throw (Exception)returnedObject;
        } catch (ConnectException e) {
            throw new HTTPRemoteException("Error while connecting the remote host: " + url, e);
        } catch (UnknownHostException e) {
            throw new HTTPRemoteException("Unknown remote host: " + url, e);
        } catch (IOException e) {
            throw new HTTPRemoteException("Error during connection with remote host" + url, e);
        } catch (URISyntaxException e) {
            throw new HTTPRemoteException("Bad URL " + url, e);
        }
    }
}
