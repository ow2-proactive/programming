/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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

package functionalTests.activeobject.webservices;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.WebServices;


public class TestWeather {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    String url;

    @Before
    public void deployWeatherService() throws Exception {
        // Loading the WebServices class enables us to retrieve the jetty
        // port number
        Class.forName("org.objectweb.proactive.extensions.webservices.WebServices");
        String port = PAProperties.PA_XMLHTTP_PORT.getValue();
        //        String port = "8081";
        this.url = "http://localhost:" + port + "/";

        WeatherService weatherService = (WeatherService) PAActiveObject.newActive(
                "functionalTests.activeobject.webservices.WeatherService", new Object[] {});
        WebServices.exposeAsWebService(weatherService, this.url, "WeatherService");
    }

    @org.junit.Test
    public void TestWeatherService() throws Exception {

        RPCServiceClient serviceClient = new RPCServiceClient();

        Options options = serviceClient.getOptions();

        EndpointReference targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH +
            "WeatherService");
        options.setTo(targetEPR);

        // Setting the weather
        options.setAction("setWeather");
        QName opSetWeather = new QName("setWeather");

        Weather w = new Weather();

        w.setTemperature((float) 39.3);
        w.setForecast("Cloudy with showers");
        w.setRain(true);
        w.setHowMuchRain((float) 4.5);

        Object[] opSetWeatherArgs = new Object[] { w };

        serviceClient.invokeRobust(opSetWeather, opSetWeatherArgs);

        // Getting the weather
        options.setAction("getWeather");
        QName opGetWeather = new QName("getWeather");

        Object[] opGetWeatherArgs = new Object[] {};
        Class<?>[] returnTypes = new Class<?>[] { Weather.class };

        Object[] response = serviceClient.invokeBlocking(opGetWeather, opGetWeatherArgs, returnTypes);

        Weather result = (Weather) response[0];

        if (result == null) {
            logger.info("Weather didn't initialize!");
            return;
        }

        // Displaying the result
        logger.info("Temperature               : " + result.getTemperature());
        logger.info("Forecast                  : " + result.getForecast());
        logger.info("Rain                      : " + result.getRain());
        logger.info("How much rain (in inches) : " + result.getHowMuchRain());

    }

    @After
    public void undeployWeatherService() throws Exception {
        WebServices.unExposeAsWebService(this.url, "WeatherService");
    }
}
