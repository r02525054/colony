package com.colonycount.cklab.asynctask.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.ImgSearchFilter;
import com.colonycount.cklab.model.Pixel;

public class AsyncTaskPayload {
	private Map<String, String> params;
	private byte[] imgData;
	private Bitmap rawImg;
	private List<Component> components;
	private Component component;
	private Pixel hitPixel;
	private JSONArray imageInfoList;
	private JSONObject imageInfoObj;
	private ImgSearchFilter imgSearchFilter;
	
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

	public JSONArray getImageInfoList() {
		return imageInfoList;
	}

	public void setImageInfoList(JSONArray imageInfoList) {
		this.imageInfoList = imageInfoList;
	}
	
	public JSONObject getImageInfoObj() {
		return imageInfoObj;
	}
	
	public void setImageInfoObj(JSONObject imageInfoObj) {
		this.imageInfoObj = imageInfoObj;
	}

	public ImgSearchFilter getImgSearchFilter() {
		return imgSearchFilter;
	}

	public void setImgSearchFilter(ImgSearchFilter imgSearchFilter) {
		this.imgSearchFilter = imgSearchFilter;
	}
}