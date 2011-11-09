package org.objectweb.proactive.core.component.adl.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.interfaces.InterfaceLoader;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * The {@link PAInterfaceLoader} extends the {@link InterfaceLoader} to look for
 * interfaces defined inside the &lt;controller&gt; tag (i.e. NF Interfaces).
 * 
 * Such interfaces are marked as NF by adding a decoration to the node.
 * 
 * @author cruz
 *
 */

public class PAInterfaceLoader extends InterfaceLoader {

	public static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

	/** 
	 * Checks the tree considering the initial &lt;definition&gt; as F component
	 */
	@Override
	public Definition load(final String name, final Map<Object, Object> context)
			throws ADLException {
		final Definition d = clientLoader.load(name, context);
		checkNode(d, true);
		return d;
	}

	/**
	 * Looks for containers of &lt;interface&gt; nodes.
	 * 
	 * @param node
	 * @throws ADLException
	 */
	protected void checkNode(final Object node, boolean functional) throws ADLException {
		//logger.debug("[PAInterfaceLoader] Analyzing node "+ node.toString()); 
		if (node instanceof InterfaceContainer) {
			checkInterfaceContainer((InterfaceContainer) node, functional);
		}
		// interfaces defined inside a <component> node are F, even if the component maybe NF
		if (node instanceof ComponentContainer) {
			for (final Component comp : ((ComponentContainer) node).getComponents()) {
				checkNode(comp, true);
			}
		}
		// interfaces defined inside a <controller> node are NF (i.e. they belong to the membrane)
		if (node instanceof ControllerContainer) {
			Controller ctrl = ((ControllerContainer) node).getController();
			if(ctrl != null) {
				checkNode(ctrl, false);
			}
		}
	}

	/**
	 * Checks &lt;interface&gt; nodes, and marks them as F or NF.<br/>
	 * This allows duplicate interface names if one of the interfaces is F and the other one is NF
	 * 
	 * @param container
	 * @param functional
	 * @throws ADLException
	 */
	protected void checkInterfaceContainer(final InterfaceContainer container, boolean functional)
			throws ADLException {

		final Map<String, Interface> names = new HashMap<String, Interface>();
		for (final Interface itf : container.getInterfaces()) {
			if (itf.getName() == null) {
				throw new ADLException(InterfaceErrors.INTERFACE_NAME_MISSING, itf);
			}
			logger.debug("[PAInterfaceLoader] Found interface "+ itf.toString() + " " + (functional?"F":"NF") );
			if(!functional) {
				itf.astSetDecoration("NF", true);
			}			
			final Interface previousDefinition = names.put(itf.getName(), itf);
			if (previousDefinition != null) {
				throw new ADLException(InterfaceErrors.DUPLICATED_INTERFACE_NAME, itf,
						itf.getName(), new NodeErrorLocator(previousDefinition));
			}
		}
	}
	
}
