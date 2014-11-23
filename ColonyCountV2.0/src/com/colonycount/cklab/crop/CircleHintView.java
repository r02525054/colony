package com.colonycount.cklab.crop;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CircleHintView {
	private float cx;
	private float cy;
	private float radius;
	
	public CircleHintView(float cx, float cy, float radius) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
	}

	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint paint = new Paint();
		paint.setColor(0xFFEF04D6);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		canvas.drawCircle(cx, cy, radius, paint);
	}
}
