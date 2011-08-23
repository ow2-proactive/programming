package functionalTests.multiactivities.can;

import java.io.Serializable;

/**
 * A class that represents the key. It maps to a unique coordinate in the CAN space.
 * @author zistvan
 *
 */
public class Key implements Serializable {
	
	private int x,y;
	
	public Key(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getCoordY() {
		// TODO Auto-generated method stub
		return y;
	}
	
	public int getCoordX() {
		// TODO Auto-generated method stub
		return x;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Key) {
			return x==((Key) obj).x && y==((Key) obj).y; 
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return x*y;
	}
	
	@Override
	public String toString() {
		return "Key ("+x+","+y+")";
	}

}
