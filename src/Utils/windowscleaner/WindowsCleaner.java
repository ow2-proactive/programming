/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package windowscleaner;

import org.jvnet.winp.WinProcess;


/**
 * This helper class kills every process on the system matching the regexp passed as parameter
 */
public class WindowsCleaner {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("You have to give a regular expression in parameter (ex: .*proactive.*)");
            System.exit(1);
        }

        final String regex = args[0];

        Iterable<WinProcess> allProcess = WinProcess.all();

        for (WinProcess process : allProcess) {
            String commandLine = process.getCommandLine();
            if (commandLine.matches(regex)) {
                System.out.println("Killing " + process.getPid() + " " + process.getCommandLine());
                process.killRecursively();
            }
        }
    }
}
