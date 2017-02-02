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
package vfsprovider;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.StreamMode;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;


/**
 * Simple usage test.
 */
public class TestFileSystemServer extends AbstractIOOperationsBase {

    @Override
    public String getTestDirFilename() {
        return "PROACTIVE-FileSystemServerFunctionalTest";
    }

    @Test
    public void test() throws IOException, StreamNotFoundException, WrongStreamTypeException {
        FileSystemServerImpl server = new FileSystemServerImpl(testDir.getAbsolutePath());
        server.startAutoClosing();
        final String path = "/" + TEST_FILENAME;
        final long stream = server.streamOpen(path, StreamMode.SEQUENTIAL_READ);
        final int len = (int) server.fileGetInfo(path).getSize() % Integer.MAX_VALUE;
        final byte[] content = server.streamRead(stream, len);
        Assert.assertArrayEquals(TEST_FILE_CONTENT.getBytes(), content);
        server.streamClose(stream);
        server.stopServer();
    }
}
