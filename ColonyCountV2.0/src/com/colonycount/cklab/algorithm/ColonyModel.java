package com.colonycount.cklab.algorithm;

import java.util.ArrayList;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;

import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.Pixel;
import com.colonycount.cklab.utils.BitmapUtil;
import com.colonycount.cklab.utils.ImageProcess;

public class ColonyModel {
	private Bitmap rawImg;
	private Bitmap finalResult;
	
	private int	   width;
	private int    height;
	private int    image_startY;
	private int    image_startX;
	private int    image_centerX;
	private int    image_centerY;
	
	private int    radius = 220;
	private int    gridInterval = 55;
	private int    connectedThreshold  	= 20;
	private int    areaThreshold 		= 20;
	private double shapeFactorThreshold = 0.51;
	
	
	public ColonyModel(Bitmap rawImg){
		this.rawImg = rawImg;
		this.width = rawImg.getWidth();
		this.height = rawImg.getHeight();
		
		this.image_startY = (this.height - 2*radius) / 2;
		this.image_startX = 240 - radius;
		
		this.image_centerX = this.width / 2;
		this.image_centerY = this.height / 2;
	}
	
	/**
	 * 
	 */
	public Bitmap count() {
		Mat raw = new Mat();
		Mat raw_gray = new Mat();
//		Mat raw_gray_median = new Mat();
		Mat raw_gray_copy = new Mat();
		Mat raw_gray_final = new Mat();
		Mat raw_gray_RGBA = new Mat();
		
//		Mat raw_median = new Mat();
		Utils.bitmapToMat(rawImg, raw);
		// 轉灰階
		Imgproc.cvtColor(raw, raw_gray, Imgproc.COLOR_BGR2GRAY);
//		// 用中位數filter做低通
//		Imgproc.medianBlur(raw_gray, raw_gray_median, 5);
		
		// to use second otsu segmentation
		raw_gray.copyTo(raw_gray_copy);
		
		// 用中位數filter做低通
//		Imgproc.medianBlur(raw, raw_median, 5);
		
		// Otsu training
//		int[] hist = getHistogramAndTotalPixels(raw_gray_median, trainingRadius);
//		int threshold = ImageProcess.otsu(hist, hist[256]);
//		setColony(raw_gray_median, trainingRadius, threshold);
		
		// kmeans clustering
//		CustomKmeans2 kmeans = new CustomKmeans2(3);
//		ArrayList<Cluster> clusterList = kmeans.kmeans(rawImg);
//		ArrayList<Cluster> clusterList = ImageProcess.kmeans(raw_median, trainingRadius, 3);
//		setColony(raw_median, clusterList);
		
		
		// init search grid
		ArrayList<String> gridIndex = initSpiralSearchGrid();
		// init gridThresholds that records thresholds of each grid
		int[][] gridThresholds = new int[radius*2/gridInterval][radius*2/gridInterval];
		for(int i = 0; i < gridThresholds.length; i++){
			for(int j = 0; j < gridThresholds[i].length; j++){
				gridThresholds[i][j] = 0;
			}
		}
		
		
		Imgproc.cvtColor(raw_gray, raw_gray_RGBA, Imgproc.COLOR_GRAY2RGBA);
		Imgproc.cvtColor(raw_gray_copy, raw_gray_final, Imgproc.COLOR_GRAY2RGBA);
		
		
		// use sobel filter to get image gradient
//		Mat raw_gray_sobel = new Mat();
//		Imgproc.Sobel(raw_gray, raw_gray_sobel, -1, 0, 1);
		
		// find colony model
		// TODO: use opencv to find connected component
		while(!gridIndex.isEmpty()){
			String unparsedSearchGrid = getNextSearchGrid(gridIndex);
			int[] searchGrid = parseSearchGrid(unparsedSearchGrid);
			int x_searchGrid = searchGrid[0];
			int y_searchGrid = searchGrid[1];
			
			int[] hist = getHistogramAndTotalPixels(x_searchGrid, y_searchGrid, raw_gray_RGBA);
			int threshold = ImageProcess.otsu(hist, hist[256]);
			gridGraySacle(x_searchGrid, y_searchGrid, threshold, raw_gray_RGBA);
			
			
			Log.d("test", y_searchGrid + "," + x_searchGrid + ": " + threshold);
			
			
			ArrayList<Component> components = findConnectedComponent(x_searchGrid, y_searchGrid, raw_gray_RGBA);
			if(findColony(components, raw_gray_RGBA)){
				gridThresholds[y_searchGrid][x_searchGrid] = threshold;
			}
		}
		Log.d("test", "-------------------------------------");
		
		// record non-zero thresholds
		ArrayList<String> nonZeroGridThreshold = new ArrayList<String>();
		for(int i = 0; i < gridThresholds.length; i++){
			for(int j = 0; j < gridThresholds[i].length; j++){
				
				if(gridThresholds[i][j] != 0){
					Log.d("test", i + "," + j + ": " + gridThresholds[i][j]);
					nonZeroGridThreshold.add(i + "," + j);
				}
				
			}
		}
		Log.d("test", "-------------------------------------");
		
		
		// find other thresholds 
//		fillThresholds(gridThresholds);
		// for 圖1
//		gridThresholds = new int[][]{
//			{185, 185, 185, 189, 193, 198, 198, 198},
//			{184, 185, 185, 185, 198, 198, 198, 198},
//			{177, 183, 185, 184, 192, 198, 198, 198},
//			{165, 177, 182, 184, 187, 192, 198, 198},
//			{165, 165, 160, 167, 160, 187, 192, 198},
//			{165, 165, 149, 133, 133, 153, 187, 192},
//			{165, 153, 133, 133, 133, 133, 151, 187},
//			{150, 133, 133, 133, 133, 133, 133, 157}
//		};
		
		// 2,2: 185
		// 2,5: 198
		// 3,2: 182
		// 3,4: 187
		// 4,1: 165
		// 5,3: 133
//		gridThresholds = new int[][]{
//			{177,  185,  191,  199,  205,  216,  222,  222},
//			{177,  183,  188,  194,  199,  207,  212,  214},
//			{177,  181,  185,  189,  193,  198,  202,  206},
//			{177,  179,  182,  184,  187,  189,  192,  194},
//			{162,  165,  168,  171,  174,  180,  183,  186},
//			{147,  151,  154,  133,  161,  171,  174,  178},
//			{132,  137,  140,  145,  148,  162,  165,  170},
//			{117,  123,  126,  132,  135,  153,  156,  162}
//		};
		
		
		// for 圖2
//		gridThresholds = new int[][]{
//			{174, 181, 197, 195, 194, 191, 191, 189},
//			{174, 174, 182, 197, 193, 193, 189, 189},
//			{167, 174, 174, 185, 197, 189, 189, 189},
//			{163, 165, 174, 176, 193, 193, 189, 176},
//			{163, 163, 158, 166, 150, 150, 150, 163},
//			{142, 133, 128, 114, 120, 150, 150, 150},
//			{122, 122, 91,  106, 104, 127, 150, 150},
//			{122, 107, 109, 102, 105, 104, 127, 150}
//		};
		
		// 2,4: 197
		// 2,5: 189
		// 3,2: 174
		// 4,1: 163
		// 4,2: 158
		// 5,5: 150
		// 6,1: 122
		// 6,2: 91
		// 6,3: 106
		// 6,4: 104
		// 7,2: 109
//		gridThresholds = new int[][]{
//			{200, 211, 222, 221, 215, 221, 225, 213},
//			{192, 199, 206, 207, 206, 205, 203, 193},
//			{184, 187, 190, 193, 197, 189, 181, 173},
//			{176, 175, 174, 179, 172, 166, 159, 153},
//			{168, 163, 158, 153, 148, 143, 138, 133},
//			{160, 142, 124, 129, 126, 150, 119, 115},
//			{153, 122, 91,  106, 104, 102, 100, 98},
//			{146, 102, 109, 83,  82,  54,  81,  90}
//		};
		
		
		
		// for 圖4
//		gridThresholds = new int[][]{
//			{209, 209, 209, 209, 172, 135, 135, 135},
//			{209, 209, 209, 209, 209, 135, 135, 135},
//			{180, 209, 209, 209, 198, 166, 166, 155},
//			{139, 165, 209, 209, 198, 188, 176, 176},
//			{142, 136, 131, 113, 142, 153, 182, 176},
//			{142, 142, 131, 108, 96,  102, 104, 143},
//			{142, 123, 98,  109, 108, 104, 110, 108},
//			{121, 98,  107, 116, 111, 117, 110, 110}
//		};
		
		// 1,6: 135
		// 2,3: 209
		// 3,5: 188
		// 3,6: 176
		// 5,1: 142
		// 5,2: 131
		// 5,4: 96
		// 6,2: 98
		// 6,4: 108
		// 6,5: 104
		// 7,3: 116
		// 7,5: 117
		
		gridThresholds = new int[][]{
			{219, 233, 248, 197, 170, 143, 114, 89},
			{206, 215, 225, 203, 181, 159, 135, 113},
			{193, 197, 202, 209, 192, 175, 156, 137},
			{158, 164, 170, 176, 160, 188, 176, 164},
			{123, 130, 138, 145, 128, 160, 150, 140},
			{153, 142, 131, 114, 96,  132, 125, 118},
			{88,  93,  98,  103, 108, 104, 100, 96},
			{115, 115, 116, 116, 116, 117, 117, 118}
		};
		
		
		
		// do second otsu segmentation
		for(int y = 0; y < gridThresholds.length; y++){
			for(int x = 0; x < gridThresholds[y].length; x++){
				Log.d("test", y + ", " + x + ":" + gridThresholds[y][x]);
				gridGraySacle(x, y, gridThresholds[y][x], raw_gray_final);
			}
		}
		
		
		Mat raw_gray_final_gaussian = new Mat();
		Imgproc.medianBlur(raw_gray_final, raw_gray_final_gaussian, 3);
		
		Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(raw_gray_final_gaussian, temp);
		
		finalResult = BitmapUtil.createForegroundImage(temp, radius);
		
		return temp;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<String> initSpiralSearchGrid(){
		ArrayList<String> gridIndex = new ArrayList<String>();
		int h_gridNumber = (radius*2) / gridInterval;
		int v_gridNumber = (radius*2) / gridInterval;
		
		// 四個方向的flag
		boolean down = true;
		boolean right = false;
		boolean up = false;
		boolean left = false;
		
		int h_increment = 2;
		int v_increment = 2;
		
		int start_h = h_gridNumber / 2 - h_increment/2;
		int start_v = v_gridNumber / 2 - v_increment/2;
		
		int search_h = start_h;
		int search_v = start_v;
		
		// spiral search
		while((down || right || up || left) && start_h >= 0) {
			gridIndex.add(search_h + "," + search_v);
			if(down) {
				search_v++;
				if(search_v == start_v + v_increment - 1){
					down = false;
					right = true;
				}
			} else if(right) {
				search_h++;
				if(search_h == start_h + h_increment - 1){
					right = false;
					up = true;
				}
			} else if(up) {
				search_v--;
				if(search_v == start_v){
					up = false;
					left = true;
				}
			} else if(left) {
				search_h--;
				if(search_h == start_h){
					left = false;
					
					h_increment += 2;
					v_increment += 2;
					
					start_h = h_gridNumber / 2 - h_increment/2;
					start_v = v_gridNumber / 2 - v_increment/2;
					
					search_h = start_h;
					search_v = start_v;
					down = true;
				}
			}
		}
		
		return gridIndex;
	}
	
	/**
	 * 
	 * @param gridIndex
	 * @return
	 */
	public String getNextSearchGrid(ArrayList<String> gridIndex) {
		return gridIndex.remove(0);
	}
	
	/**
	 * 
	 * @param unparsedSearchGrid
	 * @return
	 */
	public int[] parseSearchGrid(String unparsedSearchGrid){
		int[] tmp = new int[2];
		String[] unparsedSearchGridData = unparsedSearchGrid.split(",");
		// horizontal
		tmp[0] = Integer.parseInt(unparsedSearchGridData[0]);
		// vertical
		tmp[1] = Integer.parseInt(unparsedSearchGridData[1]);
		
		return tmp;
	}
	
	/**
	 * 
	 * @param x_searchGrid
	 * @param y_searchGrid
	 * @param mat
	 * @return
	 */
	public int[] getHistogramAndTotalPixels(int x_searchGrid, int y_searchGrid, Mat mat){
		int[] temp = new int[257];
		double[] d;
		int grayLevel;
		int total = 0;
		
		for(int y = image_startY+y_searchGrid*gridInterval; y < image_startY+(y_searchGrid+1)*gridInterval; y++){
			double xRange = Math.sqrt(Math.pow(radius, 2) - Math.pow(y - height / 2, 2));
			for(int x = image_startX+x_searchGrid*gridInterval; x < image_startX+(x_searchGrid+1)*gridInterval; x++){
				if(x >= image_centerX - xRange && x <= image_centerX + xRange){
					d = mat.get(y, x);
					grayLevel = (int) Math.round(d[0]);
					temp[grayLevel]++;
					total++;
				} 
			}
		}
		temp[256] = total;
		
		return temp;
	}
	
	/**
	 * 
	 * @param x_searchGrid
	 * @param y_searchGrid
	 * @param threshold
	 */
	public void gridGraySacle(int x_searchGrid, int y_searchGrid, int threshold, Mat mat){
		for(int y = image_startY+y_searchGrid*gridInterval; y < image_startY+(y_searchGrid+1)*gridInterval; y++){
			double xRange = Math.sqrt(Math.pow(radius, 2) - Math.pow(y - height / 2, 2));
			for(int x = image_startX + x_searchGrid*gridInterval; x < image_startX + (x_searchGrid+1)*gridInterval; x++){
				if(x >= image_centerX - xRange && x <= image_centerX + xRange){
					double[] d = mat.get(y, x);
					int intensity = (int) Math.round(d[0]);
					
					if(intensity > threshold)
						// for RGBA
						mat.put(y, x, 255, 255, 255, 255);
						// for gray
						// mat.put(y, x, 255);
					else
						// for RGBA
						mat.put(y, x, 0, 0, 0, 255);
						// for gray
						// mat.put(y, x, 0);
				}
			}
		}
	}
	
	public ArrayList<Component> findConnectedComponent(int x_searchGrid, int y_searchGrid, Mat mat){
		int x_start = image_startX + x_searchGrid * gridInterval;
		int x_end = image_startX + (x_searchGrid+1) * gridInterval;
		int y_start = image_startY + y_searchGrid * gridInterval;
		int y_end = image_startY + (y_searchGrid+1) * gridInterval;
		
		int[][] label = new int[gridInterval][gridInterval];
		int objectId = 0;	
		for(int i = 0; i < label.length; i++){
			for(int j = 0; j < label[i].length; j++){
				label[i][j] = 0;
			}
		}
		
		ArrayList<Pixel> 	 L 			= new ArrayList<Pixel>();
		ArrayList<Component> components = new ArrayList<Component>();
		
		for(int y = y_start; y < y_end; y++){
			for(int x = x_start; x < x_end; x++){
				
				// if not labeled
				if(label[y-y_start][x-x_start] == 0){
					// get this pixel
					double[] color = mat.get(y, x);
					Pixel pixel = new Pixel(x, y, color[0]);
					
					// new component
					Component component = new Component();
					component.addPixel(pixel);
					
					// label self
					objectId++;
					labelPixel(x, y, x_start, y_start, label, objectId);
					
					
					int periNumber = 0;
					int xMin = 999999;
					int xMax = 0;
					int yMin = 999999;
					int yMax = 0;
					
					if(x < xMin)
						xMin = x;
					if(x > xMax)
						xMax = x;
					if(y < yMin)
						yMin = y;
					if(y > yMax)
						yMax = y;
					
					
					// add4AdjacentNeighbors
					Pixel pixel2 = null;
					// north
					// 在範圍內
					if(y-1 >= y_start){
						pixel2 = getPixel(x, y-1, mat);
						// 是否相連
						if(connect(pixel, pixel2)){
							// 未標記過
							if(label[y-y_start-1][x-x_start] == 0){
								labelPixel(x, y-1, x_start, y_start, label, objectId);
								L.add(pixel2);
								component.addPixel(pixel2);
							}
						} else {
							periNumber++;
						}
					} else {
						periNumber++;
					}
					
					
					// east
					// 在範圍內
					if(x+1 < x_end){
						pixel2 = getPixel(x+1, y, mat);
						// 是否相連
						if(connect(pixel, pixel2)){
							// 未標記過
							if(label[y-y_start][x-x_start+1] == 0){
								labelPixel(x+1, y, x_start, y_start, label, objectId);
								L.add(pixel2);
								component.addPixel(pixel2);
							}
						} else {
							periNumber++;
						}
					} else {
						periNumber++;
					}
					
					
					// south
					// 在範圍內
					if(y+1 < y_end){
						pixel2 = getPixel(x, y+1, mat);
						// 是否相連
						if(connect(pixel, pixel2)){
							// 未標記過
							if(label[y-y_start+1][x-x_start] == 0){
								labelPixel(x, y+1, x_start, y_start, label, objectId);
								L.add(pixel2);
								component.addPixel(pixel2);
							}
						} else {
							periNumber++;
						}
					} else {
						periNumber++;
					}
					
					
					// west
					// 在範圍內
					if(x-1 >= x_start){
						pixel2 = getPixel(x-1, y, mat);
						// 是否相連
						if(connect(pixel, pixel2)){
							// 未標記過
							if(label[y-y_start][x-x_start-1] == 0){
								labelPixel(x-1, y, x_start, y_start, label, objectId);
								L.add(pixel2);
								component.addPixel(pixel2);
							}
						} else {
							periNumber++;
						}
					} else {
						periNumber++;
					}
					
					
					component.addPerimeter(periNumber);
					
					
					// get neighbor pixels from L
					while(!L.isEmpty()){
						Pixel neighbor = L.remove(L.size()-1);
						int x_neighbor = neighbor.getX();
						int y_neighbor = neighbor.getY();
						periNumber = 0;
						
						if(x_neighbor < xMin)
							xMin = x_neighbor;
						if(x_neighbor > xMax)
							xMax = x_neighbor;
						if(y_neighbor < yMin)
							yMin = y_neighbor;
						if(y_neighbor > yMax)
							yMax = y_neighbor;
						
						
						// north
						// 在範圍內
						if(y_neighbor-1 >= y_start){
							pixel2 = getPixel(x_neighbor, y_neighbor-1, mat);
							// 是否相連
							if(connect(neighbor, pixel2)){
								// 未標記過
								if(label[y_neighbor-y_start-1][x_neighbor-x_start] == 0){
									labelPixel(x_neighbor, y_neighbor-1, x_start, y_start, label, objectId);
									L.add(pixel2);
									component.addPixel(pixel2);
								}
							} else {
								periNumber++;
							}
						} else {
							periNumber++;
						}
						
						
						// east
						// 在範圍內
						if(x_neighbor+1 < x_end){
							pixel2 = getPixel(x_neighbor+1, y_neighbor, mat);
							// 是否相連
							if(connect(neighbor, pixel2)){
								// 未標記過
								if(label[y_neighbor-y_start][x_neighbor-x_start+1] == 0){
									labelPixel(x_neighbor+1, y_neighbor, x_start, y_start, label, objectId);
									L.add(pixel2);
									component.addPixel(pixel2);
								}
							} else {
								periNumber++;
							}
						} else {
							periNumber++;
						}
						
						
						// south
						// 在範圍內
						if(y_neighbor+1 < y_end){
							pixel2 = getPixel(x_neighbor, y_neighbor+1, mat);
							// 是否相連
							if(connect(neighbor, pixel2)){
								// 未標記過
								if(label[y_neighbor-y_start+1][x_neighbor-x_start] == 0){
									labelPixel(x_neighbor, y_neighbor+1, x_start, y_start, label, objectId);
									L.add(pixel2);
									component.addPixel(pixel2);
								}
							} else {
								periNumber++;
							}
						} else {
							periNumber++;
						}
						
						
						// west
						// 在範圍內
						if(x_neighbor-1 >= x_start){
							pixel2 = getPixel(x_neighbor-1, y_neighbor, mat);
							// 是否相連
							if(connect(neighbor, pixel2)){
								// 未標記過
								if(label[y_neighbor-y_start][x_neighbor-x_start-1] == 0){
									labelPixel(x_neighbor-1, y_neighbor, x_start, y_start, label, objectId);
									L.add(pixel2);
									component.addPixel(pixel2);
								}
							} else {
								periNumber++;
							}
						} else {
							periNumber++;
						}
						component.addPerimeter(periNumber);
					}
					
					component.setCenterX((xMin+xMax)/2);
					component.setCenterY((yMin+yMax)/2);
					component.setProperties();
					
					if(!(xMin == x_start+1 || xMax == x_end-1 || yMin == y_start+1 || yMax == y_end-1)){
						components.add(component);
					}
				}
			}
		}
		
		return components;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param x_start
	 * @param y_start
	 * @param label
	 * @param objectId
	 */
	public void labelPixel(int x, int y, int x_start, int y_start, int[][] label, int objectId){
		label[y-y_start][x-x_start] = objectId;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param img
	 * @return
	 */
	public Pixel getPixel(int x, int y, Mat mat){
		double[] color = mat.get(y, x);
		
		return new Pixel(x, y, color[0]);
	}
	
	/**
	 * 
	 * @param pixel
	 * @param pixel2
	 * @return
	 */
	public boolean connect(Pixel pixel, Pixel pixel2){
		if(Math.abs(pixel.getIntensity()-pixel2.getIntensity()) <= connectedThreshold)
			return true;
		else 
			return false;
	}
	
	/**
	 * 
	 * @param components
	 * @return true if find colony and mark it, else return false
	 */
	public boolean findColony(ArrayList<Component> components, Mat mat){
		int redCount = 0;
		int greenCount = 0;
		boolean findColony = false;
		
		for(int i = 0; i < components.size(); i++){
			Component component = components.get(i);
			if(component.getArea() >= areaThreshold && component.getShapeFactor() >= shapeFactorThreshold) {
				Log.d("test", i + ": area = " + component.getArea() + ", shapeFactor = " + component.getShapeFactor() + ", perimeter = " + component.getPerimeter());
				redCount++;
//				Core.circle(mat, new Point(component.getCenterX(), component.getCenterY()), (int)component.getRadius(), new Scalar(255, 0, 0, 255), 2);
				findColony = true;
			}
		}
		
		
		return findColony;
	}
}
