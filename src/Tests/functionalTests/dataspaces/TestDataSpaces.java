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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;


public class TestDataSpaces extends GCMFunctionalDataSpacesBase {
    private static final String ADDED_INPUT_NAME = "another_input";
    private static final String OUTPUT_FILE_NAME = "some_file.txt";
    private static final String OUTPUT_FILE_CONTENT1 = "didum";
    private static final String OUTPUT_FILE_CONTENT2 = "didum_toto";
    private Node node1;
    private Node node2;
    private Node node3;
    private Node node4;
    private Node nodeLocal;
    private TestActiveObject ao1;
    private TestActiveObject ao1B;
    private TestActiveObject ao2;
    private TestActiveObject ao3;
    private TestActiveObject ao4;
    private TestActiveObject aoLocal;
    private TestActiveObject aoFake;

    public TestDataSpaces() throws URISyntaxException, IOException, ProActiveException {
        super(2, 2);
        super.startDeployment();
    }

    @Before
    public void createTestActiveObjects() throws ActiveObjectCreationException, NodeException {
        node1 = getANode();
        node2 = getANode();
        node3 = getANode();
        node4 = getANode();
        nodeLocal = NodeFactory.getDefaultNode();
        // create AOs on hosts on the same and different runtimes
        ao1 = PAActiveObject.newActive(TestActiveObject.class, null, node1);
        ao1B = PAActiveObject.newActive(TestActiveObject.class, null, node1);
        ao2 = PAActiveObject.newActive(TestActiveObject.class, null, node2);
        ao3 = PAActiveObject.newActive(TestActiveObject.class, null, node3);
        ao4 = PAActiveObject.newActive(TestActiveObject.class, null, node4);
        // AO for default local node
        aoLocal = PAActiveObject.newActive(TestActiveObject.class, null, nodeLocal);
        // non-AO to test behavior for local half-bodies node
        aoFake = new TestActiveObject();
        // no need for @After, as whole GCMApp will be killed
    }

    @Test
    public void action() throws SpaceNotFoundException, NotConfiguredException, IOException,
            MalformedURIException, ProActiveTimeoutException, SpaceAlreadyRegisteredException,
            ConfigurationException, InterruptedException {
        // read inputs:
        assertEquals(INPUT_FILE_CONTENT, ao1.readDefaultInputFile(INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, ao1B.readDefaultInputFile(INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, ao2.readDefaultInputFile(INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, aoLocal.readDefaultInputFile(INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, aoFake.readDefaultInputFile(INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, ao3.readInputFile(INPUT_WITH_DIR_NAME, INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, ao4.readInputFile(INPUT_WITH_DIR_NAME, INPUT_FILE_NAME));
        assertEquals(INPUT_FILE_CONTENT, ao1.readInputFile(INPUT_WITH_FILE_NAME, null));
        assertEquals(INPUT_FILE_CONTENT, ao3.readInputFile(INPUT_WITH_FILE_NAME, null));

        // try to write to input
        try {
            ao1.writeDefaultInputFile(INPUT_FILE_NAME, INPUT_FILE_NAME);
            fail("Unexpectedly we are able to to write to input space");
        } catch (FileSystemException x) {
        }
        assertEquals(INPUT_FILE_CONTENT, ao1.readDefaultInputFile(INPUT_FILE_NAME));

        // write to outputs:
        final String defaultOutputFileUri = ao1
                .writeDefaultOutputFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT1);
        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao1B.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(defaultOutputFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoFake.readFile(defaultOutputFileUri));

        final String outputWithDirFileUri = ao1.writeOutputFile(OUTPUT_WITH_DIR_NAME, OUTPUT_FILE_NAME,
                OUTPUT_FILE_CONTENT1);
        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(outputWithDirFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(outputWithDirFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(outputWithDirFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(outputWithDirFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(outputWithDirFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoFake.readFile(outputWithDirFileUri));

        final String outputWithFileFileUri = ao1.writeOutputFile(OUTPUT_WITH_FILE_NAME, null,
                OUTPUT_FILE_CONTENT1);
        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(outputWithFileFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(outputWithFileFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(outputWithFileFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(outputWithFileFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(outputWithFileFileUri));
        assertEquals(OUTPUT_FILE_CONTENT1, aoFake.readFile(outputWithFileFileUri));

        // TODO the following code tests the automatic creation of a remote root folder of a dataspace when it doesn't automatically exists
        // currently this feature is disabled, because we cannot mount any more a dataspace based on a folder which doesn't exist
        // in order to reenable it, we could add an additional method PADataSpaces like resolveOutputOrCreate which would generate
        // unexisting root folder

        //        final String outputWithNothingFileUri1 = ao1.writeOutputFile(OUTPUT_WITH_NOTHING1_NAME,
        //                OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT1);
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(outputWithNothingFileUri1));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(outputWithNothingFileUri1));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(outputWithNothingFileUri1));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(outputWithNothingFileUri1));
        //        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(outputWithNothingFileUri1));
        //        assertEquals(OUTPUT_FILE_CONTENT1, aoFake.readFile(outputWithNothingFileUri1));
        //
        //        final String outputWithNothingFileUri2 = ao1.writeOutputFile(OUTPUT_WITH_NOTHING2_NAME, null,
        //                OUTPUT_FILE_CONTENT1);
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(outputWithNothingFileUri2));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(outputWithNothingFileUri2));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(outputWithNothingFileUri2));
        //        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(outputWithNothingFileUri2));
        //        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(outputWithNothingFileUri2));
        //        assertEquals(OUTPUT_FILE_CONTENT1, aoFake.readFile(outputWithNothingFileUri2));

        // write to scratches:
        final String scratchFileUri1 = ao1.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT1);
        final String scratchFileUri2 = ao2.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT2);
        final String scratchFileUri3 = ao3.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT1);
        final String scratchFileUri4 = ao4.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT2);
        final String scratchFileUriLocal = aoLocal.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT1);
        final String scratchFileUriFake = aoFake.writeScratchFile(OUTPUT_FILE_NAME, OUTPUT_FILE_CONTENT2);

        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(scratchFileUri1));
        assertEquals(OUTPUT_FILE_CONTENT1, ao1B.readFile(scratchFileUri1));
        assertEquals(OUTPUT_FILE_CONTENT1, ao2.readFile(scratchFileUri1));
        assertEquals(OUTPUT_FILE_CONTENT1, ao3.readFile(scratchFileUri1));
        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(scratchFileUri1));

        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri2));

        assertEquals(OUTPUT_FILE_CONTENT1, ao1.readFile(scratchFileUri3));

        assertEquals(OUTPUT_FILE_CONTENT2, ao3.readFile(scratchFileUri4));

        assertEquals(OUTPUT_FILE_CONTENT1, ao4.readFile(scratchFileUriLocal));
        assertEquals(OUTPUT_FILE_CONTENT1, aoLocal.readFile(scratchFileUriLocal));

        assertEquals(OUTPUT_FILE_CONTENT2, ao3.readFile(scratchFileUriFake));
        assertEquals(OUTPUT_FILE_CONTENT2, aoLocal.readFile(scratchFileUriFake));

        // write to AO's scratch by URI:
        ao1.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT2);
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        // try to write to other AO's scratch by URI:
        try {
            ao1B.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT1);
            fail("Unexpectedly AO from the same Node is able to write other AO's scratch");
        } catch (FileSystemException x) {
        }
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        try {
            ao2.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT1);
            fail("Unexpectedly AO from the same Runtime is able to write other AO's scratch");
        } catch (FileSystemException x) {
        }
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        try {
            ao3.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT1);
            fail("Unexpectedly AO from different Runtime is able to write other AO's scratch");
        } catch (FileSystemException x) {
        }
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        try {
            aoLocal.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT1);
            fail("Unexpectedly AO from default local node is able to write other AO's scratch");
        } catch (FileSystemException x) {
        }
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        try {
            aoFake.writeFile(scratchFileUri1, OUTPUT_FILE_CONTENT1);
            fail("Unexpectedly AO from default local node is able to write other AO's scratch");
        } catch (FileSystemException x) {
        }
        assertEquals(OUTPUT_FILE_CONTENT2, ao1.readFile(scratchFileUri1));

        // read inputs names:
        final HashSet<String> expectedInputs = new HashSet<String>();
        expectedInputs.add(PADataSpaces.DEFAULT_IN_OUT_NAME);
        expectedInputs.add(INPUT_WITH_DIR_NAME);
        expectedInputs.add(INPUT_WITH_FILE_NAME);
        assertEquals(expectedInputs, ao1.getAllKnownInputsNames());
        assertEquals(expectedInputs, ao3.getAllKnownInputsNames());
        assertEquals(expectedInputs, aoLocal.getAllKnownInputsNames());
        assertEquals(expectedInputs, aoFake.getAllKnownInputsNames());

        // read outputs names:
        final HashSet<String> expectedOutputs = new HashSet<String>();
        expectedOutputs.add(PADataSpaces.DEFAULT_IN_OUT_NAME);
        expectedOutputs.add(OUTPUT_WITH_DIR_NAME);
        expectedOutputs.add(OUTPUT_WITH_FILE_NAME);
        expectedOutputs.add(OUTPUT_WITH_NOTHING1_NAME);
        expectedOutputs.add(OUTPUT_WITH_NOTHING2_NAME);
        assertEquals(expectedOutputs, ao1.getAllKnownOutputsNames());
        assertEquals(expectedOutputs, ao3.getAllKnownOutputsNames());
        assertEquals(expectedOutputs, aoLocal.getAllKnownOutputsNames());
        assertEquals(expectedOutputs, aoFake.getAllKnownOutputsNames());

        // blocking resolve input + add input:
        // (exceptions for readInputFileBlocking - to make it asynchronous)
        PAException.tryWithCatch(new Class[] { SpaceNotFoundException.class, NotConfiguredException.class,
                IOException.class, ProActiveTimeoutException.class });
        try {
            final StringWrapper contentWrapper1B = ao1B.readInputFileBlocking(ADDED_INPUT_NAME,
                    INPUT_FILE_NAME, 30000);
            final StringWrapper contentWrapper2 = ao2.readInputFileBlocking(ADDED_INPUT_NAME,
                    INPUT_FILE_NAME, 30000);
            final StringWrapper contentWrapper4 = ao4.readInputFileBlocking(ADDED_INPUT_NAME,
                    INPUT_FILE_NAME, 30000);
            final StringWrapper contentWrapperLocal = aoLocal.readInputFileBlocking(ADDED_INPUT_NAME,
                    INPUT_FILE_NAME, 30000);
            final StringBuilder contentWrapperFake = new StringBuilder();
            new Thread() {
                public void run() {
                    try {
                        contentWrapperFake.append(aoFake.readInputFileBlocking(ADDED_INPUT_NAME,
                                INPUT_FILE_NAME, 30000));
                        synchronized (contentWrapperFake) {
                            contentWrapperFake.notifyAll();
                        }
                    } catch (Exception x) {
                        fail("Could not read added input space");
                    }
                };
            }.start();

            // give them a while to try without success (to let them ask one again for that space) 
            Thread.sleep(1000);
            ao1.addInputSpace(ADDED_INPUT_NAME, getRootSubdirURL(inputWithDirLocalHandle));

            assertEquals(INPUT_FILE_CONTENT, contentWrapper1B.getStringValue());
            assertEquals(INPUT_FILE_CONTENT, contentWrapper2.getStringValue());
            assertEquals(INPUT_FILE_CONTENT, contentWrapper4.getStringValue());
            assertEquals(INPUT_FILE_CONTENT, contentWrapperLocal.getStringValue());
            synchronized (contentWrapperFake) {
                if (contentWrapperFake.length() == 0) {
                    contentWrapperFake.wait(29000);
                }
            }
            assertEquals(INPUT_FILE_CONTENT, contentWrapperFake.toString());
            PAException.endTryWithCatch();
        } finally {
            PAException.removeTryWithCatch();
        }
        expectedInputs.add(ADDED_INPUT_NAME);
        assertEquals(expectedInputs, ao1.getAllKnownInputsNames());
        assertEquals(expectedInputs, ao1B.getAllKnownInputsNames());
        assertEquals(expectedInputs, ao2.getAllKnownInputsNames());
        assertEquals(expectedInputs, ao3.getAllKnownInputsNames());
        assertEquals(expectedInputs, ao4.getAllKnownInputsNames());
        assertEquals(expectedInputs, aoLocal.getAllKnownInputsNames());
        assertEquals(expectedInputs, aoFake.getAllKnownInputsNames());

        // do something on DataSpacesFileObject...
        // check getURI()
        assertEquals(scratchFileUri1, ao1.getFileURI(scratchFileUri1));

        final String scratchURI = ao1.getParentURI(scratchFileUri1);

        // children listing
        final List<String> children = ao1.getChildrenURIs(scratchURI);
        assertEquals(1, children.size());
        assertEquals(scratchFileUri1, children.get(0));

        // check disallowed get parent requests
        try {
            ao1.getParentURI(scratchURI);
            fail("Unexpectedly AO is able to access parent of a scratch");
        } catch (FileSystemException x) {
        }

        final String defaultOutputURI = ao1.getParentURI(defaultOutputFileUri);
        try {
            ao1.getParentURI(defaultOutputURI);
            fail("Unexpectedly AO is able to access parent of an output space");
        } catch (FileSystemException x) {
        }

        // TODO: perform more operations on DataSpacesFileObject
    }

    @ActiveObject
    public static class TestActiveObject implements Serializable {
        /**
         * 
         */

        private static String readAndCloseFile(final DataSpacesFileObject fo) throws FileSystemException,
                IOException {
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(fo.getContent()
                        .getInputStream()));
                try {
                    return reader.readLine();
                } finally {
                    reader.close();
                }
            } finally {
                fo.close();
            }
        }

        private static String writeAndCloseFile(final DataSpacesFileObject fo, String content)
                throws FileSystemException, IOException {
            try {
                final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fo.getContent()
                        .getOutputStream()));
                try {
                    writer.write(content);
                    return fo.getVirtualURI();
                } finally {
                    writer.close();
                }
            } finally {
                fo.close();
            }
        }

        public TestActiveObject() {
        }

        public String readDefaultInputFile(String fileName) throws SpaceNotFoundException,
                NotConfiguredException, IOException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveDefaultInput(fileName);
            return readAndCloseFile(fo);
        }

        public String writeDefaultInputFile(String fileName, String content) throws FileSystemException,
                IOException, SpaceNotFoundException, NotConfiguredException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveDefaultInput(fileName);
            return writeAndCloseFile(fo, content);
        }

        public String readInputFile(String inputName, String fileName) throws SpaceNotFoundException,
                NotConfiguredException, IOException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveInput(inputName, fileName);
            return readAndCloseFile(fo);
        }

        public String readFile(String uri) throws SpaceNotFoundException, NotConfiguredException,
                IOException, MalformedURIException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveFile(uri);
            return readAndCloseFile(fo);
        }

        public String writeDefaultOutputFile(String fileName, String content) throws FileSystemException,
                IOException, SpaceNotFoundException, NotConfiguredException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveDefaultOutput(fileName);
            return writeAndCloseFile(fo, content);
        }

        public String writeOutputFile(String outputName, String fileName, String content)
                throws FileSystemException, IOException, SpaceNotFoundException, NotConfiguredException,
                ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveOutput(outputName, fileName);
            return writeAndCloseFile(fo, content);
        }

        public String writeScratchFile(String fileName, String content) throws FileSystemException,
                IOException, SpaceNotFoundException, NotConfiguredException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveScratchForAO(fileName);
            return writeAndCloseFile(fo, content);
        }

        public String writeFile(String uri, String content) throws FileSystemException, IOException,
                SpaceNotFoundException, NotConfiguredException, MalformedURIException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveFile(uri);
            return writeAndCloseFile(fo, content);
        }

        public Set<String> getAllKnownInputsNames() throws NotConfiguredException {
            return PADataSpaces.getAllKnownInputNames();
        }

        public Set<String> getAllKnownOutputsNames() throws NotConfiguredException {
            return PADataSpaces.getAllKnownOutputNames();
        }

        public StringWrapper readInputFileBlocking(String inputName, String fileName, long timeout)
                throws SpaceNotFoundException, NotConfiguredException, IOException,
                ProActiveTimeoutException, ConfigurationException {
            final DataSpacesFileObject fo = PADataSpaces.resolveInputBlocking(inputName, fileName, timeout);
            return new StringWrapper(readAndCloseFile(fo));
        }

        public void addInputSpace(String inputName, String url) throws SpaceAlreadyRegisteredException,
                ConfigurationException, NotConfiguredException {
            PADataSpaces.addInput(inputName, url, null);
        }

        public String getFileURI(String uri) throws MalformedURIException, SpaceNotFoundException,
                NotConfiguredException, ConfigurationException, FileSystemException {
            return PADataSpaces.resolveFile(uri).getVirtualURI();
        }

        public String getParentURI(String uri) throws MalformedURIException, SpaceNotFoundException,
                NotConfiguredException, ConfigurationException, FileSystemException {
            return PADataSpaces.resolveFile(uri).getParent().getVirtualURI();
        }

        public List<String> getChildrenURIs(String uri) throws MalformedURIException, SpaceNotFoundException,
                NotConfiguredException, ConfigurationException, FileSystemException {
            final List<String> result = new ArrayList<String>();
            for (final DataSpacesFileObject child : PADataSpaces.resolveFile(uri).getChildren()) {
                result.add(child.getVirtualURI());
            }
            return result;
        }
    }

}
