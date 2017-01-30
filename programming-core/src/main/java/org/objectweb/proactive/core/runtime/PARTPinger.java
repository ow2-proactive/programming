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
package org.objectweb.proactive.core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Main;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A class that ping a server each time a runtime is started
 *
 */
public class PARTPinger extends Thread {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    @Override
    public void run() {

        if (!CentralPAPropertyRepository.PA_RUNTIME_PING.isTrue()) {
            return;
        }

        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        try {

            // Construct post data
            String data = URLEncoder.encode("version", "UTF-8") + "=" +
                          URLEncoder.encode(Main.getProActiveVersion(), "UTF-8");
            data += "&" + URLEncoder.encode("ping", "UTF-8") + "=" + URLEncoder.encode("true", "UTF-8");
            // Send data
            URL url = new URL(CentralPAPropertyRepository.PA_RUNTIME_PING_URL.getValue());

            URLConnection conn = url.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            conn.setDoOutput(true);

            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String response = "";
            while ((line = rd.readLine()) != null) {
                response += line;
            }
            checkVersion(response);
            wr.close();
            rd.close();

        } catch (Exception e) {
            logger.debug("unable to ping", e);
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                    if (rd != null) {
                        rd.close();
                    }
                } catch (IOException e) {
                    logger.debug("unable to close the ping stream", e);
                }
            }

        }

    }

    private static void checkVersion(String latestVersion) {
        String version = Main.getProActiveVersion();
        if (latestVersion.isEmpty() || latestVersion.length() > 20) {
            logger.debug("Got malformed response from remote server; unable to determine the latest version");
        } else if (version.equals(latestVersion) || isDevelopmentVersion(version)) {
            logger.debug("You are running the latest version of ProActive");
        } else {
            logger.warn("You don't seem to be running the latest released version of ProActive");
            logger.warn(String.format("Version you are using: %s, latest version: %s", version, latestVersion));
            logger.warn("To download the latest release, please visit http://www.activeeon.com/community-downloads");
        }
        logger.debug(String.format("To disable this check, set the %s property to false",
                                   CentralPAPropertyRepository.PA_RUNTIME_PING.getName()));
    }

    private static boolean isDevelopmentVersion(String version) {
        return version.contains("-") || version.contains("$"); // it is a date like "2013-01-01" or "$Id$"
    }

    public static void main(String[] args) {
        new PARTPinger().start();
    }

}
