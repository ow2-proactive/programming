/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.dataspaces.hello;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;


@ActiveObject
public class ExampleProcessing implements Serializable {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    private static final String FINAL_RESULTS_FILENAME = "final_results.txt";
    private static final String PARTIAL_RESULTS_FILENAME = "partial_results.txt";

    /**
     * Returns BufferedWriter of specified file's content.
     * 
     * @param outputFile
     *            file of which content writer is to be returned
     * @return writer of a file's content
     * @throws FileSystemException
     */
    private static OutputStreamWriter getWriter(final DataSpacesFileObject outputFile)
            throws FileSystemException {
        OutputStream os = outputFile.getContent().getOutputStream();
        return new OutputStreamWriter(os);
    }

    /**
     * Returns BufferedReader of specified file's content.
     * 
     * @param inputFile
     *            file of which content reader is to be returned
     * @return reader of a file's content
     * @throws FileSystemException
     */
    private static BufferedReader getReader(final DataSpacesFileObject inputFile) throws FileSystemException {
        final InputStream is = inputFile.getContent().getInputStream();
        return new BufferedReader(new InputStreamReader(is));
    }

    private static void closeResource(Object resource) {
        if (resource == null)
            return;

        try {
            if (resource instanceof Closeable)
                ((Closeable) resource).close();
            else if (resource instanceof DataSpacesFileObject)
                ((DataSpacesFileObject) resource).close();
        } catch (IOException e) {
            ProActiveLogger.logEatedException(logger, e);
        }
    }

    public ExampleProcessing() {
    }

    /**
     * Creates file within AO's scratch and writes content there.
     * 
     * @param fileName
     *            name of a file to create within AO's scratch
     * @param content
     *            file content to write
     * @return URI of written file
     * @throws NotConfiguredException
     *             when scratch data space hasn't been configured
     * @throws IOException
     *             when IO exception occurred during file writing
     * @throws ConfigurationException
     *             when wrong configuration has been provided (capabilities of a FS)
     * 
     */
    // @snippet-start DataSpacesExample_processing1
    public String writeIntoScratchFile(String fileName, String content) throws NotConfiguredException,
            IOException, ConfigurationException {
        DataSpacesFileObject file = null;
        OutputStreamWriter writer = null;

        try {
            // resolve scratch for this AO, and get Data Spaces file representation of fileName;
            // later, be sure that the file was created and open an output stream writer on this file;
            file = PADataSpaces.resolveScratchForAO(fileName);
            file.createFile();
            writer = getWriter(file);
            // finally, write the content and return files URI, valid for every AO
            writer.write(content);

            return file.getVirtualURI();
        } catch (IOException e) {
            logger.error("Exception while IO operation", e);
            throw e;
        } finally {
            closeResource(writer);
            closeResource(file);
        }
    }

    // @snippet-end DataSpacesExample_processing1
    /**
     * Computes number of lines of a document from specified input data space name, and writes
     * partial results into a file in it's scratch. URI of file within AO's scratch data space is
     * returned.
     * 
     * @param inputName
     *            name of input data space containing document to process
     * @return URI of file with partial results
     * @throws SpaceNotFoundException
     *             if specified input data space cannot be resolved
     * @throws NotConfiguredException
     *             this AO's scratch hasn't been configured
     * @throws IOException
     *             when IO exception occurred during writing partial results
     * @throws ConfigurationException
     *             when wrong configuration has been provided (capabilities of a FS)
     */
    // @snippet-start DataSpacesExample_processing2
    public StringWrapper computePartials(String inputName) throws SpaceNotFoundException,
            NotConfiguredException, IOException, ConfigurationException {

        logger.info("Processing input " + inputName);
        DataSpacesFileObject inputFile = null;
        BufferedReader reader = null;
        int lines = 0;

        try {
            // resolve a named input that's name was passed as a method's parameter
            // as input represents file that's content is to be processed, open a reader
            inputFile = PADataSpaces.resolveInput(inputName);
            reader = getReader(inputFile);

            // count lines here..
            while (reader.readLine() != null)
                lines++;

            StringBuffer sb = new StringBuffer();
            sb.append(inputName).append(": ").append(lines).append('\n');

            // store the partial result in a file within AO's scratch
            String fileUri = writeIntoScratchFile(PARTIAL_RESULTS_FILENAME, sb.toString());
            logger.info("partial results written: " + sb.toString());

            // finally return file's URI
            return new StringWrapper(fileUri);
        } catch (IOException e) {
            logger.error("Exception while IO operation", e);
            throw e;
        } finally {
            closeResource(reader);
            closeResource(inputFile);
        }
    }

    // @snippet-end DataSpacesExample_processing2
    /**
     * Gathers all partials results from specified scratches into one output file within output data
     * space.
     * 
     * @param partialResults
     *            iterable instance of file URIs containing partial results
     * @throws MalformedURIException
     *             when any specified URI is not correctly formed
     * @throws DataSpacesException
     *             when resolving default output has failed
     * @throws IOException
     *             when IO exception occurred during writing final results (failures in reading
     *             partial results are ignored)
     */
    // @snippet-start DataSpacesExample_processing3
    public void gatherPartials(Iterable<StringWrapper> partialResults) throws MalformedURIException,
            DataSpacesException, IOException {
        logger.info("Gathering and aggregating partial results");

        final List<String> results = new ArrayList<String>();

        // for every URI that was passed...
        for (StringWrapper uriWrapped : partialResults) {
            DataSpacesFileObject partialResultsFile = null;
            BufferedReader reader = null;
            try {
                // ... resolve file pointed by that URI and open a reader, as it contains partial result
                partialResultsFile = PADataSpaces.resolveFile(uriWrapped.stringValue());
                reader = getReader(partialResultsFile);

                // ... and gather partial results in a list
                results.add(reader.readLine());
            } catch (IOException x) {
                logger.error("Reading one's partial result file failed, trying to continue", x);
            } finally {
                closeResource(reader);
                closeResource(partialResultsFile);
            }
        }

        DataSpacesFileObject outputFile = null;
        OutputStreamWriter writer = null;
        try {
            // resolve a file from the default output space (as such is been defined in the GCM-A);
            // be sure that the file exists and write gathered results using the output stream writer
            outputFile = PADataSpaces.resolveDefaultOutput(FINAL_RESULTS_FILENAME);
            outputFile.createFile();
            writer = getWriter(outputFile);

            for (String line : results)
                if (line != null) {
                    writer.write(line);
                    writer.write('\n');
                }
            logger.info("Results gathered, partial results number: " + results.size());
        } catch (IOException e) {
            logger.error("Exception while IO operation", e);
            throw e;
        } finally {
            closeResource(writer);
            closeResource(outputFile);
        }
    }
    // @snippet-end DataSpacesExample_processing3
}
