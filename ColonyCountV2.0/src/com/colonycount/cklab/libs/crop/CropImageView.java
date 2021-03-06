package com.colonycount.cklab.crop;

import java.util.ArrayList;

import com.colonycount.cklab.activity.CropPhotoActivity;
import com.colonycount.cklab.croptest.HighlightView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class CropImageView extends ImageViewTouchBase {
    public ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
    HighlightView mMotionHighlightView = null;
    float mLastX, mLastY;
    float mMulLastX, mMulLastY;
    int mMotionEdge;
    
    // my code to fix edge display
    int topPadding;
    
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
        }
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    public void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }
    
    public Matrix getImageViewMatrix(){
    	return super.getImageViewMatrix();
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private void recomputeFocus(MotionEvent event) {
    	Log.d("test3", "recomputeFocus");
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
//        CropPhotoActivity cropImage = (CropPhotoActivity) this.getContext();
//        if (cropImage.mSaving) {
//            return false;
//        }
//        
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                if (cropImage.mWaitingToPick) {
//                    recomputeFocus(event);
//                } else {
//                	// single touch to move
//            		HighlightView hv = mHighlightViews.get(0);
//            		int edge = hv.getHit(event.getX(), event.getY());
//                    if (edge != HighlightView.GROW_NONE) {
//                        mMotionEdge = edge;
//                        mMotionHighlightView = hv;
//                        mLastX = event.getX();
//                        mLastY = event.getY();
//                        mMotionHighlightView.setMode(HighlightView.ModifyMode.Move);
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//            	mMotionEdge = HighlightView.GROW;
//            	mMulLastX = Math.abs(event.getX(0) - event.getX(1));
//            	mMulLastY = Math.abs(event.getY(0) - event.getY(1));
//            	break;
//            case MotionEvent.ACTION_POINTER_UP:
//            	
//            	break;
//            case MotionEvent.ACTION_UP:
//                if (cropImage.mWaitingToPick) {
//                    for (int i = 0; i < mHighlightViews.size(); i++) {
//                        HighlightView hv = mHighlightViews.get(i);
//                        if (hv.hasFocus()) {
//                            cropImage.mCrop = hv;
//                            for (int j = 0; j < mHighlightViews.size(); j++) {
//                                if (j == i) {
//                                    continue;
//                                }
//                                mHighlightViews.get(j).setHidden(true);
//                            }
//                            centerBasedOnHighlightView(hv);
//                            ((CropPhotoActivity) this.getContext()).mWaitingToPick = false;
//                            return true;
//                        }
//                    }
//                } else if (mMotionHighlightView != null) {
//                    centerBasedOnHighlightView(mMotionHighlightView);
//                    mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
//                }
//                mMotionHighlightView = null;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (cropImage.mWaitingToPick) {
//                    recomputeFocus(event);
//                } else if (mMotionHighlightView != null) {
//                	if(mMotionEdge == HighlightView.GROW){
//                		if(event.getPointerCount() > 1){
//	            			float mMulNewX = Math.abs(event.getX(0) - event.getX(1));
//	            			float mMulNewY = Math.abs(event.getY(0) - event.getY(1));
//	            			
//	                		mMotionHighlightView.handleMotion(mMotionEdge, mMulNewX - mMulLastX, mMulNewY - mMulLastY);
//	                		
//	                		mMulLastX = mMulNewX;
//	                		mMulLastY = mMulNewY;
//                		}
//                	} else if(mMotionEdge == HighlightView.MOVE){
//                		mMotionHighlightView.handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
//                        mLastX = event.getX();
//                        mLastY = event.getY();
//
//                        if (true) {
//                            // This section of code is optional. It has some user
//                            // benefit in that moving the crop rectangle against
//                            // the edge of the screen causes scrolling but it means
//                            // that the crop rectangle is no longer fixed under
//                            // the user's finger.
//                            
//                        	ensureVisible(mMotionHighlightView);
//                        }
//                	}
//                	
//                    
//                }
//                break;
//        }
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_UP:
//                center(true, true);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                // if we're not zoomed then there's no point in even allowing
//                // the user to move the image around.  This call to center puts
//                // it back to the normalized location (with false meaning don't
//                // animate).
//            	
//                if (getScale() == 1F) {
////                    center(true, true);
//                	center(false, true);
//                }
//                break;
//        }
    	
    	// new cropping method
    	
    	
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
//        Log.d("test4", "this.getBottom() = " + this.getBottom() + ", r.bottom = " + r.bottom);
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

         if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float [] coordinates = new float[] {hv.mCropRect.centerX(),
                                                hv.mCropRect.centerY()};
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
    }

    public void add(HighlightView hv) {
        mHighlightViews.add(hv);
        invalidate();
    }

	public int getTopPadding() {
		return topPadding;
	}

	public void setTopPadding(int topPadding) {
		this.topPadding = topPadding;
	}
}