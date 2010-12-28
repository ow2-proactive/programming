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
	protected final MethodGroup parent;
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
