/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.vfsprovider;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.StreamMode;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;

import unitTests.vfsprovider.AbstractIOOperationsBase;


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
