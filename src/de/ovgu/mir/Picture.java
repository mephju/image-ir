package de.ovgu.mir;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Picture {
	private String path;
	private int[] histogram;
    private float q_h;
    private float q_s;
    private float q_v;
	
	private File file;
	
	public Picture(File file, float h_bins, float s_bins, float v_bins) {
		this.file = file;	
		this.q_h = h_bins;
		this.q_s = s_bins;
		this.q_v = v_bins;
		
		histogram = new int[(int) (this.q_h*this.q_s*this.q_v)];
		
		extract(getBufferedImage()); 
	}
	
	public BufferedImage getBufferedImage(){
		BufferedImage bi = null;
		try {
		    bi = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		return bi;
	}
	
    /**
     * Extracts the color histogram from the given image.
     *
     * @param image
     */
    public void extract(BufferedImage image) {
        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB) {
        	System.out.println("Color space not supported. Only RGB.");
        	return;
            
        }
        	
        
        
        int[] pixel = new int[3];
        WritableRaster raster = image.getRaster();

        for (int x = 0; x < image.getWidth() - 40; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                histogram[quant(pixel)]++;
            }
        }
    }
    
    /**
     * Finds corresponding place in a histogram for a pixel
     *
     * @param pixel
     */        
    private int quant(int[] pixel) {
    	//      int qH = (int) Math.floor((pixel[0] * 64f) / 360f);    // more granularity in color
    	//      int qS = (int) Math.floor((pixel[2] * 8f) / 100f);
    	//      return qH * 7 + qS;
    	int qH = (int) Math.floor((pixel[0] * q_h) / 360f);    // more granularity in color
		int qS = (int) Math.floor((pixel[2] * q_s) / 100f);
		int qV = (int) Math.floor((pixel[1] * q_v) / 100f);
		if (qH == q_h) qH = (int) (q_h - 1);
		if (qS == q_s) qS = (int) (q_s - 1);
		if (qV == q_v) qV = (int) (q_v - 1);
		return (qH) * (int) (q_v * q_s) + qS * (int) q_v + qV;
	}
    
    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r
     * @param g
     * @param b
     * @param hsv
     */
    public static void rgb2hsv(int r, int g, int b, int hsv[]) {

        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }
	
	public File getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public int[] getHistogram() {
		return histogram;
	}
	
	public void setHistogram(int[] histogram) {
		this.histogram = histogram;
	}
	
	public float getQ_h() {
		return q_h;
	}

	public void setQ_h(float q_h) {
		this.q_h = q_h;
	}

	public float getQ_s() {
		return q_s;
	}

	public void setQ_s(float q_s) {
		this.q_s = q_s;
	}

	public float getQ_v() {
		return q_v;
	}

	public void setQ_v(float q_v) {
		this.q_v = q_v;
	}

	
	
/*	public static void main(String[] args) {
		File file = new File("data\\abstract_0001.jpg");
		Picture p = new Picture(file, 32, 8, 1);
		
		for (int histvalue : p.histogram) {
			System.out.println(histvalue);
		}
		
	}
*/
}
