package com.colonycount.cklab.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.colonycount.cklab.model.Component;

public class Label2 {
	private Bitmap kmeansImg;
	private int width;
	private int height;
	private int yMargin;
	private int xMargin;
	private int radius;
	
	private int K = 0;
	private List<Map<String, Integer>> L;
	private int[][] label;
	private List<Component> components;
	
	public Label2(Bitmap kmeansImg, int radius){
		this.kmeansImg = kmeansImg;
		this.width = this.kmeansImg.getWidth();
		this.height = this.kmeansImg.getHeight();
		this.yMargin = this.height / 2 - radius;
		this.xMargin = this.width / 2 - radius;
		this.radius = radius;
		
		label = new int[radius*2][radius*2];
		L = new ArrayList<Map<String, Integer>>();
		components = new ArrayList<Component>();
	}
	
	public int labelConnectedComponents(){
		for(int y = height/2 - radius; y < height/2 + radius; y++){
			for(int x = width / 2 - radius; x < width / 2 + radius; x++){
				int color = kmeansImg.getPixel(x, y);
				
				if(color == Color.WHITE && label[y-yMargin][x-xMargin] == 0){
					K++;
					labelSelfAndAdd4AdjacentNeighbors(x, y);
					Component component = new Component();
					component.incrementArea();
					// for set component property
					int neighborNumber = 4;
					int whiteNeighborNumber = 0;
					int xMin = Integer.MAX_VALUE;
					int xMax = Integer.MIN_VALUE;
					int yMin = Integer.MAX_VALUE;
					int yMax = Integer.MIN_VALUE;
					if(x < xMin)
						xMin = x;
					if(x > xMax)
						xMax = x;
					if(y < yMin)
						yMin = y;
					if(y > yMax)
						yMax = y;
					
					
//					Map<String, Integer> pixel = getPixel(x, y, color);
//					boolean isBound = false;
					
					while(!L.isEmpty()){
						Map<String, Integer> neighbor = L.remove(L.size()-1);
						int neighborX = neighbor.get("x");
						int neighborY = neighbor.get("y");
						int neighborColor = neighbor.get("color");
						
						if(neighborColor == Color.WHITE){
							whiteNeighborNumber++;
							
							if(label[neighborY-yMargin][neighborX-xMargin] == 0){
								labelSelfAndAdd4AdjacentNeighbors(neighborX, neighborY);
								component.incrementArea();
								if(neighborX < xMin)
									xMin = neighborX;
								if(neighborX > xMax)
									xMax = neighborX;
								if(neighborY < yMin)
									yMin = neighborY;
								if(neighborY > yMax)
									yMax = neighborY;
							}
						} else {
//							if(!isBound){
//								component.addBoundPixel(pixel);
//							}
						}
						
						neighborNumber--;
						// when finish checking 4 neighbors, count for perimeter
						if(neighborNumber == 0){
							int periNumber = 4 - whiteNeighborNumber;
							component.addPerimeter(periNumber);
							neighborNumber = 4;
							whiteNeighborNumber = 0;
							
//							pixel = neighbor;
//							isBound = false;
						}
						
						// when L size equal to 0, end this component and add to components 
						if(L.size() == 0){
							component.setCenterX((xMin+xMax)/2);
							component.setCenterY((yMin+yMax)/2);
							component.setProperties();
							components.add(component);
						}
					}
				}
			}
		}
		
		return K;
	}
	
	public void labelSelfAndAdd4AdjacentNeighbors(int x, int y){
		// label self
		label[y-yMargin][x-xMargin] = K;
		// add north neighbor
		L.add(getPixel(x, y-1, kmeansImg.getPixel(x, y-1)));
		// add east neighbor
		L.add(getPixel(x+1, y, kmeansImg.getPixel(x+1, y)));
		// add south neighbor
		L.add(getPixel(x, y+1, kmeansImg.getPixel(x, y+1)));
		// add west neighbor
		L.add(getPixel(x-1, y, kmeansImg.getPixel(x-1, y)));
	}
	
	public Map<String, Integer> getPixel(int x, int y, int color){
		Map<String, Integer> pixel = new HashMap<String, Integer>();
		pixel.put("x", x);
		pixel.put("y", y);
		pixel.put("color", color);
		
		return pixel;
	}
	
	public List<Component> getComponents(){
		return components;
	}
}