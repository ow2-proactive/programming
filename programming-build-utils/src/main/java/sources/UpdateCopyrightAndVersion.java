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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package sources;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UpdateCopyrightAndVersion {

    private static String GPLcopyright;
    private static String ActiveEonGPLcopyright;
    private static String ActiveEonContributorsProActiveInitialGPLcopyright;
    private static URI rootDir;
    private static File[] excludeDirs;
    private static int stats_proactive = 0;
    private static int stats_activeeon = 0;
    private static int stats_activeeon_contrib = 0;
    private static boolean update_mode;

    public static void main(String[] arg) throws java.io.IOException {

        // load copyrights 
        ActiveEonGPLcopyright = new String(
            getBytesFromInputStream(UpdateCopyrightAndVersion.class
                    .getResourceAsStream("activeeon_initialdev.txt")));
        GPLcopyright = new String(
            getBytesFromInputStream(UpdateCopyrightAndVersion.class.getResourceAsStream("proactive_gpl.txt")));
        ActiveEonContributorsProActiveInitialGPLcopyright = new String(
            getBytesFromInputStream(UpdateCopyrightAndVersion.class
                    .getResourceAsStream("activeeon_contrib.txt")));

        update_mode = "true".equals(System.getProperty("copyright.update"));
        if (update_mode) {
            System.out.println("Coryright Update mode on");
        }

        java.io.File sourceDir = new java.io.File(arg[0]);
        rootDir = sourceDir.toURI();

        excludeDirs = new File[0];
        if (arg.length > 1) {
            excludeDirs = new File[arg.length - 1];

            // we retrieve the exclusion patterns
            for (int i = 1; i < arg.length; i++) {

                URI uriexclude = new File(arg[i]).toURI();
                if (!uriexclude.isAbsolute()) {
                    excludeDirs[i - 1] = new File(rootDir.resolve(uriexclude));
                } else {
                    excludeDirs[i - 1] = new File(arg[i]);
                }
            }
        }

        addCopyrightToDir(sourceDir);

        System.out.println("Stats :\nProActive Initial Dev. Copyright = " + stats_proactive +
            "\nActiveEon Initial Dev Copyright = " + stats_activeeon + "\nActiveEon Contributor = " +
            stats_activeeon_contrib);
    }

    private static void addCopyrightToFile(java.io.File file) throws java.io.IOException {
        String name = file.getName();

        if (!name.endsWith(".java")) {
            return;
        }

        byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
        String program = new String(b);

        //        if(program.indexOf("Copyright (C)")!= -1){
        //        	return;
        //        }
        int packageStart = program.indexOf("package");

        // it is possible to find a snippet or a tutorial tag between a copyright and the package name
        // in that case we keep the snippet/tutorial.
        Pattern pattern = Pattern.compile("\\/\\/\\s*\\@snippet-start");
        Matcher matcher = pattern.matcher(program);
        int snippetStart = -1;
        if (matcher.find()) {
            snippetStart = matcher.start();
        }
        pattern = Pattern.compile("\\/\\/\\s*\\@tutorial-start");
        matcher = pattern.matcher(program);
        int tutorialStart = -1;
        if (matcher.find()) {
            tutorialStart = matcher.start();
        }

        // No annotation can be placed before copyright.
        // Otherwise, Copyright cannot be updated.
        int copyrightIndex = program.indexOf("Copyright");
        if (copyrightIndex != -1 && snippetStart < copyrightIndex)
            snippetStart = -1;

        if (copyrightIndex != -1 && tutorialStart < copyrightIndex)
            tutorialStart = -1;

        int annotationStart;
        if (snippetStart != -1) {
            annotationStart = (tutorialStart != -1) ? Math.min(snippetStart, tutorialStart) : snippetStart;
        } else {
            annotationStart = (tutorialStart != -1) ? tutorialStart : -1;
        }

        if (packageStart == -1) {
            return;
        }

        if ((annotationStart != -1) && (annotationStart < packageStart)) {
            packageStart = annotationStart;
        }
        int choice = 4;
        String copyrightInFile = program.substring(0, packageStart);

        if (copyrightInFile.contains("Copyright") &&
            (copyrightInFile.contains("The ProActive Team") || copyrightInFile.contains("ActiveEon Team"))) {

            Pattern p = Pattern.compile("^.*Initial deve.*ActiveEon.*$", Pattern.MULTILINE |
                Pattern.UNIX_LINES);
            Matcher m = p.matcher(copyrightInFile);
            boolean bool = m.find();
            if (bool) {
                System.out.println("Skipping " + file + ", activeeon initial exists.");
                stats_activeeon++;
                choice = 2;
            }

            p = Pattern.compile("^.*Initial deve.*ProActive.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
            m = p.matcher(copyrightInFile);
            bool = m.find();
            if (bool) {

                p = Pattern.compile("^.*Contributor.*ActiveEon.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
                m = p.matcher(copyrightInFile);
                bool = m.find();
                if (bool) {
                    stats_activeeon_contrib++;
                    choice = 3;
                } else {
                    stats_proactive++;
                    choice = 1;
                }
            }
            if (!update_mode) {
                return;
            }
        }

        System.out.println("Processing " + file);

        if (!update_mode) {

            Scanner in = new Scanner(System.in);

            do {
                System.out
                        .println("Which licence to apply ? : 1/ ProActive -- 2/ ActiveEon  -- 3/ ActiveEon as contr. ? -- 4 / skip:");

                //String line = in.nextLine();
                choice = in.nextInt();

            } while (!((choice > 0) && (choice < 5)));

            in.close();

        }

        String uncopyrightedProgram = program.substring(packageStart);
        String copyrightedProgram = uncopyrightedProgram;
        switch (choice) {
            case 1:
                copyrightedProgram = GPLcopyright + uncopyrightedProgram;
                break;
            case 2:
                copyrightedProgram = ActiveEonGPLcopyright + uncopyrightedProgram;
                break;
            case 3:
                copyrightedProgram = ActiveEonContributorsProActiveInitialGPLcopyright + uncopyrightedProgram;
                break;
            case 4:
                copyrightedProgram = new String(copyrightInFile) + uncopyrightedProgram;
        }

        update_copyright_in_file(file, copyrightedProgram.getBytes());
    }

    public static void update_copyright_in_file(File file, byte[] b) throws IOException {
        file.delete();
        java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file));
        out.write(b, 0, b.length);
        out.flush();
        out.close();

    }

    private static void addCopyrightToDir(java.io.File file) throws java.io.IOException {
        for (File exclude : excludeDirs) {
            if (file.equals(exclude)) {
                return;
            }
        }

        java.io.File[] listFiles = file.listFiles();

        if (listFiles == null) {
            return;
        }

        for (int i = 0; i < listFiles.length; i++) {
            java.io.File fileItem = listFiles[i];

            if (fileItem.isDirectory()) {
                if (!fileItem.getName().equals(".svn")) {
                    addCopyrightToDir(fileItem);
                }
            } else {
                addCopyrightToFile(fileItem);
            }
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in) throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];

        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }

        return bytecodes;
    }
}
