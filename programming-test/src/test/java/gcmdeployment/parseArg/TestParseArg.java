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
package gcmdeployment.parseArg;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderHelper;


/**
 * TestParseArg
 *
 * @author The ProActive Team
 */
public class TestParseArg {

    String[] args = { "-option1 My option1 is there", "-option2 \"My option2 - is there\"",
                      "-option3=\"My option3 - is there\"", "\"My option4 - is there\"", "My option5 is there",
                      "-option1 My option1 is there -option2 \"My option2 - is there\" -option3=\"My option3 - is there\" \"My option4 - is there\" My option5 is there" };

    String[][] parsed = { { "-option1", "My option1 is there" }, { "-option2", "My option2 - is there" },
                          { "-option3=My option3 - is there" }, { "My option4 - is there" }, { "My option5 is there" },
                          { "-option1", "My option1 is there", "-option2", "My option2 - is there",
                            "-option3=My option3 - is there", "My option4 - is there", "My option5 is there" } };

    @Test
    public void TestParseArg() {
        for (int i = 0; i < args.length; i++) {
            List<String> options = CommandBuilderHelper.parseArg(args[i]);
            List<String> expected = Arrays.asList(parsed[i]);
            System.out.println("" + i);
            System.out.println("Received : " + options);
            System.out.println("Expected : " + expected);
            Assert.assertEquals(expected, options);
        }
    }
}
