/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.webservices.axis2;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.webservices.common.Weather;
import functionalTests.activeobject.webservices.common.WeatherService;


public class TestWeather extends FunctionalTest {

    private String url;
    private WebServices ws;

    @org.junit.Before
    public void deployWeatherService() {

        try {
            this.url = AbstractWebServicesFactory.getLocalUrl();

            WeatherService weatherService = (WeatherService) PAActiveObject.newActive(
                    "functionalTests.activeobject.webservices.common.WeatherService", new Object[] {});

            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory("axis2");
            ws = wsf.getWebServices(url);

            ws.exposeAsWebService(weatherService, "WeatherService");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.Test
    public void TestWeatherService() {

        ClientFactory cf = null;
        try {
            cf = AbstractClientFactory.getClientFactory("axis2");
        } catch (UnknownFrameWorkException e1) {
            e1.printStackTrace();
            assertTrue(false);
        }

        try {
            Client client = cf.getClient(this.url, "WeatherService", WeatherService.class);

            Weather w = new Weather();

            w.setTemperature((float) 39.3);
            w.setForecast("Cloudy with showers");
            w.setRain(true);
            w.setHowMuchRain((float) 4.5);

            Object[] setWeatherArgs = new Object[] { w };

            client.oneWayCall("setWeather", setWeatherArgs);

            Object[] response = client.call("getWeather", null, Weather.class);

            Weather result = (Weather) response[0];

            assertTrue(((Float) result.getTemperature()).equals(new Float(39.3)));
            assertTrue(result.getForecast().equals("Cloudy with showers"));
            assertTrue(result.getRain());
            assertTrue(((Float) result.getHowMuchRain()).equals(new Float(4.5)));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.After
    public void undeployWeatherService() {
        try {
            ws.unExposeAsWebService("WeatherService");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
