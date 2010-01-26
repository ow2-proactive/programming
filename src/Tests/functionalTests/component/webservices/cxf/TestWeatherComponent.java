/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.webservices.cxf;

import static org.junit.Assert.assertTrue;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;

import functionalTests.FunctionalTest;
import functionalTests.component.webservices.common.Weather;
import functionalTests.component.webservices.common.WeatherServiceComponent;
import functionalTests.component.webservices.common.WeatherServiceItf;


public class TestWeatherComponent extends FunctionalTest {

    private String url;
    private WebServices ws;

    @org.junit.Before
    public void deployWeatherService() {
        try {
            this.url = AbstractWebServicesFactory.getLocalUrl();

            Component boot = null;
            Component comp = null;

            boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();

            TypeFactory tf = Fractal.getTypeFactory(boot);
            GenericFactory cf = Fractal.getGenericFactory(boot);

            ComponentType typeComp = tf.createFcType(new InterfaceType[] { tf.createFcItfType(
                    "weather-service", WeatherServiceItf.class.getName(), false, false, false) });

            comp = cf.newFcInstance(typeComp, new ControllerDescription("server", Constants.PRIMITIVE),
                    new ContentDescription(WeatherServiceComponent.class.getName(), null));

            Fractal.getLifeCycleController(comp).startFc();

            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory("cxf");
            ws = wsf.getWebServices(url);
            ws.exposeComponentAsWebService(comp, "server", new String[] { "weather-service" });

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.Test
    public void TestWeatherService() {

        try {
            ClientFactory cf = AbstractClientFactory.getClientFactory("cxf");
            Client client = cf.getClient(url, "server_weather-service", WeatherServiceItf.class);

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
            ws.unExposeComponentAsWebService("server", new String[] { "weather-service" });
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
