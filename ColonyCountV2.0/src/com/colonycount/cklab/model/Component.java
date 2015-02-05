package com.colonycount.cklab.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.colonycount.cklab.config.Config;

import android.util.Log;

public class Component implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 判斷colony的主要feature
	// 1.面積
	private int area = 0;
	// 2.形狀指數
	private double shapeFactor;
	// 3.平均亮度
	private double meanIntensity;
	// 4.平均R, 平均G, 平均B
	private double meanR;
	private double meanG;
	private double meanB;
	
	// 其他資訊
	// 周長
	private int perimeter = 0;
	// 直徑, 半徑
	private int diameter;
	private int radius;
	// 中心點X, 中心點Y
	private int centerX;
	private int centerY;
	
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	
	// 紀錄此component裡的pixel
	// 邊界pixel
	private List<Map<String, Integer>> boundPixels = new ArrayList<Map<String,Integer>>();
	// 所有pixel
	private List<Pixel> pixels = new ArrayList<Pixel>();
	
	
	public void incrementArea(){
		area++;
	}
	
	public int getArea() {
		return area;
	}
	
	public void addPerimeter(int periNumber){
		this.perimeter += periNumber;
	}
	
	public int getPerimeter() {
		return perimeter;
	}
	
	public void setProperties(){
//		area = pixels.size();

		// 因為output image size = 1024x1024
		// count image size = 512x512
		// 要把所有長度資訊x2, 面積資訊x4才正確
		int scale = Config.OUTPUT_IMAGE_WIDTH / Config.COUNT_IMAGE_WIDTH; 
		
		// 面積x4
		area *= scale * scale;
		// 半徑
		double radiusTemp = Math.sqrt(area / Math.PI);
		radius = (int) Math.round(radiusTemp);
		// 直徑
		diameter = radius * 2;
		// 周長x2
		perimeter *= scale;
		// x, y x2
		centerX *= scale;
		centerY *= scale;
		
		shapeFactor = 4 * Math.PI * area / Math.pow(perimeter, 2);
		shapeFactor = (double)Math.round(shapeFactor * 100) / 100.0;
	}
	
	public int getDiameter() {
		return diameter;
	}
	
	public void setRadius(int radius){
		this.radius = radius;
	}
	
	public int getRadius(){
		return radius;
	}
	
	public double getShapeFactor() {
		return shapeFactor;
	}

	public int getCenterX() {
		return centerX;
	}

	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}
	
	public void addBoundPixel(Map<String, Integer> pixel){
		boundPixels.add(pixel);
	}
	
	public List<Map<String, Integer>> getBoundPixels(){
		return boundPixels;
	}
	
	public void addPixel(Pixel pixel){
		this.pixels.add(pixel);
	}
	
	public List<Pixel> getPixels(){
		return pixels;
	}
	
	public void setMeanValues(){
		int total = pixels.size();
		int r_sum = 0;
		int g_sum = 0;
		int b_sum = 0;
		for(int i = 0; i < total; i++){
			Pixel pixel = pixels.get(i);
			r_sum += pixel.getR();
			g_sum += pixel.getG();
			b_sum += pixel.getB();
		}
		
		// set values
		meanR = r_sum / total;
		meanG = g_sum / total;
		meanB = b_sum / total;
		meanIntensity = (r_sum + g_sum + b_sum) / 3 / total;
	}

	public double getMeanIntensity() {
		return meanIntensity;
	}

	public double getMeanR() {
		return meanR;
	}

	public double getMeanG() {
		return meanG;
	}

	public double getMeanB() {
		return meanB;
	}

	public int getxMin() {
		return xMin;
	}

	public void setxMin(int xMin) {
		this.xMin = xMin;
	}

	public int getxMax() {
		return xMax;
	}

	public void setxMax(int xMax) {
		this.xMax = xMax;
	}

	public int getyMin() {
		return yMin;
	}

	public void setyMin(int yMin) {
		this.yMin = yMin;
	}

	public int getyMax() {
		return yMax;
	}

	public void setyMax(int yMax) {
		this.yMax = yMax;
	}
}