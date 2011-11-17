/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.component.adl.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.attributes.Attributes;
import org.objectweb.fractal.adl.attributes.AttributesContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeBuilder;
import org.objectweb.fractal.adl.types.TypeCompiler;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.task.core.TaskMap;
import org.objectweb.fractal.task.deployment.lib.AbstractFactoryProviderTask;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * Until now, it does not do anything different ...
 * 
 * @author cruz
 *
 */
public class PATypeCompiler extends TypeCompiler {

	public static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

	@Override
	public void compile(List path, ComponentContainer container, TaskMap tasks,
			Map context) throws ADLException {
		
		logger.debug("[PATypeCompiler] Compiler container "+ container.toString() );
		
		if (container instanceof InterfaceContainer) {
			try {
				// the task may already exist, in case of a shared component
				tasks.getTask("type", container);
			} catch (NoSuchElementException e) {
				CreateTypeTask createTypeTask = new CreateTypeTask(builder,
						(InterfaceContainer) container);
				tasks.addTask("type", container, createTypeTask);
			}
		}
		
		//super.compile(path, container, tasks, context);
	}

	
	// --------------------------------------------------------------------------
	// Inner classes
	// --------------------------------------------------------------------------

	static class CreateTypeTask extends AbstractFactoryProviderTask {

		private TypeBuilder builder;

		private InterfaceContainer container;

		public CreateTypeTask(final TypeBuilder builder,
				final InterfaceContainer container) {
			this.builder = builder;
			this.container = container;
		}

		public void execute(final Map context) throws Exception {
			if (getFactory() != null) {
				return;
			}
			List<Object> itfTypes = new ArrayList<Object>();
			Interface[] itfs = container.getInterfaces();
			for (int i = 0; i < itfs.length; i++) {
				if (itfs[i] instanceof TypeInterface) {
					TypeInterface itf = (TypeInterface) itfs[i];
					Object itfType = builder.createInterfaceType(itf.getName(),
							itf.getSignature(), itf.getRole(), itf
									.getContingency(), itf.getCardinality(),
							context);
					itfTypes.add(itfType);
				}
			}
			
			/* TODO improve module separation (how?) */
			if (container instanceof AttributesContainer) { 
				Attributes attr = ((AttributesContainer) container)
						.getAttributes();
				if (attr != null) {
					Object itfType = builder.createInterfaceType(
							"attribute-controller", attr.getSignature(),
							TypeInterface.SERVER_ROLE,
							TypeInterface.MANDATORY_CONTINGENCY,
							TypeInterface.SINGLETON_CARDINALITY, context);
					itfTypes.add(itfType);
				}
			}
			String name = null;
			if (container instanceof Definition) {
				name = ((Definition) container).getName();
			} else if (container instanceof Component) {
				name = ((Component) container).getName();
			}
			setFactory(builder.createComponentType(name, itfTypes.toArray(),
					context));
		}

		public String toString() {
			return "T" + System.identityHashCode(this) + "[PA-CreateTypeTask()]";
		}
	}
}
