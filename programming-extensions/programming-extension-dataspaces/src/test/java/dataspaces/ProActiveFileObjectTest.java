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
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileObject;


/**
 * This test checks finding files for ProActiveFileObject
 * It may be improved later on to test other ProActiveFileObject features
 * @author ActiveEon Team
 * @since 10/07/2017
 */
public class ProActiveFileObjectTest {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    static File createdFolder;

    static final int NB_FILES_PATTERN = 5000;

    static final String FOLDER_PATTERN = "findFiles/subfolder";

    static final String DEPTH_FOLDER_PATTERN = "findFiles";

    static FileSystemServerDeployer deployer;

    static DefaultFileSystemManager manager;

    static String proActiveFSURL;

    @BeforeClass
    public static void setUp() throws IOException {

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getLogger(Loggers.VFS_PROVIDER_SERVER).setLevel(Level.TRACE);
        createdFolder = tempFolder.newFolder();
        deployer = new FileSystemServerDeployer(createdFolder.getAbsolutePath(), false);
        for (String url : deployer.getVFSRootURLs()) {
            if (!url.startsWith("file:")) {
                proActiveFSURL = url;
            }
        }
        manager = VFSFactory.createDefaultFileSystemManager();

        generateFiles();
    }

    private static void generateFiles() throws IOException {
        int nbFiles = NB_FILES_PATTERN;
        File patternFindFilesFolder = new File(createdFolder, FOLDER_PATTERN);
        patternFindFilesFolder.mkdirs();

        File patternFindFilesDepthFolder = new File(createdFolder, DEPTH_FOLDER_PATTERN);
        patternFindFilesDepthFolder.mkdirs();
        for (int i = 0; i < nbFiles; i++) {
            File fileToFindWithPatternTest = new File(patternFindFilesFolder, "file_" + i + ".in");
            fileToFindWithPatternTest.createNewFile();

            File fileToFindWithPatternDepthTest = new File(patternFindFilesDepthFolder, "file_" + i + ".in");
            fileToFindWithPatternDepthTest.createNewFile();
        }
    }

    @Test
    public void testFindFilesWithPattern() throws IOException {
        FileObject rootFO = manager.resolveFile(proActiveFSURL);
        FileObject[] answer = rootFO.findFiles(new FileSelector(FOLDER_PATTERN + "/*.in"));
        assertEquals(NB_FILES_PATTERN, answer.length);
    }

    @Test
    public void testFindFilesWithPatternAndDepth() throws IOException {
        FileObject rootFO = manager.resolveFile(proActiveFSURL);
        FileObject[] answer = rootFO.findFiles(new PatternDepthFileSelector(0, 2, "**/*.in"));
        assertEquals(NB_FILES_PATTERN, answer.length);
    }

    @AfterClass
    public static void clean() throws Exception {
        manager.close();
        deployer.terminate();
    }
}
