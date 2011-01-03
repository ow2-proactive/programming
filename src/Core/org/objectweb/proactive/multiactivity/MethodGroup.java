package org.objectweb.proactive.multiactivity;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a group of methods. A group can be compatible with other groups, meaning that
 * methods belonging to these two groups can run in parallel.
 * <br/>
 * Groups can extend an other group, meaning that it will inherit all compatibilities of the parent, and 
 * add its own ones too.
 * <br/>
 * A group has the following properties:
 * <ul>
 *  <li>self compatible -- if this flag is false only one instance of one method of this group 
 *  can run at any time</li>
 *  <li>compatibility list -- list of groups it can run in parallel with</li>
 * 	<li>parent group --  the group this one extends</li>
 *  <li>name -- this usually describes the role of the methods in the group </li>
 * </ul>
 * @author Zsolt Istvan
 *
 */
public class MethodGroup {
	
	private final int hashCode;
	private final boolean selfCompatible;
	public final String  name;
	private Set<MethodGroup> compatibleWith = new HashSet<MethodGroup>();
	
	public MethodGroup(String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		
	}
	
	public MethodGroup(MethodGroup from, String name, boolean selfCompatible) {
		this.selfCompatible = selfCompatible;
		this.name = from.name+"_"+name;
		this.hashCode = name.hashCode();
		
		if (selfCompatible) {
			this.compatibleWith.add(this);
		}
		this.compatibleWith.addAll(from.getCompatibleWith());
		
		for (MethodGroup mg : this.compatibleWith) {
			mg.addCompatibleWith(this);
		}
	}

	public void setCompatibleWith(Set<MethodGroup> compatibleWith) {
		this.compatibleWith = compatibleWith;
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
			return (this.name.equals(other.name));
		} 
		return false;
	}
	
}
