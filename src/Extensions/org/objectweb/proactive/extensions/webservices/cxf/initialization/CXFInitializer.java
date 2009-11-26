package org.objectweb.proactive.extensions.webservices.cxf.initialization;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.cxf.WSConstants;
import org.objectweb.proactive.extensions.webservices.cxf.servicedeployer.ServiceDeployer;
import org.objectweb.proactive.extensions.webservices.cxf.servicedeployer.ServiceDeployerItf;


public class CXFInitializer {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    public static synchronized void init() {
        // Retrieve or launch a Jetty server
        // in case of a local exposition
        HTTPServer httpServer = HTTPServer.get();

        if (httpServer.isMapped(WSConstants.SERVLET_PATH)) {
            logger.info("The CXF servlet has already been installed");
            return;
        }

        // Creates a CXF servlet and register it
        // to the Jetty server
        CXFServlet cxf = new CXFServlet();
        ServletHolder CXFServletHolder = new ServletHolder(cxf);

        httpServer.registerServlet(CXFServletHolder, WSConstants.SERVLET_PATH);

        // Configures the bus
        Bus bus = cxf.getBus();
        BusFactory.setDefaultBus(bus);

        /*
         * Configure the service
         */
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setAddress("/ServiceDeployer");
        svrFactory.setServiceClass(ServiceDeployerItf.class);
        svrFactory.setServiceBean(new ServiceDeployer());

        if (logger.getLevel() != null && logger.getLevel() == Level.DEBUG) {

            /*
             * Attaches a list of in-interceptors
             * In our case, only a logger is attached in order to be able
             * to see input soap messages
             */
            List<Interceptor> inInterceptors = new ArrayList<Interceptor>();
            LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
            inInterceptors.add(loggingInInterceptor);
            svrFactory.setInInterceptors(inInterceptors);

            /*
             * Attaches a list of out-interceptors
             * In our case, only a logger is attached in order to be able
             * to see output soap messages
             */
            List<Interceptor> outInterceptors = new ArrayList<Interceptor>();
            LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
            outInterceptors.add(loggingOutInterceptor);
            svrFactory.setOutInterceptors(outInterceptors);
        }

        // Creates the service
        svrFactory.create();
    }

}
