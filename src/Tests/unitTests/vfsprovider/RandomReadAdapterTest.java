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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package unitTests.vfsprovider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.RandomAccessStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


public class RandomReadAdapterTest extends AbstractStreamBase {

    @Override
    protected Stream getInstance(File f) throws Exception {
        return RandomAccessStreamAdapter.createRandomAccessRead(f);
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void writeTest() throws IOException, WrongStreamTypeException {
        super.writeTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void flushTest() throws IOException, WrongStreamTypeException {
        super.flushTest();
    }

    @Override
    @Test(expected = FileNotFoundException.class)
    public void createFromNotExistingFileTest() throws Exception {
        super.createFromNotExistingFileTest();
    }

    @Override
    protected long changePosition(Stream s) throws Exception {
        s.seek(10);
        return 10;
    }
}
