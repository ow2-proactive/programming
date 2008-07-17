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

    private static String PatternBegin = "$$%%";
    private static String PatternEnd = "%%$$";
    private static String VersionPattern = "ProActiveVersion";
    private static String VersionUnderscorePattern = "ProActiveUnderscoreVersion";
    private static String CopyrightYearsPattern = "CopyrightYears";
    private static String CopyrightYearsShortPattern = "CopyrightYearsShort";
    private static String LastYearCopyrightPattern = "CopyrightLastYear";
    private static String LastYearShortCopyrightPattern = "CopyrightLastYearShort";
    private static String FirstYearCopyrightPattern = "CopyrightFirstYear";
    private static String FirstYearShortCopyrightPattern = "CopyrightFirstYearShort";
    private static String CurrentVersion = "3.2.1";
    private static String CurrentVersionUnderscore = "3_2_1";
    private static String CopyrightYears = "1997-2007";
    private static String CopyrightYearsShort = "97-07";
    private static String LastYearCopyright = "2007";
    private static String LastYearShortCopyright = "07";
    private static String FirstYearCopyright = "1997";
    private static String FirstYearShortCopyright = "97";
    private static String[][] replacements = { { VersionPattern, CurrentVersion },
            { VersionUnderscorePattern, CurrentVersionUnderscore },
            { CopyrightYearsPattern, CopyrightYears }, { CopyrightYearsShortPattern, CopyrightYearsShort },
            { LastYearCopyrightPattern, LastYearCopyright },
            { LastYearShortCopyrightPattern, LastYearShortCopyright },
            { FirstYearCopyrightPattern, FirstYearCopyright },
            { FirstYearShortCopyrightPattern, FirstYearShortCopyright } };
    private static URI rootDir;
    private static File[] excludeDirs;
    private static int stats_proactive = 0;
    private static int stats_activeeon = 0;
    private static int stats_activeeon_contrib = 0;
    private static boolean update_mode;

    public static void main(String[] arg) throws java.io.IOException {

	// load copyrights
	ActiveEonGPLcopyright = new String(getBytesFromInputStream(UpdateCopyrightAndVersion.class.getResourceAsStream("activeeon_initialdev.txt")));
	ActiveEonContributorsProActiveInitialGPLcopyright = new String( getBytesFromInputStream(UpdateCopyrightAndVersion.class.getResourceAsStream("activeeon_contrib.txt")));
	GPLcopyright = new String(getBytesFromInputStream(UpdateCopyrightAndVersion.class.getResourceAsStream("proactive_gpl.txt")));


	 update_mode = "true".equals(System.getProperty("copyright.update"));
	 if (update_mode) {
             System.out.println("Coryright Update mode on");
          }

	 System.out.println(System.getProperty("copyright.update"));
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

        // it is possible to find a snippet tag between a copyright and the package name
        // in that case we keep the snippet.
        int snippetStart = program.indexOf("//@" + "snippet-start");

        if (packageStart == -1) {
            return;
        }

        if ((snippetStart != -1) && (snippetStart < packageStart)) {
            packageStart = snippetStart;
        }
        int choice = 4;
        String copyrightInFile = program.substring(0, packageStart);

        if (copyrightInFile.contains("Copyright") &&
            (copyrightInFile.contains("The ProActive Team") || copyrightInFile.contains("ActiveEon Team"))) {

            String copyright = null;

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
                    System.out.println("Skipping " + file + ", activeeon contributor exists.");
                    stats_activeeon_contrib++;
                    choice = 3;
                } else {
                    System.out.println("Skipping " + file + ", proactive initial exists.");
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
            System.out.println("----> " + choice);
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
            case 4:
		copyrightedProgram = new String(copyrightInFile) + uncopyrightedProgram;
        }

        update_copyright_in_file(file,copyrightedProgram.getBytes());
    }

    public static void update_copyright_in_file(File file, byte[] b) throws IOException {
         file.delete();
         java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file));
         out.write(b, 0, b.length);
         out.flush();
         out.close();

    }


    private static void patternReplacementsInFile(java.io.File file) throws java.io.IOException {
        String name = file.getName();

        // selection of files where patterns will be found
        if (name.endsWith(".java") || name.endsWith(".xml") || name.endsWith(".xsl") ||
            name.endsWith(".xslt") || name.endsWith(".html") || name.endsWith(".htm")) {
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
            String filetext = new String(b);

            file.delete();

            java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file));

            String currentText = filetext;
            filetext = null;
            int indexoffirstword = -1;
            boolean anyReplacement = false;

            do {
                indexoffirstword = -1;

                // index of the first pattern which has been found or the end of the file
                int lowestindex = currentText.length();

                // we try to locate each pattern in the replacements list 
                for (int i = 0; i < replacements.length; i++) {
                    int indexfound = currentText.indexOf(PatternBegin + replacements[i][0] + PatternEnd);

                    if ((indexfound != -1) && (indexfound < lowestindex)) {
                        // we save the pattern which happens first
                        indexoffirstword = i;
                        lowestindex = indexfound;
                        anyReplacement = true;
                    }
                }

                String toWrite = currentText.substring(0, lowestindex);

                // we writes what's before the first pattern
                b = toWrite.getBytes();
                out.write(b, 0, b.length);
                out.flush();

                // we replace the pattern found
                if (indexoffirstword != -1) {
                    b = replacements[indexoffirstword][1].getBytes();
                    out.write(b, 0, b.length);
                    out.flush();

                    // we skip the pattern in the source
                    currentText = currentText.substring(lowestindex + PatternBegin.length() +
                        replacements[indexoffirstword][0].length() + PatternEnd.length());
                }
                // if there are still more patterns, go on
            } while (indexoffirstword != -1);

            out.close();
            if (anyReplacement) {
                System.out.println("Patterns replaced in " + file);
            }
        }
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
                patternReplacementsInFile(fileItem);
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
