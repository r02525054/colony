package com.colonycount.cklab.model;

public class CancelView {
	private float x;
	private float y;
	private float r;
	private Type type;
	
	public enum Type {
		RED, GREEN, PURPLE
	}
	
	public CancelView(float x, float y, float r, Type type){
		this.x = x;
		this.y = y;
		this.r = r;
		this.type = type;
	}
	
	
	public Type getType(){
		return type;
	}
	public void setType(Type type){
		this.type = type;
	}


	public float getX() {
		return x;
	}


	public void setX(float x) {
		this.x = x;
	}


	public float getY() {
		return y;
	}


	public void setY(float y) {
		this.y = y;
	}


	public float getR() {
		return r;
	}


	public void setR(float r) {
		this.r = r;
	}
}
