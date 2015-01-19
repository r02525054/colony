package com.colonycount.cklab.asynctask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.Pixel;

public class AsyncTaskPayload {
	private Map<String, String> params;
	private byte[] imgData;
	private Bitmap rawImg;
	private List<Component> components;
	private Component component;
	private Pixel hitPixel;
	
	public AsyncTaskPayload(){
		this.params = new HashMap<String, String>();
	}
	
	public String getValue(String key){
		return params.get(key);
	}
	
	public void putValue(String key, String value){
		params.put(key, value);
	}

	public void setImgData(byte[] imgData) {
		this.imgData = imgData;
	}

	public void setRawImg(Bitmap rawImg) {
		this.rawImg = rawImg;
	}

	public byte[] getImgData() {
		return imgData;
	}

	public Bitmap getRawImg() {
		return rawImg;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public void setComponent(Component component){
		this.component = component;
	}
	
	public Component getComponent(){
		return component;
	}

	public Pixel getHitPixel() {
		return hitPixel;
	}

	public void setHitPixel(Pixel hitPixel) {
		this.hitPixel = hitPixel;
	}
}