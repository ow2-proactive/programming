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
package org.objectweb.proactive.core.component.adl.components;

import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A ProActive based implementation of the {@link ComponentBuilder} interface.
 * This implementation uses the GCM API to add and start components.
 * It slightly differs from the FractalComponentBuilder class : the name of the component
 * is not specified in this addition operation, but when the component is instantiated. <br/><br/>
 *
 * The addComponent method is extended to discriminate functional subcomponents (that go in the functional content)
 * from NF components (that go in the membrane).<br/><br/>
 * 
 * It also includes a method to explicitly start the membrane, so that the functional lifecycle can be
 * properly started later.
 * 
 * @author The ProActive Team
 *
 */
public class PAComponentBuilder implements PAComponentBuilderItf {
	
	protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
	
    // --------------------------------------------------------------------------
    // Implementation of the ComponentBuilder interface
    // --------------------------------------------------------------------------
	@Override
    public void addComponent(final Object superComponent, final Object subComponent, final String name,
            final Object context) throws Exception {
    	
        GCM.getContentController((Component) superComponent).addFcSubComponent((Component) subComponent);
        // as opposed  to the standard fractal implementation, we do not set
        // the name of the component here because :
        // 1. it is already name at instantiation time
        // 2. it could be a group of components, and we do not want to give the
        // same name to all the elements of the group
        //    try {
        //      GCM.getNameController((Component)subComponent).setFcName(name);
        //    } catch (NoSuchInterfaceException ignored) {
        //    }
    }
	
	/**
	 * Add the subComponent into the superComponent, either in the functional content,
	 * or in the membrane.
	 */
	@Override
	public void addComponent(Object superComponent, Object subComponent,
			String name, boolean isFunctional, Map<Object, Object> context)
			throws Exception {
		
		//DEBUG
		String superComponentName = ((PAComponent) superComponent).getComponentParameters().getName();
		String subComponentName = ((PAComponent) subComponent).getComponentParameters().getName();
		logger.debug("[PAComponentBuilder] Adding "+ (isFunctional?"F":"NF") + " component " + subComponentName + " to " + superComponentName );
		//--DEBUG
		
		if(isFunctional) {
			// use the ContentController
			GCM.getContentController((Component) superComponent).addFcSubComponent((Component) subComponent);
		}
		else {
			// use the MembraneController
			try {
				// membrane of superComponent must be stopped
				PAMembraneController pamc = Utils.getPAMembraneController((Component)superComponent);
				pamc.stopMembrane();
				//membrane of subComponent must be started (why?)
				PAMembraneController subPamc = Utils.getPAMembraneController((Component)subComponent);
				subPamc.startMembrane();
				
				logger.debug("[PAComponentBuilder] Membrane state-sup: "+ pamc.getMembraneState());
				logger.debug("[PAComponentBuilder] Membrane state-sub: "+ subPamc.getMembraneState());
				
				// add the NF component
				pamc.nfAddFcSubComponent((Component) subComponent);
				
			} catch (NoSuchInterfaceException nsie) {
				logger.debug("[PAComponentBuilder] NOT FOUND Membrane Controller in "+ superComponentName);
			}

		}
		
	}

    /**
     * Nothing
     */
    public void startComponent(final Object component, final Object context) throws Exception {
    	
    }

    /**
     * Start the membrane of the component. 
     * This method should be called after all the manipulations inside the membrane have been made.
     */
	@Override
	public void startMembrane(final Object component, final Map<Object, Object> context) throws Exception {
		
		// DEBUG
		String componentName = ((PAComponent) component).getComponentParameters().getName();
		logger.debug("[PAComponentBuilder] Starting membrane of "+ componentName);
		//--DEBUG
		
		try {
			// start the membrane
			PAMembraneController pamc = Utils.getPAMembraneController((Component)component);
			pamc.startMembrane();
			
		} catch (NoSuchInterfaceException e) {
			logger.debug("[PAComponentBuilder] NOT FOUND Membrane Controller in "+ componentName);
			e.printStackTrace();
		}
		
		
	}


}
