package com.colonycount.cklab.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Component implements Serializable {
	// �P�_colony���D�nfeature
	// 1.���n
	private int area = 0;
	// 2.�Ϊ����
	private double shapeFactor;
	// 3.�����G��
	private double meanIntensity;
	// 4.����R, ����G, ����B
	private double meanR;
	private double meanG;
	private double meanB;
	
	// ��L��T
	// �P��
	private int perimeter = 0;
	// ���|, �b�|
	private double diameter;
	private double radius;
	// �����IX, �����IY
	private int centerX;
	private int centerY;
	
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	
	// ����component�̪�pixel
	// ���pixel
	private List<Map<String, Integer>> boundPixels = new ArrayList<Map<String,Integer>>();
	// �Ҧ�pixel
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
		diameter = 2 * Math.sqrt(area / Math.PI); 
		diameter = (double)Math.round(diameter * 100) / 100.0;
		radius = diameter / 2;
		shapeFactor = 4 * Math.PI * area / Math.pow(perimeter, 2);
		shapeFactor = (double)Math.round(shapeFactor * 100) / 100.0;
		
		Log.d("test", "area = " + area + ", diameter = " + diameter + ", perimeter = " + perimeter + ", shapeFactor = " + shapeFactor);
//		setMeanValues();
	}
	
	public double getDiameter() {
		return diameter;
	}
	
	public void setRadius(double radius){
		this.radius = radius;
	}
	
	public double getRadius(){
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