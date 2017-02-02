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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.OutputStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


public class OutputStreamAdapterTest extends AbstractStreamBase {

    @Override
    protected Stream getInstance(File f) throws Exception {
        return new OutputStreamAdapter(f, false);
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTest() throws IOException, WrongStreamTypeException {
        super.skipTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTestMore() throws IOException, WrongStreamTypeException {
        super.skipTestMore();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void skipTestZero() throws IOException, WrongStreamTypeException {
        super.skipTestZero();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readMoreTest() throws IOException, WrongStreamTypeException {
        super.readMoreTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readTest() throws IOException, WrongStreamTypeException {
        super.readTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void readZeroTest() throws IOException, WrongStreamTypeException {
        super.readZeroTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthTest() throws IOException, WrongStreamTypeException {
        super.getLengthTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthAfterChange() throws Exception {
        super.getLengthAfterChange();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getPositionTest() throws Exception {
        super.getPositionTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void seekTest() throws IOException, WrongStreamTypeException {
        super.seekTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void seekAndGetLengthTest() throws IOException, WrongStreamTypeException {
        super.seekAndGetLengthTest();
    }
}
