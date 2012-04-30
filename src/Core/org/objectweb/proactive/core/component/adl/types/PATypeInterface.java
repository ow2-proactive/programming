package org.objectweb.proactive.core.component.adl.types;

import org.objectweb.fractal.adl.types.TypeInterface;

public interface PATypeInterface extends TypeInterface {

	final String INTERNAL_SERVER_ROLE = "internal-server";
	final String INTERNAL_CLIENT_ROLE = "internal-client";
	
	final String MULTICAST_CARDINALITY = "multicast";
	final String GATHERCAST_CARDINALITY = "gathercast";
	
	
}
