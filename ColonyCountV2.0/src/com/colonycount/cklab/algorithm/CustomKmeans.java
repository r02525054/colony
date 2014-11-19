package com.colonycount.cklab.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.colonycount.cklab.model.Cluster;
import com.colonycount.cklab.model.Pixel;

public class CustomKmeans {
	private Bitmap rawImg;
	private Bitmap finalResult;
	
	private int K;
	private int trainRadius = 190;
	private int testRadius = 190;
	private int height;
	private int width;
	private ArrayList<Pixel> 	pixelList;
	private ArrayList<Cluster> 	clusterList;
	private double[] 			rTotal;
	private double[] 			gTotal;
	private double[] 			bTotal;
	private int iteration;
	private double intra;
	private double inter;
	private double validity;
	private int startX;
	private int endX;
	private int startY;
	private int endY;
	private double computeTime;
	
	
	public CustomKmeans(Bitmap rawImg, int K){
		this.K = K;
		this.rawImg = rawImg;
		this.width = rawImg.getWidth();
		this.height = rawImg.getHeight();
	}
	
	/**
	 * 
	 * @param image
	 * @return
	 */
	public ArrayList<Cluster> kmeans(){
		long startTime = System.currentTimeMillis();
		
		init();
		addData();
		do {
			iteration++;
			Log.d("test", "iteration = " + iteration);
			assignNewCentroids();
			resetInfoValue();
			clustering();
		} while(!converge());
		setClusterPixels();
		
		long endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		setComputeTime(time/1000);
		
		return clusterList;
	}
	
	public void init(){
		pixelList 	= new ArrayList<Pixel>();
		clusterList = new ArrayList<Cluster>();
		
		rTotal 		= new double[K];
		gTotal 		= new double[K];
		bTotal 		= new double[K];
	}
	
	public void setTrainingRange(int startX, int endX, int startY, int endY){
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
	}
	
	public void addData(){
		for (int y = height / 2 - trainRadius; y < height / 2 + trainRadius; y++) {
			double xRange = Math.sqrt(trainRadius * trainRadius - Math.pow(y - height / 2, 2));
			for (int x = (int)(width / 2 - xRange); x < (int)(width / 2 + xRange); x++) {
				int color = rawImg.getPixel(x, y);
				Pixel pixel = new Pixel(x, y, Color.red(color), Color.green(color), Color.blue(color));
				pixelList.add(pixel);
			}
		}
		
		// add cluster
		for(int i = 0; i < K; i++){
			clusterList.add(new Cluster());
		}
	}
	
	/**
	 * add rectangle image data
	 * @param pixelList
	 * @param image
	 */
//	public void addData(Bitmap image){
//		float[] hsv = new float[3];
//		
//		// add training pixels
//		for(int y = startY; y < endY; y++){
//			for(int x = startX; x < endX; x++){
//				int color = image.getPixel(x, y);
//				int r = Color.red(color);
//				int g = Color.green(color);
//				int b = Color.blue(color);
//				Color.colorToHSV(color, hsv);
//				float h = hsv[0];
//				float s = hsv[1];
//				float v = hsv[2];
//				pixelList.add(new Pixel(x, y, r, g, b, h, s, v));
//			}
//		}
//		
//		// add cluster
//		for(int i = 0; i < K; i++){
//			clusterList.add(new Cluster());
//		}
//	}
	
	public void assignNewCentroids(){
		// initial centroids
		if(iteration == 1) {
			randomInitialCentroid();
		} else {
			for(int i = 0; i < clusterList.size(); i++){
				Cluster cluster = clusterList.get(i);
				int size = cluster.getSize();
				double rMean = rTotal[i] / size;
				double gMean = gTotal[i] / size;
				double bMean = bTotal[i] / size;
				
				Pixel newCentroid = new Pixel((int)rMean, (int)gMean, (int)bMean);
				cluster.setLastCentroid(cluster.getCentroid());
				cluster.setCentroid(newCentroid);
				Log.d("test", "centroid r = " + newCentroid.getR() + ", g = " + newCentroid.getG() + ", b = " + newCentroid.getB());
			}
		}
	}
	
	public void randomInitialCentroid() {
		Set<Integer> indexs = new LinkedHashSet<Integer>();
		Random r = new Random();
		while(indexs.size() < K){
			int index = r.nextInt(pixelList.size());
			indexs.add(index);
		}
		
		Iterator<Integer> it = indexs.iterator();
		
		for(int i = 0; i < K; i++){
			Pixel centroid = pixelList.get(it.next());
			clusterList.get(i).setCentroid(centroid);
			Log.d("test", "centroid r = " + centroid.getR() + ", g = " + centroid.getG() + ", b = " + centroid.getB());
		}
	}
	
	public void resetInfoValue(){
		for(int i = 0; i < K; i++){
			rTotal[i] = 0;
			gTotal[i] = 0;
			bTotal[i] = 0;
			intra = 0;
		}
	}
	
	

	public void clustering(){
		// 暫時儲存每個cluster size
		int[] clusterSize = new int[K];
		// 將size全部初始化為0
		for(int i = 0; i < clusterSize.length; i++){
			clusterSize[i] = 0;
		}
		
		// 掃描全部要分群的pixel
		for(int i = 0; i < pixelList.size(); i++){
			Pixel pixel = pixelList.get(i);
			double distanceMin = 99999999;
			int clusterNum = 99999;
			
			// 決定pixel在哪一群
			for(int j = 0; j < K; j++){
				Pixel centroid = clusterList.get(j).getCentroid();
				double d = distance(pixel, centroid);
				if(d < distanceMin){
					distanceMin = d;
					clusterNum = j;
				}
			}
			
			//將pixel分到該群
			pixel.setBelong(clusterNum);
			clusterSize[clusterNum]++;
			
			//加總該群的rgb值
			rTotal[clusterNum] += pixel.getR();
			gTotal[clusterNum] += pixel.getG();
			bTotal[clusterNum] += pixel.getB();
			
			//加總intra
			intra += distanceMin;
		}
		
		//計算出intra - 每個cluster裡的pixel離自己群中心的平均距離
		intra /= pixelList.size();
		inter = countInter();
		validity = intra / inter;
		
		// 更新每群的size
		for(int i = 0; i < clusterList.size(); i++){
			clusterList.get(i).setSize(clusterSize[i]);
		}
		
		Log.d("test", "intra = " + intra + ", inter = " + inter + ", validity = " + validity);
	}
	
	/**
	 * 計算cluster centroid間的最小距離
	 * @return cluster centroid間的最小距離
	 */
	public double countInter(){
		double min = 999999;
		for(int i = 0; i < clusterList.size()-1; i++){
			for(int j = i+1; j < clusterList.size(); j++){
				Pixel pi = clusterList.get(i).getCentroid();
				Pixel pj = clusterList.get(j).getCentroid();
				double d = distance(pi, pj);
				if(d < min)
					min = d;
			}
		}
		
		return min;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean converge(){
		for(int i = 0; i < clusterList.size(); i++){
			Pixel centroid = clusterList.get(i).getCentroid();
			Pixel lastCentroid = clusterList.get(i).getLastCentroid();
			if(!equal(centroid, lastCentroid))
				return false;
		}
		
		return true;
	}
	
	public boolean equal(Pixel p1, Pixel p2){
		return p1.getR() == p2.getR() && p1.getG() == p2.getG() && p1.getB() == p2.getB();
	}
	
	public double distance(Pixel p1, Pixel p2){
		double d = Math.pow(p1.getR()-p2.getR(), 2) + Math.pow(p1.getG()-p2.getG(), 2) + Math.pow(p1.getB()-p2.getB(), 2);
		d = Math.sqrt(d);
		
		return d;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getComputeTime(){
		return computeTime;
	}
	
	public void setComputeTime(double computeTime){
		this.computeTime = computeTime;
	}
	
	public void setClusterPixels(){
		for(int i = 0; i < pixelList.size(); i++){
			Pixel pixel = pixelList.get(i);
			int clusterNumber = pixel.getBelong();
			clusterList.get(clusterNumber).addPixel(pixel);
		}
	}
}
