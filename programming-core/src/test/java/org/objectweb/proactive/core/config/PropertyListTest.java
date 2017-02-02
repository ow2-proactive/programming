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
package org.objectweb.proactive.core.config;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;


/**
 * PropertyListTest
 *
 * @author The ProActive Team
 */
public class PropertyListTest {

    @Test
    public void testParseListProperty() {
        PAPropertyList myProperty = new PAPropertyList("my.property", ",", false, "test,a,value");
        myProperty.setValue("test,a,value");

        Assert.assertEquals(Arrays.asList("test", "a", "value"), myProperty.getValue());

        Assert.assertEquals(Arrays.asList("test", "a", "value"), myProperty.getDefaultValue());

        myProperty.setValue(Arrays.asList("test", "b", "value"));
        Assert.assertEquals("test,b,value", myProperty.getValueAsString());

        myProperty.setValue("test , , a");
        Assert.assertEquals(Arrays.asList("test", "a"), myProperty.getValue());

        myProperty.setValue("test,,a");
        Assert.assertEquals(Arrays.asList("test", "a"), myProperty.getValue());

        myProperty.setValue(",");
        Assert.assertEquals(Arrays.asList(), myProperty.getValue());

        myProperty.setValue(",,,,");
        Assert.assertEquals(Arrays.asList(), myProperty.getValue());

        myProperty.setValue(",a");
        Assert.assertEquals(Arrays.asList("a"), myProperty.getValue());

        myProperty.setValue("a,");
        Assert.assertEquals(Arrays.asList("a"), myProperty.getValue());
    }

    @Test
    public void testValidateListUniqueElementsPropertyOk() {

        PAPropertyList myPropertyToValidate1 = new PAPropertyList("my.property",
                                                                  ",",
                                                                  false,
                                                                  CentralPAPropertyRepositoryUtils.IS_SET);
        // should be valid
        myPropertyToValidate1.setValue("a,b,c");
        Assert.assertEquals(Arrays.asList("a", "b", "c"), myPropertyToValidate1.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateListUniqueElementsPropertyKo() {

        PAPropertyList myPropertyToValidate1 = new PAPropertyList("my.property",
                                                                  ",",
                                                                  false,
                                                                  CentralPAPropertyRepositoryUtils.IS_SET);
        // should be not valid
        myPropertyToValidate1.setValue("a,b,a");
    }

    @Test
    public void testValidateAdditionalProtocolsPropertyOk() {
        PAPropertyString myPropertyString = new PAPropertyString("my.string", false);
        myPropertyString.setValue("k");
        PAPropertyList myPropertyToValidate2 = new PAPropertyList("my.property",
                                                                  ",",
                                                                  false,
                                                                  new CentralPAPropertyRepositoryUtils.AdditionalProtocolsValidator(myPropertyString));
        // should be valid
        myPropertyToValidate2.setValue("a,b,c");
        Assert.assertEquals(Arrays.asList(new String[] { "a", "b", "c" }), myPropertyToValidate2.getValue());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateAdditionalProtocolsPropertyKo() {
        PAPropertyString myPropertyString = new PAPropertyString("my.string", false);
        myPropertyString.setValue("k");
        PAPropertyList myPropertyToValidate2 = new PAPropertyList("my.property",
                                                                  ",",
                                                                  false,
                                                                  new CentralPAPropertyRepositoryUtils.AdditionalProtocolsValidator(myPropertyString));
        // should be not valid
        myPropertyToValidate2.setValue("a,b,k");
    }
}
