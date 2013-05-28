package de.ovgu.mir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Picture {
	private String path;
	private HashMap<HSV, Double> histogram;
	
	private File file;
	
	public Picture(File file) {
		this.file = file;	
	}
	
	private BufferedImage getBufferedImage(File lFile){
		BufferedImage bi = null;
		try {
		    bi = ImageIO.read(lFile);
		} catch (IOException e) {
		}
		return bi;
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
	public HashMap<HSV, Double> getHistogram() {
		return histogram;
	}
	public void setHistogram(HashMap<HSV, Double> histogram) {
		this.histogram = histogram;
	}
	
}
