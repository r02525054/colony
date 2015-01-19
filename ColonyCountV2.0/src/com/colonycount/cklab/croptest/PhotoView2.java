/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.colonycount.cklab.croptest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.widget.ImageView;

import com.colonycount.cklab.activity.Test2.State;
import com.colonycount.cklab.crop.HighlightView;
import com.colonycount.cklab.croptest.PhotoViewAttacher2.OnMatrixChangedListener;
import com.colonycount.cklab.croptest.PhotoViewAttacher2.OnPhotoTapListener;
import com.colonycount.cklab.croptest.PhotoViewAttacher2.OnViewTapListener;

public class PhotoView2 extends ImageView implements IPhotoView2, OnDragCallback, OnScaleCallback {
    private final PhotoViewAttacher2 mAttacher;
    
    // my code 
    private int screenWidth;
    private int screenHeight;
    
    private ScaleType mPendingScaleType;
    
    private List<HighlightView> colonyList = new ArrayList<HighlightView>();
    private List<HighlightView> colonyAddTempList = new ArrayList<HighlightView>();
    private List<HighlightView> colonyRemoveTempList = new ArrayList<HighlightView>();
    
    private Paint mNoFocusPaint = new Paint();
    private Paint mOutlinePaint = new Paint();
    private Rect viewDrawingRect = new Rect();
    
    private State state = State.VIEW;
    
    public PhotoView2(Context context) {
        this(context, null);
    }

    public PhotoView2(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView2(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setScaleType(ScaleType.MATRIX);
        mAttacher = new PhotoViewAttacher2(this);
        
        // drawing setting
        mOutlinePaint.setStrokeWidth(2F);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);
        mOutlinePaint.setColor(0xFFEF04D6);
        mNoFocusPaint.setARGB(125, 50, 50, 50);
        
        if (null != mPendingScaleType) {
            setScaleType(mPendingScaleType);
            mPendingScaleType = null;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(width < height)
        	setMeasuredDimension(width, width); //Snap to width
        else
        	setMeasuredDimension(height, height); //Snap to height
    }

    /**
     * @deprecated use {@link #setRotationTo(float)}
     */
    @Override
    public void setPhotoViewRotation(float rotationDegree) {
        mAttacher.setRotationTo(rotationDegree);
    }
    
    @Override
    public void setRotationTo(float rotationDegree) {
        mAttacher.setRotationTo(rotationDegree);
    }

    @Override
    public void setRotationBy(float rotationDegree) {
        mAttacher.setRotationBy(rotationDegree);
    }

    @Override
    public boolean canZoom() {
        return mAttacher.canZoom();
    }

    @Override
    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Override
    public Matrix getDisplayMatrix() {
        return mAttacher.getDrawMatrix();
    }

    @Override
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return mAttacher.setDisplayMatrix(finalRectangle);
    }

    @Override
    @Deprecated
    public float getMinScale() {
        return getMinimumScale();
    }

    @Override
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override
    @Deprecated
    public float getMidScale() {
        return getMediumScale();
    }

    @Override
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override
    @Deprecated
    public float getMaxScale() {
        return getMaximumScale();
    }

    @Override
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public ScaleType getScaleType() {
        return mAttacher.getScaleType();
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    @Deprecated
    public void setMinScale(float minScale) {
        setMinimumScale(minScale);
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override
    @Deprecated
    public void setMidScale(float midScale) {
        setMediumScale(midScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override
    @Deprecated
    public void setMaxScale(float maxScale) {
        setMaximumScale(maxScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (null != mAttacher) {
            mAttacher.update();
        }
    }

    @Override
    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mAttacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mAttacher.getOnPhotoTapListener();
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mAttacher.getOnViewTapListener();
    }

    @Override
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (null != mAttacher) {
            mAttacher.setScaleType(scaleType);
        } else {
            mPendingScaleType = scaleType;
        }
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mAttacher.setZoomable(zoomable);
    }

    @Override
    public Bitmap getVisibleRectangleBitmap() {
        return mAttacher.getVisibleRectangleBitmap();
    }

    @Override
    public void setZoomTransitionDuration(int milliseconds) {
        mAttacher.setZoomTransitionDuration(milliseconds);
    }

    @Override
    public IPhotoView2 getIPhotoViewImplementation() {
        return mAttacher;
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        mAttacher.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.cleanup();
        super.onDetachedFromWindow();
    }

	public void setScreenWidth(int screenWidth){
		this.screenWidth = screenWidth;
		mAttacher.setScreenWidth(screenWidth);
	}
	
	public void setScreenHeight(int screenHeight){
		this.screenHeight = screenHeight;
		mAttacher.setScreenHeight(screenHeight);
	}
	
//	public void setMinimumScale(){
//		mAttacher.setMinimumScale();
//	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Path path = new Path();
		canvas.save();
		
		for(int i = 0; i < colonyList.size(); i++){
			HighlightView hv = colonyList.get(i);
            path.addCircle(hv.getX(), hv.getY(), hv.getR(), Path.Direction.CW);
            canvas.clipPath(path, Region.Op.DIFFERENCE);
		}
		
		for(int i = 0; i < colonyAddTempList.size(); i++){
			HighlightView hv = colonyAddTempList.get(i);
            path.addCircle(hv.getX(), hv.getY(), hv.getR(), Path.Direction.CW);
            canvas.clipPath(path, Region.Op.DIFFERENCE);
		}
		
		if(state == State.ADD || state == State.SUB){
			this.getDrawingRect(viewDrawingRect);
			canvas.drawRect(viewDrawingRect, mNoFocusPaint);
		}
		
        canvas.restore();
        canvas.drawPath(path, mOutlinePaint);
	}
	
	public void addColonyView(){
		colonyList.addAll(colonyAddTempList);
		colonyAddTempList.clear();
		invalidate();
	}
	
	public void removeColonyView(){
		colonyRemoveTempList.clear();
		invalidate();
	}
	
	public void addColonyTempView(HighlightView hv){
		colonyAddTempList.add(hv);
		invalidate();
	}
	
	public void addRemoveColonyTempView(HighlightView hv){
		colonyRemoveTempList.add(hv);
		colonyList.remove(hv);
		invalidate();
	}
	
	public void cancelAddColonyView(){
		colonyAddTempList.clear();
		invalidate();
	}
	
	public void cancelRemoveColonyView(){
		colonyList.addAll(colonyRemoveTempList);
		colonyRemoveTempList.clear();
		invalidate();
	}

	@Override
	public void onDragFinish(float dx, float dy) {
		// draw colony view
		for(int i = 0; i < colonyList.size(); i++){
			HighlightView hv = colonyList.get(i);
            hv.mMatrix.postTranslate(dx, dy);
            hv.invalidate();
		}
		
		// draw colony temp view
		for(int i = 0; i < colonyAddTempList.size(); i++){
			HighlightView hv = colonyAddTempList.get(i);
            hv.mMatrix.postTranslate(dx, dy);
            hv.invalidate();
		}
		
		invalidate();
	}

	@Override
	public void onScaleFinish(float dx, float dy) {
		// draw colony view
		for(int i = 0; i < colonyList.size(); i++){
			HighlightView hv = colonyList.get(i);	
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
		
		// draw colony temp view
		for(int i = 0; i < colonyAddTempList.size(); i++){
			HighlightView hv = colonyAddTempList.get(i);
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
		
		invalidate();
	}
	
	public void setState(State state){
		this.state = state;
	}
	
	public List<HighlightView> getColonyList(){
		return colonyList;
	}
	
	public List<HighlightView> getColonyAddTempList(){
		return colonyAddTempList;
	}
}