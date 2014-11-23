package com.colonycount.cklab.crop;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.colonycount.cklab.activity.CropPhotoActivity;
import com.colonycount.cklab.activity.ResultActivity;
import com.colonycount.cklab.activity.ResultActivity.State;
import com.colonycount.cklab.model.CancelView;

public class CropImageView2 extends ImageViewTouchBase {
    public ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
    public HighlightView mMotionHighlightView = null;
    public float mLastX, mLastY;
    public int mMotionEdge;
   
    // my code to fix edge display
    int topPadding;
    
    public ArrayList<HighlightView2> myViews = new ArrayList<HighlightView2>();
    public float mLastXSub, mLastYSub;
    public HighlightView3 showView;
    public ArrayList<HighlightView4> rViews = new ArrayList<HighlightView4>();
    public ArrayList<HighlightView5> gViews = new ArrayList<HighlightView5>();
    public ArrayList<CancelView> cViews = new ArrayList<CancelView>();
    
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (mBitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
            
            for (HighlightView2 hv : myViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
            
            if(showView != null){
            	showView.mMatrix.set(getImageMatrix());
            	showView.invalidate();
                if (showView.mIsFocused) {
                    centerBasedOnHighlightView(showView);
                }
            }
        }
    }

    public CropImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
    	
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        for (HighlightView2 hv : myViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        if(showView != null){
        	showView.mMatrix.set(getImageMatrix());
        	showView.invalidate();
        }
        
        for (HighlightView4 hv : rViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        for (HighlightView5 hv : gViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }
    
    public void zoomTo(float scale, float centerX, float centerY, float durationMs){
    	super.zoomTo(scale, centerX, centerY, durationMs);
    }
    
    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        for (HighlightView2 hv : myViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        if(showView != null){
        	showView.mMatrix.set(getImageMatrix());
        	showView.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        for (HighlightView2 hv : myViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
        
        if(showView != null){
        	showView.mMatrix.set(getImageMatrix());
        	showView.invalidate();
        }
    }
    
    public void setHighlightView3Scale(){
    	if(showView != null){
        	showView.mMatrix.set(getImageMatrix());
        	showView.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
        
        for(int i = 0; i < myViews.size(); i++){
        	HighlightView2 hv = myViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
        
        if(showView != null){
        	showView.mMatrix.postTranslate(deltaX, deltaY);
        	showView.invalidate();
        }
        
        for(int i = 0; i < rViews.size(); i++){
        	HighlightView4 hv = rViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
        
        for(int i = 0; i < gViews.size(); i++){
        	HighlightView5 hv = gViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private void recomputeFocus(MotionEvent event) {
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.setFocus(false);
            hv.invalidate();
        }

        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            int edge = hv.getHit(event.getX(), event.getY());
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true);
                    hv.invalidate();
                }
                break;
            }
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ResultActivity cropImage = (ResultActivity) this.getContext();
        
        if (cropImage.mSaving) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event);
                } else {
                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        int edge = hv.getHit(event.getX(), event.getY());
                        
                        if (edge != HighlightView.GROW_NONE) {
                            mMotionEdge = edge;
                            mMotionHighlightView = hv;
                            mLastX = event.getX();
                            mLastY = event.getY();
                            mMotionHighlightView.setMode(
                                    (edge == HighlightView.MOVE)
                                    ? HighlightView.ModifyMode.Move
                                    : HighlightView.ModifyMode.Grow);
                            break;
                        }
                    }
                    
                    // my code
                    mLastXSub = event.getX();
                    mLastYSub = event.getY();
                    // hit HighlightView2
                    if(cropImage.getState() == State.SUB){
                    	boolean subed = false;
                    	// 找有沒有點到新加進去的view
                    	for(int i = myViews.size() - 1; i >= 0; i--){
                    		// just choose one to sub
                    		if(subed)
                    			break;
                    			
                        	HighlightView2 hv2 = myViews.get(i);
                        	int edge2 = hv2.getHit(event.getX(), event.getY());
                        	if(edge2 > HighlightView2.GROW_NONE){
                        		float centerX = hv2.getCenterX();
                        		float centerY = hv2.getCenterY();
                        		float radius = hv2.getRadius();
                        		
                        		myViews.remove(hv2);
                        		add(centerX, centerY, radius, CancelView.Type.PURPLE);
                        		subed = true;
                        		invalidate();
                            	cropImage.drawHighlightImage();
                        	}
                        }
                    	
                    	// 找有沒有點到被算到是colony的view
                    	for(int i = rViews.size() - 1; i>= 0; i--){
                    		if(subed)
                    			break;
                    		
                    		HighlightView4 hv4 = rViews.get(i);
                    		int edge4 = hv4.getHit(event.getX(), event.getY());
                    		if(edge4 > HighlightView4.GROW_NONE){
                    			float centerX = hv4.getCenterX();
                        		float centerY = hv4.getCenterY();
                        		float radius = hv4.getRadius();
                        		
                        		rViews.remove(hv4);
                        		add(centerX, centerY, radius, CancelView.Type.RED);
                        		subed = true;
                        		invalidate();
                            	cropImage.drawHighlightImage();
                        	}
                    	}
                    	
                    	// 找有沒有點到被算到不是colony的view
                    	for(int i = gViews.size() - 1; i >= 0; i--){
                    		if(subed)
                    			break;
                    		
                    		HighlightView5 hv5 = gViews.get(i);
                    		int edge5 = hv5.getHit(event.getX(), event.getY());
                    		if(edge5 > HighlightView5.GROW_NONE){
                    			float centerX = hv5.getCenterX();
                        		float centerY = hv5.getCenterY();
                        		float radius = hv5.getRadius();
                        		
                    			gViews.remove(hv5);
                    			add(centerX, centerY, radius, CancelView.Type.GREEN);
                        		subed = true;
                        		invalidate();
                            	cropImage.drawHighlightImage();
                        	}
                    	}
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (cropImage.mWaitingToPick) {
                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        if (hv.hasFocus()) {
                            cropImage.mCrop = hv;
                            for (int j = 0; j < mHighlightViews.size(); j++) {
                                if (j == i) {
                                    continue;
                                }
                                mHighlightViews.get(j).setHidden(true);
                            }
                            centerBasedOnHighlightView(hv);
                            ((CropPhotoActivity) this.getContext()).mWaitingToPick = false;
                            return true;
                        }
                    }
                } else if (mMotionHighlightView != null) {
                    centerBasedOnHighlightView(mMotionHighlightView);
                    mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
                    
                }
                mMotionHighlightView = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event);
                } else if (mMotionHighlightView != null) {
                    mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
                    mLastX = event.getX();
                    mLastY = event.getY();
                    
                    if (true) {
                        // This section of code is optional. It has some user
                        // benefit in that moving the crop rectangle against
                        // the edge of the screen causes scrolling but it means
                        // that the crop rectangle is no longer fixed under
                        // the user's finger.
                        ensureVisible(mMotionHighlightView);
                    }
                    
                // my code
                } else if(cropImage.getState() == State.SUB) {
                	// add another highlight view to move
                	showView.handleMotion(HighlightView3.MOVE, event.getX() - mLastXSub, event.getY() - mLastYSub);
                	if (true) {
                        // This section of code is optional. It has some user
                        // benefit in that moving the crop rectangle against
                        // the edge of the screen causes scrolling but it means
                        // that the crop rectangle is no longer fixed under
                        // the user's finger.
                        ensureVisible(showView);
                    }
                }
                
                break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                center(true, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around.  This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    center(true, true);
                }
                break;
        }

        return true;
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, this.getLeft() - r.left);
        int panDeltaX2 = Math.min(0, this.getRight() - r.right);

//        int panDeltaY1 = Math.max(0, this.getTop() - r.top);
        int panDeltaY1 = Math.max(0, this.getTop() - r.top - topPadding);
//        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom);
        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom - topPadding);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }
    
 // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView2 hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, this.getLeft() - r.left);
        int panDeltaX2 = Math.min(0, this.getRight() - r.right);

//        int panDeltaY1 = Math.max(0, this.getTop() - r.top);
        int panDeltaY1 = Math.max(0, this.getTop() - r.top - topPadding);
//        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom);
        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom - topPadding);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }
    
    private void ensureVisible(HighlightView3 hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, this.getLeft() - r.left);
        int panDeltaX2 = Math.min(0, this.getRight() - r.right);

//        int panDeltaY1 = Math.max(0, this.getTop() - r.top);
        int panDeltaY1 = Math.max(0, this.getTop() - r.top - topPadding);
//        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom);
        int panDeltaY2 = Math.min(0, this.getBottom() - r.bottom - topPadding);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }
    
    

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();
        
        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        
//        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
        // my code
        // set max zoom = 6
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
        	if(zoom > 6)
        		zoom = 6;
        		
            float [] coordinates = new float[] {hv.mCropRect.centerX(), hv.mCropRect.centerY()};
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }
    
    
    private void centerBasedOnHighlightView(HighlightView2 hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();
        
        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float [] coordinates = new float[] {hv.mCropRect.centerX(), hv.mCropRect.centerY()};
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }
    
    
    private void centerBasedOnHighlightView(HighlightView3 hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();
        
        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;
        
        
        Log.d("test4", "z1, z2 = " + z1 + ", " + z2);
        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float [] coordinates = new float[] {hv.mCropRect.centerX(), hv.mCropRect.centerY()};
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }
    

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            mHighlightViews.get(i).draw(canvas);
        }
        
        for(int i = 0; i < myViews.size(); i++){
        	myViews.get(i).draw(canvas);
        }
        
        if(showView != null)
        	showView.draw(canvas);
        
        for(int i = 0; i < rViews.size(); i++){
        	rViews.get(i).draw(canvas);
        }
        
        for(int i = 0; i < gViews.size(); i++){
        	gViews.get(i).draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        mHighlightViews.add(hv);
        invalidate();
    }
    
    public void add(HighlightView2 hv2){
    	myViews.add(hv2);
        invalidate();
    }
    
    public void add(HighlightView3 hv3){
    	showView = hv3;
    	invalidate();
    }
    
    public void add(HighlightView4 hv4){
    	rViews.add(hv4);
    	invalidate();
    }
    
    public void add(HighlightView5 hv5){
    	gViews.add(hv5);
    	invalidate();
    }
    
    public void add(float x, float y, float r, CancelView.Type type){
    	cViews.add(new CancelView(x, y, r, type));
    }
    
    public void removeHighlightView3(){
    	showView = null;
    }
    
	public void setTopPadding(int topPadding) {
		this.topPadding = topPadding;
	}
	
	public float getScale(){
		return super.getScale();
	}
	
	public float getMaxZoom(){
		return super.getMaxZoom();
	}
}    