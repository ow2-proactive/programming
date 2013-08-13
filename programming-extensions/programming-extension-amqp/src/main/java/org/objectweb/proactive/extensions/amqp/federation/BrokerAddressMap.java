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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.amqp.federation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConnectionParameters;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConstants;


/**
 * In the use case when amqp-federation protocol is used it isn't
 * possible to extract broker's address from the remote object's
 * URL since there is no guarantee that node has direct access to the 
 * remote object's broker, instead node has access to the dedicated
 * broker(s) which are linked with others brokers via RabbitMQ 
 * federation plugin.
 * <p>
 * Information about brokers which should be used to communicate with 
 * remote objects is specified in the configuration file as mapping
 * between broker's address in the remote object's URL and 
 * connection parameters of the broker which should be used to 
 * communicate with this remote object. 
 * <p>
 * BrokerAddressMap parses configuration file with mapping and provides 
 * method to get broker's connection parameters by the remote object's URL. 
 * 
 * @author ProActive team
 *
 */
public class BrokerAddressMap {

    private final AMQPConnectionParameters defaultParams;

    private final Map<String, AMQPConnectionParameters> mapping;

    private static final String BROKER_KEY_PREFIX = "broker_key.";

    /**
     *  Create BrokerAddressMap using broker address mapping specified in the given
     *  properties file. Expected file format:
     *  
     *  <pre>
     *  broker_key.<broker logical name>=<host>:<port>
     *  <broker logical name>.host=<host>
     *  <broker logical name>.port=<port>
     *  <broker logical name>.user=<user>
     *  <broker logical name>.password=<password>
     *  <broker logical name>.vhost=<vhost>
     *  </pre>
     *  
     *  If some property '<broker logical name>.*' isn't specified then for this property
     *  default value specified in the AMQPConstants is used.
     *  <p>
     *  Here is an example of the configuration file:
     *  
     *  <pre>
     *  broker_key.broker1=host1:5672
     *  broker1.host=host2
     *  broker1.port=5673
     *  broker1.user=user1
     *  broker1.password=123
     *  
     *  broker_key.broker2=host3:5674
     *  broker2.host=host4
     *  broker2.port=5675
     *  </pre>
     *  
     *  With this configuration if remote object's URL is based on the 'host1:5672' 
     *  then to communicate with this object will be used broker with address 'host2:5673', 
     *  and for connection user name 'user1' and password '123' will be used.
     *  <p>
     *  If remote object's URL is based on the 'host3:5674' then to communicate 
     *  with this object will be used broker with address 'host4:5675'; since
     *  properties 'broker2.user' and 'broker2.password' aren't specified then
     *  for connection default user name/password will be used. 
     *  
     */
    public static BrokerAddressMap createFromMappingFile(AMQPConnectionParameters defaultParams,
            String mappingFilePath) {
        File file = new File(mappingFilePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " not found");
        }

        Properties properties = new Properties();
        try {
            FileReader reader = new FileReader(file);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load configuration properties from the " +
                file.getAbsolutePath(), e);
        }

        Map<String, AMQPConnectionParameters> mapping = new HashMap<String, AMQPConnectionParameters>();
        for (Object key : properties.keySet()) {
            String property = key.toString();
            if (property.startsWith(BROKER_KEY_PREFIX)) {
                String brokerName = property.substring(BROKER_KEY_PREFIX.length());
                if (brokerName.isEmpty()) {
                    throw new IllegalArgumentException("Broker name is empty: " + property);
                }
                String brokerKey = properties.getProperty(property);
                if (brokerKey == null) {
                    throw new IllegalArgumentException("Broker's host:port is empty for property: " +
                        property);
                }

                String host = properties.getProperty(brokerName + ".host", AMQPConstants.DEFAULT_BROKER_HOST);
                String port = properties.getProperty(brokerName + ".port", String
                        .valueOf(AMQPConstants.DEFAULT_BROKER_PORT));
                String username = properties.getProperty(brokerName + ".user", AMQPConstants.DEFAULT_USER);
                String password = properties.getProperty(brokerName + ".password",
                        AMQPConstants.DEFAULT_PASSWORD);
                String vhost = properties.getProperty(brokerName + ".vhost", AMQPConstants.DEFAULT_VHOST);
                try {
                    mapping.put(brokerKey, new AMQPConnectionParameters(host, Integer.valueOf(port),
                        username, password, vhost));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid broker port value: " + port, e);
                }
            }
        }

        BrokerAddressMap brokerAddressMap = new BrokerAddressMap(defaultParams, mapping);
        return brokerAddressMap;
    }

    public BrokerAddressMap(AMQPConnectionParameters defaultParams,
            Map<String, AMQPConnectionParameters> mapping) {
        this.defaultParams = defaultParams;
        this.mapping = mapping;
    }

    public AMQPConnectionParameters getBrokerForObject(URI objectURI) {
        String key = objectURI.getHost() + ":" + objectURI.getPort();
        AMQPConnectionParameters params = mapping.get(key);
        return params != null ? params : defaultParams;
    }

}
