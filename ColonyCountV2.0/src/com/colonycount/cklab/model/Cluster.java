package com.colonycount.cklab.model;

import java.util.ArrayList;

public class Cluster {
	private Pixel 			 centroid;
	private Pixel 			 lastCentroid;
	private int 			 size;
	private ArrayList<Pixel> pixels;
	
	public Cluster(){
		centroid 	 = new Pixel();
		lastCentroid = new Pixel();
		pixels 		 = new ArrayList<Pixel>();
	}

	public Pixel getCentroid() {
		return centroid;
	}

	public void setCentroid(Pixel centroid) {
		this.centroid = centroid;
	}

	public Pixel getLastCentroid() {
		return lastCentroid;
	}

	public void setLastCentroid(Pixel lastCentroid) {
		this.lastCentroid = lastCentroid;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void addPixel(Pixel pixel){
		pixels.add(pixel);
	}
	
	public ArrayList<Pixel> getPixels(){
		return pixels;
	}
}