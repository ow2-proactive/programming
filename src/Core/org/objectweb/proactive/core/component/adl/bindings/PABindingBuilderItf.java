package org.objectweb.proactive.core.component.adl.bindings;

import org.objectweb.fractal.adl.bindings.BindingBuilder;

/**
 * Extends the {@link BindingBuilder} interface to add a method
 * able to recognize a NF Binding.
 * 
 * An NF Binding is a binding that involves at least one NF interface,
 * or one NF Component.
 * 
 * An NF Binding must be carried out by the MembraneController, instead
 * of the ContentController.
 * 
 * @author The ProActive Team
 *
 */
public interface PABindingBuilderItf extends BindingBuilder {

	void bindComponent (int type, Object client, String clientItf, Object server, String serverItf, boolean isFunctional, Object context) throws Exception;
	
}
