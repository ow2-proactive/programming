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
package windowscleaner;

import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;


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
            try {
                String commandLine = process.getCommandLine();
                // System.out.println("COMMAND LINE: " + commandLine);
                if (commandLine.matches(".*" + WindowsCleaner.class.getName() + ".*")) {
                    // System.out.println(process.getPid() + " is a Windows Cleaner");
                } else {
                    if (commandLine.matches(regex)) {
                        // System.out.println(process.getPid() + " matches " + regex);
                        process.killRecursively();
                        System.out.println("Killed " + process.getPid() + " " + commandLine);
                    } else {
                        // System.out.println(process.getPid() + " do not match " + regex);
                    }
                }
            } catch (WinpException e) {
                // Miam Miam Miam
            }
        }
    }
}
