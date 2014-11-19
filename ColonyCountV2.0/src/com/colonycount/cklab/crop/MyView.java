package com.colonycount.cklab.crop;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class MyView {
	private float centerX;
	private float centerY;
	private float radius;
	
	public MyView(float centerX, float centerY, float radius){
		this.centerX = centerX;
		this.centerY = centerY;
		this.radius = radius;
	}
	
	public void draw(Canvas canvas){
		Paint paint = new Paint();
		paint.setColor(0xFFEF04D6);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		
		Path outline = new Path();
		outline.addCircle(centerX, centerY, radius, Path.Direction.CW); 
		canvas.drawPath(outline, paint);
	}
}
