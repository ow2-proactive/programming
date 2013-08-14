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
