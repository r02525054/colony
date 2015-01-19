/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.colonycount.cklab.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.CountColonyAsyncTask;
import com.colonycount.cklab.crop.CropImageView;
import com.colonycount.cklab.crop.HighlightView;
import com.colonycount.cklab.crop.IImage;
import com.colonycount.cklab.crop.IImageList;
import com.colonycount.cklab.crop.ImageManager;
import com.colonycount.cklab.crop.MonitoredActivity;
import com.colonycount.cklab.crop.Util;
import com.colonycount.cklab.model.DataWrapper;
import com.google.android.gms.internal.hv;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropPhotoActivity extends MonitoredActivity implements AsyncTaskCompleteListener<Boolean>{
    private static final String TAG = "CropImage";
    
    private static final int LAUNCH_COUNT_COLONY_TASK = 0;
    
    // These are various options can be specified in the intent.
    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only used with mSaveUri
    private int mOutputQuality = 100; // only used with mSaveUri and JPEG format
    private Uri mSaveUri = null;
    private boolean mSetWallpaper = false;
    private int mAspectX, mAspectY;
    private boolean mDoFaceDetection = false;
    private boolean mCircleCrop = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper()){
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case LAUNCH_COUNT_COLONY_TASK:
    			Bitmap croppedImage = (Bitmap) msg.obj;
//    			Log.d("test", "in handleMessage: width = " + croppedImage.getWidth() + ", height = " + croppedImage.getHeight());
//    			
//    			AsyncTaskPayload asyncTaskPayload = new AsyncTaskPayload();
//    			asyncTaskPayload.setRawImg(croppedImage);
//    			new CountColonyAsyncTask(context, "系統訊息", "計算中，請稍後...", asyncTaskCompleteListener, CountColonyAsyncTask.class).execute(asyncTaskPayload);
    			
    			ImageView showImageTest = (ImageView) findViewById(R.id.show_image_test);
    			showImageTest.setImageBitmap(croppedImage);
    			showImageTest.setVisibility(View.VISIBLE);
    			
    			mImageView.setVisibility(View.GONE);
    			
    			
//    			mImageView.setImageBitmap(croppedImage);
    			break;
    		}
    	}
    };
    
    // These options specifiy the output image size and whether we should
    // scale the output to fit it (or just crop it).
    private int mOutputX, mOutputY;
    private boolean mScale;
    private boolean mScaleUp = true;
    
    public boolean mWaitingToPick; // Whether we are wait the user to pick a face.
    public boolean mSaving;  // Whether the "save" button is already clicked.

    private CropImageView mImageView;
    private ContentResolver mContentResolver;

    private Bitmap mBitmap;
    public HighlightView mCrop;

    private IImageList mAllImages;
    private IImage mImage;
    
    private Context context;
    private AsyncTaskCompleteListener<Boolean> asyncTaskCompleteListener;
    
    private RelativeLayout relativeLayout_root;
    private ImageButton btnClose;
    private ImageButton btnCount;
    
    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private float lastX, lastY;
    private float scaleFactor = 1;
    
    
    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    @Override
	protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mContentResolver = getContentResolver();
        context = this;
        asyncTaskCompleteListener = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_crop_photo);
        
        relativeLayout_root = (RelativeLayout) findViewById(R.id.relativeLayout_root);
        mImageView = (CropImageView) findViewById(R.id.cropImageView);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				CropImageView view = (CropImageView) v;
		        switch (event.getAction() & MotionEvent.ACTION_MASK) {
		        case MotionEvent.ACTION_DOWN:
	                savedMatrix.set(matrix);
	                start.set(event.getX(), event.getY());
	                lastX = event.getX();
	                lastY = event.getY();
	                mode = DRAG;
	                lastEvent = null;
	                break;
	            case MotionEvent.ACTION_POINTER_DOWN:
	            	oldDist = spacing(event);
	                if (oldDist > 10f) {
	                    savedMatrix.set(matrix);
	                    midPoint(mid, event);
	                    mode = ZOOM;
	                }
	                lastEvent = new float[4];
	                lastEvent[0] = event.getX(0);
	                lastEvent[1] = event.getX(1);
	                lastEvent[2] = event.getY(0);
	                lastEvent[3] = event.getY(1);
//	                d = rotation(event);
	                break;
	            case MotionEvent.ACTION_UP:
	            case MotionEvent.ACTION_POINTER_UP:
	                mode = NONE;
	                lastEvent = null;
	                break;
		        case MotionEvent.ACTION_MOVE:
	                if (mode == DRAG) {
//	                	matrix.set(savedMatrix);
	                	float[] values = new float[9];
	                    matrix.getValues(values);
	                    float matrixleft = values[Matrix.MTRANS_X];
	                    float matrixRight = matrixleft + mBitmap.getWidth() * scaleFactor;
	                    float matrixTop = values[Matrix.MTRANS_Y];
	                    float matrixBot = matrixTop + mBitmap.getHeight() * scaleFactor;
	                    
	                    float dx = event.getX() - lastX;
	                    float dy = event.getY() - lastY;
	                    
	                    if(matrixleft + dx > mCrop.mDrawRect.left){
	                    	dx = 0;
	                    }
	                    
	                    if(matrixRight + dx < mCrop.mDrawRect.right){
	                    	dx = 0;
	                    }
	                    
	                    if(matrixTop + dy > mCrop.mDrawRect.top){
	                    	dy = 0;
	                    }
	                    
	                    if(matrixBot + dy < mCrop.mDrawRect.bottom){
	                    	dy = 0;
	                    }
	                    
                    	matrix.postTranslate(dx, dy);
                    	
                    	lastX = event.getX();
                    	lastY = event.getY();
                    	
                    	Log.d("Test2", "matrixleft = " + matrixleft + ", matrixRight = " + matrixRight);
                    	Log.d("Test2", "matrixTop = " + matrixTop + ", matrixBot = " + matrixBot);
	                } else if (mode == ZOOM) {
	                	// TODO: 縮小時一邊碰到底時要縮小另一邊
	                    float newDist = spacing(event);
	                    if (newDist > 10f) {
	                    	float[] values = new float[9];
		                    matrix.getValues(values);
		                    float matrixScaleX = values[Matrix.MSCALE_X];
		                    float matrixScaleY = values[Matrix.MSCALE_Y];
		                    float matrixLeft = values[Matrix.MTRANS_X];
		                    float matrixRight = matrixLeft + mBitmap.getWidth() * scaleFactor;
		                    float matrixTop = values[Matrix.MTRANS_Y];
		                    float matrixBot = matrixTop + mBitmap.getHeight() * scaleFactor;
		                    
	                        float scale = (newDist / oldDist);
	                        
	                        if((mid.x - matrixLeft) * scale < mid.x - mCrop.mDrawRect.left){
	                        	scale = 1;
	                        }
	                        
	                        if((matrixRight - mid.x) * scale < mCrop.mDrawRect.right - mid.x){
	                        	scale = 1;
	                        }
	                        
	                        if((mid.y - matrixTop) * scale < mid.y - mCrop.mDrawRect.top){
	                        	scale = 1;
	                        }
	                        
	                        if((matrixBot - mid.y) * scale < mCrop.mDrawRect.bottom - mid.y){
	                        	scale = 1;
	                        }
	                        
	                        	
	                        matrix.postScale(scale, scale, mid.x, mid.y);
	                        oldDist = newDist;
	                        scaleFactor = matrixScaleX;
	                        Log.d("Test2", "matrixScaleX = " + matrixScaleX);
	                        Log.d("Test2", "matrixleft = " + matrixLeft + ", matrixRight = " + matrixRight);
	                    	Log.d("Test2", "matrixTop = " + matrixTop + ", matrixBot = " + matrixBot);
	                    }
//	                    if (lastEvent != null && event.getPointerCount() == 3) {
//	                        newRot = rotation(event);
//	                        float r = newRot - d;
//	                        float[] values = new float[9];
//	                        matrix.getValues(values);
//	                        float tx = values[2];
//	                        float ty = values[5];
//	                        float sx = values[0];
//	                        float xc = (view.getWidth() / 2) * sx;
//	                        float yc = (view.getHeight() / 2) * sx;
//	                        matrix.postRotate(r, tx + xc, ty + yc);
//	                    }
	                }
	                break;
		        }
		        view.setImageMatrix(matrix);
		        return true;
			}
		});
        
        btnCount = (ImageButton) findViewById(R.id.btn_count);
        btnClose = (ImageButton) findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        // Work-around for devices incapable of using hardware-accelerated clipPath.
        // (android.view.GLES20Canvas.clipPath)
        //
        // See also:
        // - https://code.google.com/p/android/issues/detail?id=20474
        // - https://github.com/lvillani/android-cropimage/issues/20
        //
        if (Build.VERSION.SDK_INT > 10 && Build.VERSION.SDK_INT < 16) { // >= Gingerbread && < Jelly Bean
            mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.getBoolean("circleCrop", false)) {
                mCircleCrop = true;
                mAspectX = 1;
                mAspectY = 1;
                mOutputFormat = Bitmap.CompressFormat.PNG;
            }
            mSaveUri = (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (mSaveUri != null) {
                String outputFormatString = extras.getString("outputFormat");
                if (outputFormatString != null) {
                    mOutputFormat = Bitmap.CompressFormat.valueOf(
                            outputFormatString);
                }
                mOutputQuality = extras.getInt("outputQuality", 100);
            } else {
                mSetWallpaper = extras.getBoolean("setWallpaper");
            }
            mBitmap = (Bitmap) extras.getParcelable("data");
            mAspectX = extras.getInt("aspectX");
            mAspectY = extras.getInt("aspectY");
            mOutputX = extras.getInt("outputX");
            mOutputY = extras.getInt("outputY");
            mScale = extras.getBoolean("scale", true);
            mScaleUp = extras.getBoolean("scaleUpIfNeeded", true);
            mDoFaceDetection = extras.containsKey("noFaceDetection")
                    ? !extras.getBoolean("noFaceDetection")
                    : true;
        }

        if (mBitmap == null) {
            Uri target = intent.getData();
            mAllImages = ImageManager.makeImageList(mContentResolver, target, ImageManager.SORT_ASCENDING);
            mImage = mAllImages.getImageForUri(target);
            if (mImage != null) {
                // Don't read in really large bitmaps. Use the (big) thumbnail
                // instead.
                // TODO when saving the resulting bitmap use the
                // decode/crop/encode api so we don't lose any resolution.
                mBitmap = mImage.thumbBitmap(IImage.ROTATE_AS_NEEDED);
            }
        }

        if (mBitmap == null) {
            finish();
            return;
        }

        // Make UI fullscreen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
//        findViewById(R.id.discard).setOnClickListener(
//                new View.OnClickListener() {
//                    public void onClick(View v) {
//                        setResult(RESULT_CANCELED);
//                        finish();
//                    }
//                });
//
//        findViewById(R.id.save).setOnClickListener(
//                new View.OnClickListener() {
//                    public void onClick(View v) {
//                        onSaveClicked();
//                    }
//                });

        startFaceDetection();
    }
    

    private void startFaceDetection() {
        if (isFinishing()) {
            return;
        }
        
        mImageView.setImageBitmapResetBase(mBitmap, true);
        
        Util.startBackgroundJob(this, null,
                getResources().getString(R.string.runningFaceDetection),
                new Runnable() {
            public void run() {
                final CountDownLatch latch = new CountDownLatch(1);
                final Bitmap b = (mImage != null)
                        ? mImage.fullSizeBitmap(IImage.UNCONSTRAINED,
                        1024 * 1024)
                        : mBitmap;
                mHandler.post(new Runnable() {
                    public void run() {
                        if (b != mBitmap && b != null) {
                            mImageView.setImageBitmapResetBase(b, true);
                            
                            ViewTreeObserver vto = mImageView.getViewTreeObserver();
                            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    			@SuppressLint("NewApi")
                    			@Override
                    			public void onGlobalLayout() {
                    				// TODO Auto-generated method stub
                    				mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    int finalHeight = mImageView.getHeight();
                                    
                                    // add bottom bar
                                    RelativeLayout top = (RelativeLayout) findViewById(R.id.relativeLayout_top);
//                                    final ImageView botBar = new ImageView(context);
//                                    botBar.setBackgroundResource(R.color.btn_background);
                    		    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//                    		    	params.leftMargin = 0;
//                    		    	params.topMargin = finalHeight + top.getHeight();
//                    		    	relativeLayout_root.addView(botBar, params);
                    		    	
                    		    	Display display = getWindowManager().getDefaultDisplay();
                    		    	Point size = new Point();
                    		    	display.getSize(size);
                    		    	
                    		    	// add button count
//                    		    	ImageButton btnCount = new ImageButton(context);
//                    		    	btnCount.setImageResource(R.drawable.btn_count);
//                    		    	btnCount.setBackgroundResource(R.drawable.selector_btn_clicked);
//                    		    	int buttonR = (size.y - top.getHeight() - finalHeight - 10) / 2;
//                    		    	btnCount.setMinimumWidth(2*buttonR);
//                    		    	btnCount.setMinimumHeight(2*buttonR);
//                    		    	btnCount.setMaxWidth(300);
//                    		    	btnCount.setMaxHeight(300);
//                    		    	btnCount.setScaleType(ScaleType.FIT_CENTER);
//                    		    	btnCount.setPadding(0, 0, 0, 0);
//                    		    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//                    		    	params.leftMargin = size.x / 2 - 2 * buttonR;
//                    		    	params.topMargin = finalHeight + top.getHeight() + 5;
//                    		    	relativeLayout_root.addView(btnCount, params);
                    		    	
                    		    	btnCount.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											onSaveClicked();
										}
									});
                    		    	
                    		    	mImageView.setTopPadding(top.getHeight());
//                    		    	Log.d("test4", "size.y = " + size.y + ", finalHeight = " + finalHeight + ", top.getHeight() = " + top.getHeight());
                    			}
                            });
                            
                            mBitmap.recycle();
                            mBitmap = b;
                        }
                        if (mImageView.getScale() == 1F) {
                            mImageView.center(true, true);
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mRunFaceDetection.run();
            }
        }, mHandler);
    }

    private void onSaveClicked() {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mCrop == null) {
            return;
        }

        if (mSaving) return;
//        mSaving = true;

        Bitmap croppedImage;

        // If the output is required to a specific size, create an new image
        // with the cropped image in the center and the extra space filled.
        if (mOutputX != 0 && mOutputY != 0 && !mScale) {
            // Don't scale the image but instead fill it so it's the
            // required dimension
//            croppedImage = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.RGB_565);
        	croppedImage = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(croppedImage);
            

            Rect srcRect = mCrop.getCropRect();
            Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

            int dx = (srcRect.width() - dstRect.width()) / 2;
            int dy = (srcRect.height() - dstRect.height()) / 2;

            // If the srcRect is too big, use the center part of it.
            srcRect.inset(Math.max(0, dx), Math.max(0, dy));

            // If the dstRect is too big, use the center part of it.
            dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

            // Draw the cropped bitmap in the center
            canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

            // Release bitmap memory as soon as possible
            mImageView.clear();
            mBitmap.recycle();
        } else {
            Rect r = mCrop.getCropRect();

            int width = r.width();
            int height = r.height();
            
            // If we are circle cropping, we want alpha channel, which is the 
            // third param here.
//            croppedImage = Bitmap.createBitmap(width, height, mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            
            Canvas canvas = new Canvas(croppedImage);
            
            // TODO
            float[] values = new float[9];
            matrix.getValues(values);
            float matrixScaleX = values[Matrix.MSCALE_X];
            float matrixScaleY = values[Matrix.MSCALE_Y];
            float matrixLeft = values[Matrix.MTRANS_X];
            float matrixRight = matrixLeft + mBitmap.getWidth() * scaleFactor;
            float matrixTop = values[Matrix.MTRANS_Y];
            float matrixBot = matrixTop + mBitmap.getHeight() * scaleFactor;
            
            int diameter = mCrop.mDrawRect.right - mCrop.mDrawRect.left;
            int left = (int)(mCrop.mDrawRect.left-matrixLeft);
            int top = (int)(mCrop.mDrawRect.top-matrixTop);
            int right = (int)((left + diameter) / scaleFactor);
            int bot = (int)((top+diameter) / scaleFactor);
            
//            Rect dstRect = new Rect(left, top, right, bot);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(mBitmap, dstRect, r, null);

            Log.d("Test2", "left = " + left + ", top = " + top + ", right = " + right + ", bot = " + bot);
            
            // Release bitmap memory as soon as possible
//            mImageView.clear();
//            mBitmap.recycle();

            if (mCircleCrop) {
                // OK, so what's all this about?
                // Bitmaps are inherently rectangular but we want to return
                // something that's basically a circle.  So we fill in the
                // area around the circle with alpha.  Note the all important
                // PortDuff.Mode.CLEAR.
                Canvas c = new Canvas(croppedImage);
                Path p = new Path();
                p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
                c.clipPath(p, Region.Op.DIFFERENCE);
                c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
            }

            // If the required dimension is specified, scale the image.
            if (mOutputX != 0 && mOutputY != 0 && mScale) {
                croppedImage = Util.transform(new Matrix(), croppedImage, mOutputX, mOutputY, mScaleUp, Util.RECYCLE_INPUT);
            }
        }

//        mImageView.setImageBitmapResetBase(croppedImage, true);
//        mImageView.center(true, true);
//        mImageView.mHighlightViews.clear();

        // Return the cropped image directly or save it to the specified URI.
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable("data") != null || myExtras.getBoolean("return-data"))) {
            Bundle extras = new Bundle();
            extras.putParcelable("data", croppedImage);
            setResult(RESULT_OK, (new Intent()).setAction("inline-data").putExtras(extras));
            finish();
        } else {
            final Bitmap b = croppedImage;
            final int msdId = mSetWallpaper ? R.string.wallpaper : R.string.savingImage;
            
//            Util.startBackgroundJob(this, null,
//                    getResources().getString(msdId),
//                    new Runnable() {
//                public void run() {
//                    saveOutput(b);
//                }
//            }, mHandler);
            
            Util.startBackgroundJob(this, new Runnable() {
	          public void run() {
	              saveOutput(b);
	          }
	        }, mHandler);
        }
    }

    private void saveOutput(Bitmap croppedImage) {
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, mOutputQuality, outputStream);
                }
            } catch (IOException ex) {
                // TODO: report error to caller
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
            } finally {
                Util.closeSilently(outputStream);
            }
            
            mHandler.obtainMessage(LAUNCH_COUNT_COLONY_TASK, croppedImage).sendToTarget();
        } else if (mSetWallpaper) {
            try {
                WallpaperManager.getInstance(this).setBitmap(croppedImage);
                setResult(RESULT_OK);
            } catch (IOException e) {
                Log.e(TAG, "Failed to set wallpaper.", e);
                setResult(RESULT_CANCELED);
            }
        } else {
            Bundle extras = new Bundle();
            extras.putString("rect", mCrop.getCropRect().toString());

            File oldPath = new File(mImage.getDataPath());
            File directory = new File(oldPath.getParent());

            int x = 0;
            String fileName = oldPath.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));

            // Try file-1.jpg, file-2.jpg, ... until we find a filename which
            // does not exist yet.
            while (true) {
                x += 1;
                String candidate = directory.toString()
                        + "/" + fileName + "-" + x + ".jpg";
                boolean exists = (new File(candidate)).exists();
                if (!exists) {
                    break;
                }
            }

            try {
                int[] degree = new int[1];
                Uri newUri = ImageManager.addImage(
                        mContentResolver,
                        mImage.getTitle(),
                        mImage.getDateTaken(),
                        null,    // TODO this null is going to cause us to lose
                                 // the location (gps).
                        directory.toString(), fileName + "-" + x + ".jpg",
                        croppedImage, null,
                        degree);
                
                setResult(RESULT_OK, new Intent()
                        .setAction(newUri.toString())
                        .putExtras(extras));
            } catch (Exception ex) {
                // basically ignore this or put up
                // some ui saying we failed
                Log.e(TAG, "store image fail, continue anyway", ex);
            }
        }

//        final Bitmap b = croppedImage;
//        mHandler.post(new Runnable() {
//            public void run() {
//                mImageView.clear();
//                b.recycle();
//            }
//        });
        
        
//        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAllImages != null) {
            mAllImages.close();
        }
        super.onDestroy();
    }

    Runnable mRunFaceDetection = new Runnable() {
        @SuppressWarnings("hiding")
        float mScale = 1F;
        Matrix mImageMatrix;
        FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
        int mNumFaces;

        // For each face, we create a HightlightView for it.
        private void handleFace(FaceDetector.Face f) {
            PointF midPoint = new PointF();

            int r = ((int) (f.eyesDistance() * mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= mScale;
            midPoint.y *= mScale;

            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;

            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            RectF faceRect = new RectF(midX, midY, midX, midY);
            faceRect.inset(-r, -r);
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }

            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }

            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right,
                               faceRect.right - imageRect.right);
            }

            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom,
                               faceRect.bottom - imageRect.bottom);
            }

            hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
                     mAspectX != 0 && mAspectY != 0);

            mImageView.add(hv);
        }

        // Create a default HightlightView if we found no face in the picture.
        private void makeDefault() {
            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);
            
            // make the default size to (width or height) - 20
            int cropWidth = Math.min(width, height) - 20;
            int cropHeight = cropWidth;

            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX;
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY;
                }
            }

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
            mImageView.add(hv);
        }

        // Scale the image down for faster face detection.
        private Bitmap prepareBitmap() {
            if (mBitmap == null ||  mBitmap.isRecycled()) {
                return null;
            }

            // 256 pixels wide is enough.
            if (mBitmap.getWidth() > 256) {
                mScale = 256.0F / mBitmap.getWidth();
            }
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            
            return faceBitmap;
        }

        public void run() {
            mImageMatrix = mImageView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();

            mScale = 1.0F / mScale;
            if (faceBitmap != null && mDoFaceDetection) {
                FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
                        faceBitmap.getHeight(), mFaces.length);
                mNumFaces = detector.findFaces(faceBitmap, mFaces);
            }

            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle();
            }

            mHandler.post(new Runnable() {
                public void run() {
                    mWaitingToPick = mNumFaces > 1;
                    if (mNumFaces > 0) {
                    	Log.d("test", "mNumFaces > 0, not make default");
                        for (int i = 0; i < mNumFaces; i++) {
                            handleFace(mFaces[i]);
                        }
                    } else {
                    	Log.d("test", "make default calls");
                        makeDefault();
                    }
                    
                    mImageView.invalidate();
                    if (mImageView.mHighlightViews.size() == 1) {
                        mCrop = mImageView.mHighlightViews.get(0);
                        mCrop.setFocus(true);
                    }

                    if (mNumFaces > 1) {
                        Toast t = Toast.makeText(CropPhotoActivity.this,
                                R.string.multiface_crop_help,
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
        }
    };

	@SuppressLint("NewApi")
	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		if(taskName.equals("CountColonyAsyncTask")){
//			Intent intent = new Intent(this, ResultActivity.class);
			
			Intent intent = new Intent(this, Test2.class);
			
			if(result.getRawImg() != null){
				Bitmap bitmap = result.getRawImg();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 100, bos);//100 is the best quality possibe
				byte[] square = bos.toByteArray();
				intent.putExtra("pictureData", square);	
			}
			
			if(result.getComponents() != null){
				intent.putExtra("pictureComponent",  new DataWrapper(result.getComponents()));
			}
			
			startActivity(intent);
			finish();
		}
	}
}
