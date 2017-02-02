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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


/**
 * This task can be used to merge the META-INF/services/* declared in each sourcepath.
 * 
 * Each ProActive source path declares its owns service providers in a META-INF directory
 * located at the root of the source path. When building the ProActive.jar we need to merge
 * theses declarations into a single META-INF directory.
 * 
 * @since ProActive 4.3.0
 */
public class ServiceMerger extends Task {
    /** The path of the final META-INF directory */
    private Path destdir;

    /** The service declarations to be merged */
    private List<FileSet> filesets = new LinkedList<FileSet>();

    public void setDestdir(Path path) {
        this.destdir = path;
    }

    public void addFileset(FileSet fileset) {
        this.filesets.add(fileset);
    }

    @Override
    public void execute() throws BuildException {
        // Construct a map:  Service -> {Files}
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (FileSet fileset : this.filesets) {
            DirectoryScanner ds = fileset.getDirectoryScanner(this.getProject());
            String[] includedFiles = ds.getIncludedFiles();
            for (String s : includedFiles) {
                int lastIndex = s.lastIndexOf(File.separator);
                String filename = s.substring(lastIndex, s.length());

                List<String> list = map.get(filename);
                if (list == null) {
                    list = new LinkedList<String>();
                    map.put(filename, list);
                }

                list.add(ds.getBasedir() + File.separator + s);
            }
        }

        // Check the destdir is ok
        File dir = new File(destdir.toString());
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new BuildException("destdir " + destdir + " exists but is not a directory");
            }
        } else {
            if (!dir.mkdirs()) {
                throw new BuildException("Failed to create " + dir);
            }
        }

        // Fill the destdir
        for (String service : map.keySet()) {
            File f = new File(destdir + File.separator + service);

            try {
                FileWriter fw = new FileWriter(f, false);

                for (String file : map.get(service)) {
                    try {
                        FileReader fr = new FileReader(new File(file));
                        BufferedReader br = new BufferedReader(fr);

                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                            fw.write(line + "\n");
                        }
                        br.close();
                        fr.close();
                    } catch (FileNotFoundException e) {
                        throw new BuildException("Failed to merge the services", e);
                    } catch (IOException e) {
                        throw new BuildException("Failed to merge the services", e);
                    }
                }

                fw.close();
            } catch (IOException e1) {
                throw new BuildException("Failed to merge the services", e1);
            }
        }
    }

    @Override
    public void init() throws BuildException {
        super.init();
    }

    @Override
    public void reconfigure() {
        super.reconfigure();
    }

}
