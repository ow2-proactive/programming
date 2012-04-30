package org.objectweb.proactive.core.component.adl.types;

import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;

/**
 * Utility methods about the properties of a PATypeInterface AST node (&lt;interface&gt;)
 * 
 * 
 * @author The ProActive Team
 *
 */
public class PATypeInterfaceUtil  {

	public static boolean isInternal(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String role = paItf.getRole();
			return PATypeInterface.INTERNAL_CLIENT_ROLE.equals(role) || PATypeInterface.INTERNAL_SERVER_ROLE.equals(role);
		}
		return false;
	}
	
	public static boolean isServer(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String role = paItf.getRole();
			return PATypeInterface.SERVER_ROLE.equals(role) || PATypeInterface.INTERNAL_SERVER_ROLE.equals(role);
		}
		else if(itf instanceof TypeInterface) {
			return TypeInterfaceUtil.isServer(itf);
		}
		return false;
	}
	
	public static boolean isClient(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String role = paItf.getRole();
			return PATypeInterface.CLIENT_ROLE.equals(role) || PATypeInterface.INTERNAL_CLIENT_ROLE.equals(role);
		}
		else if(itf instanceof TypeInterface) {
			return TypeInterfaceUtil.isClient(itf);
		}
		return false;
	}
	
	public static boolean isOptional(final Interface itf) {
		return TypeInterfaceUtil.isOptional(itf);
	}
	
	public static boolean isMandatory(final Interface itf) {
		return TypeInterfaceUtil.isMandatory(itf);
	}
	
	public static boolean isSingleton(final Interface itf) {
		return TypeInterfaceUtil.isSingleton(itf);
	}
	
	public static boolean isCollection(final Interface itf) {
		return TypeInterfaceUtil.isCollection(itf);
	}
	
	public static boolean isMulticast(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String cardinality = paItf.getCardinality();
			return PATypeInterface.MULTICAST_CARDINALITY.equals(cardinality);
		}
		return false;
	}
	
	public static boolean isGathercast(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String cardinality = paItf.getCardinality();
			return PATypeInterface.GATHERCAST_CARDINALITY.equals(cardinality);
		}
		return false;
	}
	
	public static boolean isCollective(final Interface itf) {
		if(itf instanceof PATypeInterface) {
			final PATypeInterface paItf = (PATypeInterface) itf;
			String cardinality = paItf.getCardinality();
			return PATypeInterface.MULTICAST_CARDINALITY.equals(cardinality) || PATypeInterface.GATHERCAST_CARDINALITY.equals(cardinality);
		}
		return false;
	}
	
}
