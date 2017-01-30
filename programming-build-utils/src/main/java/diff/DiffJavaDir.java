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
package diff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DiffJavaDir {

    /**
     * Perform an unified diff between to directory.
     * Only Java file are compared
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            usageAndExit();
        }

        File dir1 = new File(args[0]);
        File dir2 = new File(args[1]);

        if (!directoryExist(dir1) || !directoryExist(dir2)) {
            System.exit(1);
        }

        List<File> files = exploreDirectory(dir1);
        boolean diffFound = false;

        for (File file : files) {
            String file2 = file.toString().replaceFirst(dir1.toString(), dir2.toString());
            if (!new File(file2).exists()) {
                System.err.println(file2 + " does not exist in " + dir2);
                diffFound = true;
                continue;
            }

            if (DiffPrint.printUnifiedDiff(file.toString(), file2)) {
                diffFound = true;
            }
        }

        if (diffFound) {
            System.exit(1);
        }
    }

    static private List<File> exploreDirectory(File dir) {
        List<File> files = new ArrayList<File>();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(exploreDirectory(file));
            }

            if (!file.toString().endsWith(".java")) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    private static boolean directoryExist(File dir) {
        if (!dir.exists()) {
            return false;
        }
        if (!dir.isDirectory()) {
            return false;
        }
        if (!dir.canRead()) {
            return false;
        }

        return true;
    }

    private static void usageAndExit() {
        System.err.println("Usage:");
        System.err.println("\tcommand dir1 dir2");
        System.exit(2);
    }
}
