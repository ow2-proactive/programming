package org.objectweb.proactive.extensions.webservices.servlet;

import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.context.ConfigurationContext;

public class PAAxisServlet extends AxisServlet {
	public ConfigurationContext getConfigContext() {
		return this.configContext;
	}
}
