package functionalTests.multiactivities.imageprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Bitmap implements java.io.Serializable{
    
    private byte[][] red;
    private byte[][] blue;
    private byte[][] green;
    private int width;
    private int height;
    
    public Bitmap(int width, int height) {
        red = new byte[width][height];
        blue = new byte[width][height];
        green = new byte[width][height];
        this.width = width;
        this.height = height;
    }
    
    public byte[] getRGB(int x, int y) {
        byte[] res = {red[x][y], green[x][y], blue[x][y]};
        return res;
    }
    
    public void setRGB(int x, int y, byte[] pixel) {
        red[x][y] = pixel[0];
        green[x][y] = pixel[1];
        blue[x][y] = pixel[2];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public static Bitmap loadBitmap(String file) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(file));
            Bitmap bitmap = new Bitmap(img.getWidth(), img.getHeight());
            for (int x=0; x<img.getWidth(); x++) {
                for (int y=0; y<img.getHeight(); y++) {
                    byte[] pixel = new byte[3];
                    pixel[0] = (byte) img.getData().getSample(x, y, 0);
                    pixel[1] = (byte) img.getData().getSample(x, y, 1);
                    pixel[2] = (byte) img.getData().getSample(x, y, 2);
                }
            }
            
            return bitmap;
            
        } catch (IOException e) {
        }
        
        return null;
    }

    public static void saveBitmap(String file) {
        //TODO
    }
}
