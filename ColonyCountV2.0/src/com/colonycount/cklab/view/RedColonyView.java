package com.colonycount.cklab.view;

import com.colonycount.cklab.config.Config;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.Log;

public class RedColonyView {
	private boolean isFocus;
	private boolean isHidden;
	private Paint outlinePaint = new Paint();
	private Paint noFocusPaint = new Paint();
	private int x;
	private int y;
	private int r;
	
	public RedColonyView(int x, int y, int r){
		outlinePaint.setColor(Color.RED);
		outlinePaint.setAntiAlias(true);
		outlinePaint.setStyle(Style.STROKE);
		outlinePaint.setStrokeWidth(2F);
		noFocusPaint.setARGB(125, 50, 50, 50);
		this.x = x;
		this.y = y;
		this.r = r;
	}
	
	public void draw(Canvas canvas){
		if(isHidden){
			return;
		}
		
		if(isFocus){
			canvas.save();
			Path path = new Path();
			path.addCircle(x, y, r, Path.Direction.CW);
//			canvas.clipPath(path, Region.Op.DIFFERENCE);
//			canvas.drawRect(viewDrawingRect, hasFocus() ? mFocusPaint : mNoFocusPaint);
//			canvas.restore();
//            canvas.drawPath(path, mOutlinePaint);
		} else {
			float scale = Config.OUTPUT_IMAGE_WIDTH / (float)canvas.getWidth();
//			Log.d("test4", "scale = " + scale);
			
//			Log.d("test4", "x = " + x + ", y = " + y + ", r = " + r);
			canvas.drawCircle(x/scale, y/scale, r/scale, outlinePaint);
			
//			canvas.drawCircle(500, 480, 50, outlinePaint);
		}
	}
}
