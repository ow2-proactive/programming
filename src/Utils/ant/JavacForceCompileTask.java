/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Javac;


/**
 * This class extends the behaviour of the ant builtin javac task
 * in order to start a compilation process even if the input files 
 * have not been modified
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class JavacForceCompileTask extends Javac {

    /**
     * the input files will be in the (protected) filed compileList
     */
    private void getInputFiles() {
        checkParameters();
        resetFileLists();

        // scan source directories and dest directory to build up
        // compile lists
        String[] list = getSrcdir().list();
        for (int i = 0; i < list.length; i++) {
            File srcDir = getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir \"" + srcDir.getPath() + "\" does not exist!", getLocation());
            }

            DirectoryScanner ds = this.getDirectoryScanner(srcDir);
            String[] files = ds.getIncludedFiles();

            scanDir(srcDir, srcDir, files);
        }
    }

    /**
     * Set modification time for all input files at current time
     * this will determine javac to actually compile the input files... 
     */
    private void touch() {
        getInputFiles();
        for (File inputFile : compileList) {
            inputFile.setLastModified(System.currentTimeMillis());
        }
    }

    @Override
    public void execute() throws BuildException {

        touch();
        super.execute();

    }

}
