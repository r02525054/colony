package com.colonycount.cklab.config;

public class Config {
	public static final int CROP_REQUEST_WIDTH = 1024;
	public static final int CROP_REQUEST_HEIGHT = 1024;
	
	public static final int OUTPUT_IMAGE_WIDTH = 1024;
	public static final int OUTPUT_IMAGE_HEIGHT = 1024;
	
	public static final int COUNT_IMAGE_WIDTH = 512;
	public static final int COUNT_IMAGE_HEIGHT = 512;
	public static final int TRAIN_RADIUS = COUNT_IMAGE_WIDTH / 2 - 60;
	public static final int TEST_RADIUS = COUNT_IMAGE_WIDTH / 2 - 30;
	
	public static final int T_AREA = 4;
	public static final double T_SHAPE_FACTOR = 0.6;
}
