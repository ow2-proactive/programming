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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.InputStreamAdapter;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


/**
 * Test suite for {@link InputStreamAdapter}. Redefines those tests, that are not supported by this
 * adapter.
 */
public class InputStreamAdapterTest extends AbstractStreamBase {

    @Override
    @Test(expected = FileNotFoundException.class)
    public void createFromNotExistingFileTest() throws Exception {
        super.createFromNotExistingFileTest();
    }

    @Override
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthTest() throws IOException, WrongStreamTypeException {
        super.getLengthTest();
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
    @Test(expected = WrongStreamTypeException.class)
    public void getLengthAfterChange() throws Exception {
        super.getLengthAfterChange();
    }

    @Override
    protected Stream getInstance(File f) throws Exception {
        return new InputStreamAdapter(f);
    }
}
