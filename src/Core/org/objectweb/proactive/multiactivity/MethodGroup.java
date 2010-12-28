package org.objectweb.proactive.multiactivity;

import java.util.HashSet;
import java.util.Set;

public class MethodGroup {
	
	public final int hashCode;
	public final MethodGroup parent;
	public final String  name;
	public final boolean selfCompatible;
	private Set<MethodGroup> compatibleWith = new HashSet<MethodGroup>();
	
	public MethodGroup(String name, boolean selfCompatible) {
		this.name = name;
		this.selfCompatible = selfCompatible;
		this.parent = null;
		this.hashCode = name.hashCode();
	}
	
	public MethodGroup(MethodGroup from, String name, boolean selfCompatible) {
		this.name = from.name+"_"+name;
		this.selfCompatible = from.selfCompatible || selfCompatible;

		this.compatibleWith.addAll(from.getCompatibleWith());
		
		this.parent = from;
		this.hashCode = from.hashCode();
	}

	public void addCompatibleWith(Set<MethodGroup> compatibleWith) {
		this.compatibleWith.addAll(compatibleWith);
	}
	
	public void addCompatibleWith(MethodGroup compatibleWith) {
		this.compatibleWith.add(compatibleWith);
	}

	public Set<MethodGroup> getCompatibleWith() {
		return compatibleWith;
	}

	@Override
	public int hashCode() {
		return hashCode; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodGroup) {
			MethodGroup other = (MethodGroup) obj;
			return (this.name.equals(other.name)) || (this.parent!=null && this.parent.equals(other));
		} 
		return false;
	}
	
}
