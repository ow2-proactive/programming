/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.dconnection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class DebuggeePortSetter {

    protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEBUGGER);

    public static void main(String[] argv) {
        String dir = System.getProperty("java.io.tmpdir");
        String name = argv[0];
        String port = argv[2];

        File file = new File(dir + File.separator + name);
        if (file.isFile()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(port);
            writer.close();
            logger.info("Debugger JVM started");
        } catch (IOException e) {
            logger.fatal(
                    "Could not create the file where to store the debuggee port, exception message follows ",
                    e);
        }

    }

}
