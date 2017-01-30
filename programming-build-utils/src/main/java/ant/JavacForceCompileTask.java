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
