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
package functionalTests.activeobject.wrapper;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.FunctionalTest;


/**
 * Test if a futre is created for primitive wrappers
 * Test primitive wrapper for asynchronous call.
 * @author The ProActive Team
 *
 * Created on Jul 28, 2005
 */

public class Test extends FunctionalTest {
    private A ao;

    private BooleanMutableWrapper boolMutable;

    private DoubleMutableWrapper dbleMutable;

    private IntMutableWrapper integerMutable;

    private LongMutableWrapper longNumberMutable;

    private StringMutableWrapper stringMutable;

    private FloatMutableWrapper fltMutable;

    private BooleanWrapper bool;

    private DoubleWrapper dble;

    private IntWrapper integer;

    private LongWrapper longNumber;

    private StringWrapper string;

    private FloatWrapper flt;

    @Before
    public void initTest() throws Exception {
        this.ao = PAActiveObject.newActive(A.class, null);
    }

    @org.junit.Test
    public void action() throws Exception {
        assertTrue(ao != null);

        this.boolMutable = this.ao.testBooleanMutableWrapper();
        this.dbleMutable = this.ao.testDoubleMutableWrapper();
        this.integerMutable = this.ao.testIntMutableWrapper();
        this.longNumberMutable = this.ao.testLongMutableWrapper();
        this.stringMutable = this.ao.testStringMutableWrapper();
        this.fltMutable = this.ao.testFloatMutableWrapper();

        this.bool = this.ao.testBooleanWrapper();
        this.dble = this.ao.testDoubleWrapper();
        this.integer = this.ao.testIntWrapper();
        this.longNumber = this.ao.testLongWrapper();
        this.string = this.ao.testStringWrapper();
        this.flt = this.ao.testFloatWrapper();

        assertTrue(PAFuture.isAwaited(this.boolMutable));
        assertTrue(PAFuture.isAwaited(this.dbleMutable));
        assertTrue(PAFuture.isAwaited(this.integerMutable));
        assertTrue(PAFuture.isAwaited(this.longNumberMutable));
        assertTrue(PAFuture.isAwaited(this.stringMutable));
        assertTrue(PAFuture.isAwaited(this.fltMutable));
        assertTrue(PAFuture.isAwaited(this.bool));
        assertTrue(PAFuture.isAwaited(this.dble));
        assertTrue(PAFuture.isAwaited(this.integer));
        assertTrue(PAFuture.isAwaited(this.longNumber));
        assertTrue(PAFuture.isAwaited(this.string));
        assertTrue(PAFuture.isAwaited(this.flt));
    }

    @After
    public void endTest() throws Exception {
        this.ao.terminate();
        this.ao = null;
    }
}
