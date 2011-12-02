package org.objectweb.proactive.core.component.adl.types;

import java.util.Map;

import org.objectweb.fractal.adl.types.TypeBuilder;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

/**
 * The {@link PATypeBuilderItf} extends the {@link TypeBuilder} interface to provide
 * a method that creates a ComponentType (technically, a PAComponentType) using an array
 * of F InterfaceTypes, and a set of NF InterfaceTypes. 
 * 
 * It also adds a method that considers the internal/external attribute for the {@link InterfaceType}.
 * 
 * @author The ProActive Team
 *
 */
public interface PATypeBuilderItf extends TypeBuilder {

	InterfaceType createInterfaceType(String name, String signature,
			String role, String contingency, String cardinality, Map<Object, Object> context)
			throws Exception;
	
	InterfaceType createInterfaceType(String name, String signature,
			String role, String contingency, String cardinality, boolean isInternal, Map<Object, Object> context)
			throws Exception;

	ComponentType createComponentType(String name,
			InterfaceType[] interfaceTypes, Map<Object, Object> context)
			throws Exception;

	ComponentType createComponentType(String name,
			InterfaceType[] fInterfaceTypes, InterfaceType[] nfInterfaceTypes,
			Map<Object, Object> context) throws Exception;
}
