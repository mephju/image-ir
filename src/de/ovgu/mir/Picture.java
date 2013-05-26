package de.ovgu.mir;

import java.io.File;
import java.util.HashMap;

public class Picture {
	private String path;
	private HashMap<HSV, Double> histogram;
	
	private File file;
	
	public Picture(File file) {
	this.file = file;	
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
