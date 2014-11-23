package com.colonycount.cklab.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.colonycount.cklab.model.Cluster;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.Pixel;

public class CustomKmeans2 {
	private int 				K;
	private ArrayList<Pixel> 	pixelList;
	private ArrayList<Cluster> 	clusterList;
	private double[] 			rTotal;
	private double[] 			gTotal;
	private double[] 			bTotal;
	
	private int 				height;
	private int 				width;
	private int 				trainRadius = 190;
	private int 				testRadius = 220;
	private int 				areaThreshold = 4;
	private double 				shapeFactorThreshold = 0.5;
	private int 				totalCount;
	private int 				iteration;
	private double 				intra;
	private double 				inter;
	private double 				validity;
	
	private int 				startX;
	private int 				endX;
	private int 				startY;
	private int 				endY;
	
	private double 				computeTime;
	
	private Bitmap 				rawImg;
	private Bitmap 				result;
	private Bitmap				markedResult;
	/**
	 * initialize K, pixelList, clusterList
	 * 
	 */
	public CustomKmeans2(Bitmap rawImg, int K){
		this.rawImg = rawImg;
		this.K 		= K;
		this.width = rawImg.getWidth();
		this.height = rawImg.getHeight();
		this.result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		this.markedResult = rawImg.copy(Config.ARGB_8888, true);
	}
	
	/**
	 * 
	 * @param image
	 * @return
	 */
	public List<Component> count(){
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
		setColony();
		List<Component> components = label();
		Log.d("test", "components size = " + components.size());
//		markColony(components);
		
		long endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		setComputeTime(time/1000);
		validity = Math.round(validity * 1000) / 1000.0;
		
		return components;
	}
	
	public void init(){
		pixelList 	= new ArrayList<Pixel>();
		clusterList = new ArrayList<Cluster>();
		
		rTotal 		= new double[K];
		gTotal 		= new double[K];
		bTotal 		= new double[K];
	}
	
//	public void setTrainingRange(int startX, int endX, int startY, int endY){
//		this.startX = startX;
//		this.endX = endX;
//		this.startY = startY;
//		this.endY = endY;
//	}
	
	/**
	 * add rectangle image data
	 * @param pixelList
	 * @param image
	 */
	public void addData(){
		float[] hsv = new float[3];
		
		// add training pixels
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
		
		for (int y = height / 2 - trainRadius; y < height / 2 + trainRadius; y++) {
			double xRange = Math.sqrt(trainRadius * trainRadius - Math.pow(y - height / 2, 2));
			for (int x = (int)(width / 2 - xRange); x < (int)(width / 2 + xRange); x++) {
				int color = rawImg.getPixel(x, y);
				int r = Color.red(color);
				int g = Color.green(color);
				int b = Color.blue(color);
				Color.colorToHSV(color, hsv);
				float h = hsv[0];
				float s = hsv[1];
				float v = hsv[2];
				pixelList.add(new Pixel(x, y, r, g, b, h, s, v));
			}
		}
		
		// add cluster
		for(int i = 0; i < K; i++){
			clusterList.add(new Cluster());
		}
	}
	
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
		// �Ȯ��x�s�C��cluster size
		int[] clusterSize = new int[K];
		// �Nsize������l�Ƭ�0
		for(int i = 0; i < clusterSize.length; i++){
			clusterSize[i] = 0;
		}
		
		// ���y�����n���s��pixel
		for(int i = 0; i < pixelList.size(); i++){
			Pixel pixel = pixelList.get(i);
			double distanceMin = 99999999;
			int clusterNum = 99999;
			
			// �M�wpixel�b���@�s
			for(int j = 0; j < K; j++){
				Pixel centroid = clusterList.get(j).getCentroid();
				double d = distance(pixel, centroid);
				if(d < distanceMin){
					distanceMin = d;
					clusterNum = j;
				}
			}
			
			//�Npixel����Ӹs
			pixel.setBelong(clusterNum);
			clusterSize[clusterNum]++;
			
			//�[�`�Ӹs��rgb��
			rTotal[clusterNum] += pixel.getR();
			gTotal[clusterNum] += pixel.getG();
			bTotal[clusterNum] += pixel.getB();
			
			//�[�`intra
			intra += distanceMin;
		}
		
		//�p��Xintra - �C��cluster�̪�pixel���ۤv�s���ߪ������Z��
		intra /= pixelList.size();
		inter = countInter();
		validity = intra / inter;
		
		// ��s�C�s��size
		for(int i = 0; i < clusterList.size(); i++){
			clusterList.get(i).setSize(clusterSize[i]);
		}
		
		Log.d("test", "intra = " + intra + ", inter = " + inter + ", validity = " + validity);
	}
	
	/**
	 * �p��cluster centroid�����̤p�Z��
	 * @return cluster centroid�����̤p�Z��
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
	
	
	public void setColony(){
		result.eraseColor(Color.BLACK);
		int index = 999;
		int intensityMin = 9999;
		for(int i = 0; i < clusterList.size(); i++){
			Pixel centroid = clusterList.get(i).getCentroid();
			int intensity = (centroid.getR() + centroid.getG() + centroid.getB()) / 3;
			// 選出亮度最高的centroid那群
			if(intensity < intensityMin){
				intensityMin = intensity;
				index = i;
			}
		}
		
		// 用加入training的object來分
//		for(int i = 0; i < pixelList.size(); i++){
//			Pixel pixel = pixelList.get(i);
//			if(pixel.getBelong() == index){
//				result.setPixel(pixel.getX(), pixel.getY(), Color.WHITE);
//			}
//		}
		
		// 用testRadius來決定二元化的範圍
		for (int y = height / 2 - testRadius; y < height / 2 + testRadius; y++) {
			double xRange = Math.sqrt(testRadius * testRadius - Math.pow(y - height / 2, 2));
			for (int x = (int)(width / 2 - xRange); x < (int)(width / 2 + xRange); x++) {
				int color = rawImg.getPixel(x, y);
				int r = Color.red(color);
				int g = Color.green(color);
				int b = Color.blue(color);
				int clusterNo = 99999;
				double dMin = 99999;
				for(int i = 0; i < clusterList.size(); i++){
					Cluster cluster = clusterList.get(i);
					Pixel centroid = cluster.getCentroid();
					double d = Math.pow(centroid.getR() - r, 2) + Math.pow(centroid.getG() - g, 2) + Math.pow(centroid.getB() - b, 2);
					d = Math.sqrt(d);
					if(d < dMin){
						dMin = d;
						clusterNo = i;
					}
				}
				
				if(clusterNo == index)
					result.setPixel(x, y, Color.WHITE);
			}
		}
	}
	
	public List<Component> label(){
		Label2 label = new Label2(result, testRadius);
		label.labelConnectedComponents();
		List<Component> components = label.getComponents();
		
		return components;
	}
	
	public Bitmap getResult(){
		return result;
	}
	
//	public void markColony(List<Component> components){
//		Canvas canvas = new Canvas(markedResult);
//		Paint paint = new Paint();
//		
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setStrokeWidth(2);
//		paint.setAntiAlias(true);
//		
//		int redCount = 0;
//		int greenCount = 0;
//		
//		for(int i = 0; i < components.size(); i++){
//			Component component = components.get(i);
//			if(component.getArea() >= areaThreshold && component.getShapeFactor() >= shapeFactorThreshold) {
//				paint.setColor(Color.RED);
//				redCount++;
//			} else {
//				paint.setColor(Color.GREEN);
//				greenCount++;
//			}
//			canvas.drawCircle((float)component.getCenterX(), (float)component.getCenterY(), (float)component.getRadius(), paint);
//		}
//		
//		double total = redCount * 240.0 * 240.0 / testRadius / testRadius;
//		totalCount = (int) total;
//		Log.d("test", "redCount:" + redCount + ",greenCount:" + greenCount);
//		Log.d("timer", "total = " + (int)total);
//		Log.d("timer", "iteration = " + iteration);
//	}
}
