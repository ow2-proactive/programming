package org.objectweb.proactive.extra.multiactivecan;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.api.PAFuture;

public class Router implements Serializable {
	
	private Zone center;
	protected Zone oldCenter;
	
	private Set<Zone> neighbours = new HashSet<Zone>();
	
	public Router(Zone center) {
		this.center = center;
		this.oldCenter = center;
	}
	
	
	public Peer getNeighbourClosestTo(Key k) {
		double minDist = 0;
		Peer closest = null;
		
		for (Zone z : neighbours) {
			double dist = z.distanceFromUnitSquare(k.getCoordX(), k.getCoordY()); 
			if (dist < minDist || closest==null) {
				minDist = dist;
				closest = z.getOwner();
			}
		}
		
		return closest;
		
	}
	
	public void removeNeighbour(Zone oldZone) {
		neighbours.remove(oldZone);
	}
	
	public void updateNeighbours(Zone oldZone, Collection<Zone> newZones) {
		if (oldZone!=null) {
			neighbours.remove(oldZone);
		}
		neighbours.addAll(newZones);
	}
	
	public void updateNeighbours(Zone oldZone, Zone newZones) {
		if (oldZone!=null) {
			neighbours.remove(oldZone);
		}
		neighbours.add(newZones);
	}
	
	public void addNeighbour(Zone zone) {
		neighbours.add(zone);
	}
	
	public Router splitWith(Peer joiner) {
		List<Zone> splitZones = new LinkedList<Zone>();
		boolean splitOnX = center.getHeight()<center.getWidth();
		
		if (Math.max(center.getHeight(),center.getWidth())<=1) {
			return null;
		}
		
		Zone newCenter = new Zone(center.getCornerX(), center.getCornerY(), splitOnX ? center.getWidth()/2 : center.getWidth(), splitOnX ? center.getHeight() : center.getHeight()/2, center.getOwner());
		splitZones.add(newCenter);
		
		Zone newZone = new Zone(splitOnX ? center.getCornerX()+center.getWidth()/2 :  center.getCornerX(), splitOnX ? center.getCornerY() : center.getCornerY() + center.getHeight()/2, splitOnX ? center.getWidth()/2 : center.getWidth(), splitOnX ? center.getHeight() : center.getHeight()/2, joiner);
		splitZones.add(newZone);
		
		//System.out.println("	Splitted "+center+" into new center:"+newCenter+" and other:"+newZone);
		
		Router newRouter = new Router(newZone);
		
		Iterator<Zone> i = neighbours.iterator();
		
		while (i.hasNext()) {
			Zone z = i.next();
			List<Zone> toAdd = new LinkedList<Zone>();
			
			if (z.touches(newZone)) {
					toAdd.add(newZone);
					newRouter.addNeighbour(z);
			}
			
			if (z.touches(newCenter)) {
				toAdd.add(newCenter);
			} else {
				//System.out.println("removed a neighbour!");
				i.remove();
			}
			
			//System.out.println("     Updating "+z+" with "+toAdd.size()+" neighbs");
			PAFuture.waitFor(z.getOwner().updateNeighbours(center, toAdd));
		}
		
		newRouter.addNeighbour(newCenter);
		neighbours.add(newZone);
		
		center.resize(newCenter);
		
		return newRouter;
	}
	
	public boolean isLocal(Key k) {
		return center.containsUnitSquare(k.getCoordX(), k.getCoordY());
	}
	
	public boolean isRecentLocal(Key k) {
		synchronized (oldCenter) {
			return oldCenter.containsUnitSquare(k.getCoordX(), k.getCoordY());
		}
	}
	
	public void joinFinished() {
		synchronized (oldCenter) {
			oldCenter = center;
		}
	}
	
	@Override
	public String toString() {
		return center.toString(); 
	}

}
