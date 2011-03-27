package functionalTests.multiactivities.imageprocessing;

public class BitmapRegion implements java.io.Serializable{
    
    private String bitmapName;
    private int x;
    private int y;
    private int width;
    private int height;
    
    public BitmapRegion(String bitmapName, int x, int y, int width, int height) {
        super();
        this.bitmapName = bitmapName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getBitmapName() {
        return bitmapName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public boolean overlaps(BitmapRegion other) {
        if (!other.getBitmapName().equals(bitmapName)) {
            return false;
        }
        
        int centX1 = x+width/2;
        int centY1 = y+height/2;
        int centX2 = other.getX()+other.getWidth()/2;
        int centY2 = other.getY()+other.getHeight()/2;
        return (Math.abs(centX1-centX2)<(other.getWidth()/2 + width/2)) || (Math.abs(centY1-centY2)<(other.getHeight()/2 + height/2));
    }
    
    @Override
    public String toString() {
        return bitmapName;
    }
    
    public static boolean sameName(Object param1, Object param2) {
        return param1.toString().equals(param2.toString());
    }

}
