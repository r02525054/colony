package com.colonycount.cklab.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;
import android.util.Log;

import com.colonycount.cklab.model.Cluster;
import com.colonycount.cklab.model.Pixel;


public class ImageProcess {
	/**
	 * 
	 * @param hist
	 * @param total
	 * @return
	 */
	public static int otsu(int[] hist, int total){
		float sum = 0;
		for(int i = 0; i < 256; i++){
			sum += i * hist[i];
		}
		
		float sumB = 0;
		int wB = 0;
		int wF = 0;
		
		float varMax = 0;
		int threshold = 0;
		
		for(int i = 0; i < 256; i++){
			wB += hist[i];
			if(wB == 0)
				continue;
			wF = total - wB;
			if(wF == 0)
				break;
			
			sumB += (float) (i * hist[i]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
			if(varBetween > varMax){
				varMax = varBetween;
				threshold = i;
			}
		}
		
		return threshold;
	}
	
	
	/**
	 * 
	 * @param mat
	 * @return
	 */
//	public static ArrayList<Cluster> kmeans(Mat rgba_mat, int trainRadius, int K){
	public static ArrayList<Cluster> kmeans(Bitmap img, int trainRadius, int K){
		//--------------------------------------
		// initialize
		
		// variables
		int 			   iteration   = 0;
		// measure K-means
		double 			   intra	   = 0;
		double 			   inter	   = 0;
		double 			   validity	   = 0;
		// record rgb temp total value
		double[] 		   rTotal	   = new double[K];
		double[] 		   gTotal	   = new double[K];
		double[] 		   bTotal	   = new double[K];
		// save pixels and clusters
		ArrayList<Pixel>   pixelList   = new ArrayList<Pixel>();
		ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
		int[] 			   pixelBelongs;
		
		// initialize temp total value
		for(int i = 0; i < K; i++){
			rTotal[i] = 0;
			gTotal[i] = 0;
			bTotal[i] = 0;
		}
		
//		Mat rgb_mat = new Mat();
//		Mat hsv_mat = new Mat();
//		Imgproc.cvtColor(rgba_mat, rgb_mat, Imgproc.COLOR_RGBA2RGB);
//		Imgproc.cvtColor(rgb_mat, hsv_mat, Imgproc.COLOR_RGB2HSV);
		
//		int height = rgba_mat.height();
//		int width = rgba_mat.width();
		
		int height = img.getHeight();
		int width = img.getWidth();
//		double[] rgb;
//		double[] hsv;
		
		//--------------------------------------
		// add training pixels
		for (int y = height / 2 - trainRadius; y < height / 2 + trainRadius; y++) {
			double xRange = Math.sqrt(trainRadius * trainRadius - Math.pow(y - height / 2, 2));
			for (int x = (int)(width / 2 - xRange); x < (int)(width / 2 + xRange); x++) {
				int color = img.getPixel(x, y);
//				rgb = rgb_mat.get(y, x);
//				hsv = hsv_mat.get(y, x);
//				Pixel pixel = new Pixel(x, y, (int)rgb[0], (int)rgb[1], (int)rgb[2], hsv[0], hsv[1], hsv[2]);
//				pixelList.add(pixel);
			}
		}
		// add cluster
		for(int i = 0; i < K; i++){
			clusterList.add(new Cluster());
		}
		// initialize pixel belongs
		pixelBelongs = new int[pixelList.size()];
		
		
		
		//--------------------------------------
		// main algorithm
//		do {
//			iteration++;
//			Log.d("test", "iteration = " + iteration);
//			// assign new centroid
//			if(iteration == 1) {
//				kmeans_randomInitialCentroid(K, pixelList, clusterList);
//			} else {
//				for(int i = 0; i < K; i++){
//					Cluster cluster = clusterList.get(i);
//					int size = cluster.getSize();
//					double rMean = rTotal[i] / size;
//					double gMean = gTotal[i] / size;
//					double bMean = bTotal[i] / size;
//					
//					Pixel newCentroid = new Pixel((int)rMean, (int)gMean, (int)bMean);
//					cluster.setLastCentroid(cluster.getCentroid());
//					cluster.setCentroid(newCentroid);
//					Log.d("test", "centroid r = " + newCentroid.getR() + ", g = " + newCentroid.getG() + ", b = " + newCentroid.getB());
//					
//					// reset temp total value
//					rTotal[i] = 0;
//					gTotal[i] = 0;
//					bTotal[i] = 0;
//				}
//				intra = 0;
//			}
//				
//			//--------------------------------------
//			// clustering
//			int[] size = new int[K];
//			// 將size初始化為0
//			for(int i = 0; i < K; i++){
//				size[i] = 0;
//			}
//			
//			for(int i = 0; i < pixelList.size(); i++){
//				Pixel pixel = pixelList.get(i);
//				double dMin = 99999999;
//				int index = 99999;
//				
//				// 決定pixel在哪一群
//				for(int j = 0; j < K; j++){
//					Pixel centroid = clusterList.get(j).getCentroid();
//					double d = kmeans_distance(pixel, centroid);
//					
//					if(d < dMin){
//						dMin = d;
//						index = j;
//					}
//				}
//				
//				//將pixel分到該群
//				pixelBelongs[i] = index;
//				size[index]++;
//				
//				//加總該群的rgb值
//				rTotal[index] += pixel.getR();
//				gTotal[index] += pixel.getG();
//				bTotal[index] += pixel.getB();
//				
//				//加總intra
//				intra += dMin;
//			}
//			//計算出intra
//			intra /= pixelList.size();
//			inter = kmeans_countInter(clusterList);
//			validity = intra / inter;
//			
//			// 更新每群的size
//			for(int i = 0; i < clusterList.size(); i++){
//				clusterList.get(i).setSize(size[i]);
//			}
//			
//			Log.d("test", "intra = " + intra + ", inter = " + inter + ", validity = " + validity);
//		} while(!kmeans_converge(clusterList));
//		
//		//--------------------------------------
//		// set cluster pixels
//		for(int i = 0; i < pixelBelongs.length; i++){
//			Pixel pixel = pixelList.get(i);
//			clusterList.get(pixelBelongs[i]).addPixel(pixel);
//		}
		
		return clusterList;
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static double kmeans_distance(Pixel p1, Pixel p2){
		double d = Math.pow(p1.getR()-p2.getR(), 2) + Math.pow(p1.getG()-p2.getG(), 2) + Math.pow(p1.getB()-p2.getB(), 2);
		d = Math.sqrt(d);
		
		return d;
	}
	
	/**
	 * 
	 * @param clusterList
	 * @return
	 */
	private static double kmeans_countInter(ArrayList<Cluster> clusterList){
		double min = 999999;
		for(int i = 0; i < clusterList.size()-1; i++){
			for(int j = i+1; j < clusterList.size(); j++){
				Pixel pi = clusterList.get(i).getCentroid();
				Pixel pj = clusterList.get(j).getCentroid();
				double d = Math.pow(pi.getR()-pj.getR(), 2) + Math.pow(pi.getG()-pj.getG(), 2) + Math.pow(pi.getB()-pj.getB(), 2);
				d = Math.sqrt(d);
				if(d < min)
					min = d;
			}
		}
		
		return min;
	}
	
	/**
	 * 
	 * @param clusterList
	 * @return
	 */
	private static boolean kmeans_converge(ArrayList<Cluster> clusterList){
		for(int i = 0; i < clusterList.size(); i++){
			Pixel centroid = clusterList.get(i).getCentroid();
			Pixel lastCentroid = clusterList.get(i).getLastCentroid();
			if(!kmeans_color_equal(centroid, lastCentroid))
				return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private static boolean kmeans_color_equal(Pixel p1, Pixel p2){
		return p1.getR() == p2.getR() && p1.getG() == p2.getG() && p1.getB() == p2.getB();
	}
	
	/**
	 * 
	 * @param K
	 * @param pixelList
	 * @param clusterList
	 */
	private static void kmeans_randomInitialCentroid(int K, ArrayList<Pixel> pixelList, ArrayList<Cluster> clusterList){
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
	
	
//	public static int[][] findColonyModel(){
//		
//	}
}
