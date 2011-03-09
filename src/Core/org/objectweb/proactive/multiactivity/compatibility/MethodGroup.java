package org.objectweb.proactive.multiactivity.compatibility;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a group of methods. A group is compatible with other groups, meaning that
 * methods belonging to these groups can run in parallel.
 * @author Zsolt Istvan
 *
 */
public class MethodGroup {

	public final String name;
	private final boolean selfCompatible;
	
	private Set<MethodGroup> compatibleWith = new HashSet<MethodGroup>();
	
	private final int hashCode;
	
	/**
	 * Standard constructor of a named group.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 */
	public MethodGroup(String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		
	}
	
	/**
	 * Standard constructor of a named group.
	 * @param name A descriptive name for the group -- all group names have to be unique
	 * @param selfCompatible if the methods that are members of the group can run in parallel
	 */
	public MethodGroup(MethodGroup from, String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		
		if (from!=null) {
			this.addCompatibleWith(from.compatibleWith);
			if (from.selfCompatible) {
				from.addCompatibleWith(this);
			}
		}
		
	}
	
	/**
	 * Set the set of compatible groups
	 * @param compatibleWith
	 */
	public void setCompatibleWith(Set<MethodGroup> compatibleWith) {
		this.compatibleWith = compatibleWith;
	}
	
	/**
	 * Add a group whose methods will be runnable in parallel with the methods
	 * belonging to this one
	 * @param compatibleWith the other group
	 */
	public void addCompatibleWith(MethodGroup compatibleWith) {
		this.compatibleWith.add(compatibleWith);
	}

	public void addCompatibleWith(Set<MethodGroup> compatibleSet) {
		this.compatibleWith.addAll(compatibleSet);
		
	}

	/**
	 * Returns the set of the groups whose member methods can run in parallel with the
	 * methods belonging to this group
	 * @return
	 */
	public Set<MethodGroup> getCompatibleWith() {
		return compatibleWith;
	}

	public boolean isSelfCompatible() {
		return selfCompatible;
	}

	@Override
	public int hashCode() {
		return hashCode; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodGroup) {
			MethodGroup other = (MethodGroup) obj;
			return (this.name.equals(other.name));
		} 
		return false;
	}
	
}
