package com.colonycount.cklab.asynctask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.colonycount.cklab.base.BaseAsyncTask;
import com.colonycount.cklab.config.Config;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.Pixel;
import com.colonycount.cklab.utils.MyImageProcess;

public class AddColonyAsyncTask extends BaseAsyncTask {
	private Pixel centerPixel = new Pixel(Config.OUTPUT_IMAGE_WIDTH/2, Config.OUTPUT_IMAGE_HEIGHT/2);
	
	private double dis_threshold = 20;
	
	private int r_threshold = 30;
	
	private Bitmap rawImg;
	
	public AddColonyAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls, showDialog);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 */
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		AsyncTaskPayload paramPayload = params[0];
		rawImg = paramPayload.getRawImg();
		Pixel hitPixel = paramPayload.getHitPixel();
		
		Component colonyComponent = regionGrowing(hitPixel);
		
//		int area = colonyComponent.getArea();
//		int centerX = colonyComponent.getCenterX();
//		int centerY = colonyComponent.getCenterY();
//		int r = (int) colonyComponent.getRadius();
//		Log.d("test4", "area = " + area + ", centerX = " + centerX + ", centerY = " + centerY + ", r = " + r);
		
		AsyncTaskPayload resultPayload = new AsyncTaskPayload();
		resultPayload.setComponent(colonyComponent);
//		resultPayload.setRawImg(params[0].getRawImg());
		
		return resultPayload;
		
//		return paramPayload;
	}
	
	// use RGB as connected region threshold
	public Component regionGrowing(Pixel hitPixel){
		List<Pixel> L = new ArrayList<Pixel>();
		Component component = new Component();
		Map<String, Boolean> label = new HashMap<String, Boolean>();
		
		// add hit pixel
		L.add(hitPixel);
		component.addPixel(hitPixel);
		
		// use BFS search
		while(!L.isEmpty()){
			Pixel pixel = L.remove(0);
			int px = pixel.getX();
			int py = pixel.getY();
			int pr = pixel.getR();
			int pg = pixel.getG();
			int pb = pixel.getB();
			
//			Log.d("test4", "r = " + pr + ", g = " + pg + ", b = " + pb);
			int xMin = 1000000;
			int xMax = -1;
			int yMin = 1000000;
			int yMax = -1;
			if(px < xMin)
				xMin = px;
			if(px > xMax)
				xMax = px;
			if(py < yMin)
				yMin = py;
			if(py > yMax)
				yMax = py;
			
			// check top pixel
			// 1. check distance
			if(inBound(px, py-1) && inDistanceRange(px, py-1, hitPixel)){
				// 2. check connectivity
				int colorTop = rawImg.getPixel(px, py-1);
				String coordinateTop = px + "," + (py-1);
				Pixel pTop = new Pixel(px, py-1, Color.red(colorTop), Color.green(colorTop), Color.blue(colorTop));
				
//				Log.d("test4", "top r = " + Color.red(colorTop) + ", g = " + Color.green(colorTop) + ", b = " + Color.blue(colorTop));
				
				if(!labeled(coordinateTop, label) && MyImageProcess.RGBDistance(pixel, pTop) < dis_threshold){
					L.add(pTop);
					component.addPixel(pTop);
					label.put(coordinateTop, true);
				}
			}
			
			// check right pixel
			// 1. check distance
			if(inBound(px+1, py) && inDistanceRange(px+1, py, hitPixel)){
				// 2. check connectivity
				int colorRight = rawImg.getPixel(px+1, py);
				String coordinateRight = (px+1) + "," + py;
				Pixel pRight = new Pixel(px+1, py, Color.red(colorRight), Color.green(colorRight), Color.blue(colorRight));
				
//				Log.d("test4", "right r = " + Color.red(colorRight) + ", g = " + Color.green(colorRight) + ", b = " + Color.blue(colorRight));
				if(!labeled(coordinateRight, label) && MyImageProcess.RGBDistance(pixel, pRight) < dis_threshold){
					L.add(pRight);
					component.addPixel(pRight);
					label.put(coordinateRight, true);
				}
			}
			
			// check bottom pixel
			// 1. check distance
			if(inBound(px, py+1) && inDistanceRange(px, py+1, hitPixel)){
				// 2. check connectivity
				int colorBot = rawImg.getPixel(px, py+1);
				String coordinateBot = px + "," + (py+1);
				Pixel pBot = new Pixel(px, py+1, Color.red(colorBot), Color.green(colorBot), Color.blue(colorBot));
				
//				Log.d("test4", "bot r = " + Color.red(colorBot) + ", g = " + Color.green(colorBot) + ", b = " + Color.blue(colorBot));
				if(!labeled(coordinateBot, label) && MyImageProcess.RGBDistance(pixel, pBot) < dis_threshold){
					L.add(pBot);
					component.addPixel(pBot);
					label.put(coordinateBot, true);
				}
			}
			
			// check left pixel
			// 1. check distance
			if(inBound(px-1, py) && inDistanceRange(px-1, py, hitPixel)){
				// 2. check connectivity
				int colorLeft = rawImg.getPixel(px-1, py);
				String coordinateLeft = (px-1) + "," + py;
				Pixel pLeft = new Pixel(px-1, py, Color.red(colorLeft), Color.green(colorLeft), Color.blue(colorLeft));
				
//				Log.d("test4", "left r = " + Color.red(colorLeft) + ", g = " + Color.green(colorLeft) + ", b = " + Color.blue(colorLeft));
				if(!labeled(coordinateLeft, label) && MyImageProcess.RGBDistance(pixel, pLeft) < dis_threshold){
					L.add(pLeft);
					component.addPixel(pLeft);
					label.put(coordinateLeft, true);
				}
			}
			
			if(L.isEmpty()){
				component.setCenterX((xMin+xMax)/2);
				component.setCenterY((yMin+yMax)/2);
				component.setProperties();
			}
		}
		
		return component;
	}
	
//	public boolean inBound(Pixel pixel){
//		if(MyImageProcess.coordinateDistance(pixel, centerPixel) <= Config.OUTPUT_IMAGE_WIDTH/2)
//			return true;
//		else 
//			return false;
//	}
	
	public boolean inBound(int x, int y){
		double d = Math.pow(x-centerPixel.getX(), 2) + Math.pow(y-centerPixel.getY(), 2);
		d = Math.sqrt(d);
		
		if(d <= Config.OUTPUT_IMAGE_WIDTH/2)
			return true;
		else 
			return false;
	}
	
	public boolean inDistanceRange(int x, int y, Pixel hitPixel){
		double d = Math.pow(x-hitPixel.getX(), 2) + Math.pow(y-hitPixel.getY(), 2);
		d = Math.sqrt(d);
		
//		Log.d("test4", "d = " + d);
		if(d <= r_threshold){
//			Log.d("test4", "in distance range");
			return true;
		}
		else 
			return false;
	}
	
//	public boolean inDistanceRange(Pixel p1, Pixel p2){
//		if(MyImageProcess.coordinateDistance(p1, p2) <= r_threshold)
//			return true;
//		else 
//			return false;
//	}
	
	public boolean labeled(String coordinate, Map<String, Boolean> label){
		return label.containsKey(coordinate);
	}
}
