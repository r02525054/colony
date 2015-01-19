package com.colonycount.cklab.model;

public class Pixel {
	/**
	 * 
	 */
	private int x;
	private int y;
	/**
	 * 
	 */
	private int r;
	private int g;
	private int b;
	/**
	 * 
	 */
	private double h;
	private double s;
	private double v;
	
	private double intensity;
	
	private int belong;
	
	public Pixel(){
		
	}
	
	public Pixel(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Pixel(int r, int g, int b){
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Pixel(int x, int y, int r, int g, int b){
		this.x = x;
		this.y = y;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Pixel(int x, int y, double intensity){
		this.x = x;
		this.y = y;
		this.intensity = intensity;
	}
	
	public Pixel(int x, int y, int r, int g, int b, double h, double s, double v){
		this.x = x;
		this.y = y;
		this.r = r;
		this.g = g;
		this.b = b;
		this.h = h;
		this.s = s;
		this.v = v;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
	}
	
	public double getH() {
		return h;
	}

	public double getS() {
		return s;
	}

	public double getV() {
		return v;
	}

	public int getBelong() {
		return belong;
	}

	public void setBelong(int belong) {
		this.belong = belong;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
}
