/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.examples.eratosthenes;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * @author The ProActive Team
 * Serves to print newly found prime numbers to the console.
 */
@ActiveObject
public class ConsolePrimeOutputListener implements PrimeOutputListener, java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private long startTime;
    private int numberCounter;

    /**
     * Constructor for ConsolePrimeOutputListener.
     */
    public ConsolePrimeOutputListener() {
        super();
    }

    public void newPrimeNumberFound(long n) {
        numberCounter++;
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        String time = Long.toString((System.currentTimeMillis() - startTime) / 1000);
        String counter = Integer.toString(numberCounter);
        StringBuffer line = new StringBuffer(50);
        line.append("    ");
        line.append("Prime number ");
        for (int i = counter.length(); i < 6; i++)
            line.append(' ');
        line.append('#');
        line.append(counter);
        line.append(" found with value ");
        line.append(n);
        line.append("\t (");
        for (int i = time.length(); i < 6; i++)
            line.append('0');
        line.append(time);
        line.append("s)\n");
        logger.info(line);
    }
}
