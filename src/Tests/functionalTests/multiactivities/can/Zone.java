package functionalTests.multiactivities.can;

import java.io.Serializable;
import java.util.Collection;

/**
 * A zone in the CAN topology.
 * @author zistvan
 *
 */
public class Zone implements Serializable {
	
	public static final int MAX_X = 1000000;
	public static final int MAX_Y = 1000000;
	
	private int x,y,w,h;
	private Peer owner;
	
	public Zone(int x, int y, int w, int h, Peer owner) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.owner = owner;
	}

	public Peer getOwner() {
		return owner;
	}
	
	public int getCornerX() {
		return x;
	}
	
	public int getCornerY() {
		return y;
	}
	
	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return h;
	}
	
	
	public boolean touches(Zone other) {
		double cx = x+w/2.0;
		double cy = y+h/2.0;
		
		double cxo = other.x+other.w/2.0;
		double cyo = other.y+other.h/2.0;
		
		return (Math.abs(cx-cxo)==(w/2.0+other.w/2.0) ^ Math.abs(cy-cyo)==(h/2.0+other.h/2.0));
		
	}
	
	public boolean containsUnitSquare(int sx, int sy) {
		double cx = x+w/2.0;
		double cy = y+h/2.0;
		return (Math.abs(cx-sx-0.5)<=(w/2.0) && Math.abs(cy-sy-0.5)<=(h/2.0));
	}
	
	public double distanceFromUnitSquare(int sx, int sy) {
		double distX = 0;
		double distY = 0;
		
		if (sx>x+w) {
			distX = x+w-sx;
		} else if (sx+1<x) {
			distX = sx+1-x;
		}
		
		if (sy>y+h) {
			distY = y+h-sy;
		} else if (sy+1<y) {
			distY = sy+1-y;
		}
		
		return Math.sqrt(distX*distX + distY*distY);
		
	}
	
	public void resize(Zone z) {
		this.x = z.x;
		this.y = z.y;
		this.w = z.w;
		this.h = z.h;
	}

	@Override
	public String toString() {
		return "Zone ("+x+","+y+")("+w+","+h+")";
	}
	
	@Override
	public int hashCode() {
		return x*10000 + y*1000 + w*100 + h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Zone) {
			Zone other = (Zone) obj;
			return other.x==x && other.y==y && other.w==w && other.h==h;
		}
		
		return false;
	}

}
