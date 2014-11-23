package com.colonycount.cklab.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.CountColonyAsyncTask;
import com.colonycount.cklab.base.GPlusClientActivity;


public class TakePhotoActivity extends GPlusClientActivity implements SurfaceHolder.Callback, AsyncTaskCompleteListener<Boolean>{
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
    // The first rear facing camera
    int defaultCameraId;
    
    private SurfaceView mSurfaceView;
    private RelativeLayout relativeLayout_root;
    private RelativeLayout relativeLayout_top;
    private ImageView imageView_bot;
    private ImageView photoPreview;
    private ImageButton button1;
    private ImageButton btnCapture;
    private ImageButton btnReCapture;
    private ImageButton btnCount;
    
    private AsyncTaskCompleteListener<Boolean> asyncTaskCompleteListener;
    private Bitmap rawImg;
    
    private int rotation;
    private boolean previewing;
    private int circleViewRadius;
    
    public static final int TAKE_PHOTO = 0;
    public static final int SELECT_PHOTO = 1;
    public static final int REQUEST_WIDTH = 480;
	public static final int REQUEST_HEIGHT = 640;
	
    private CameraOrientationListener myOrientationListener;
    
    SurfaceHolder mHolder;
    Size mPreviewSize;
    
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		int requestCode = intent.getIntExtra("requestCode", -1);
		if(requestCode != -1){
			if(requestCode == TAKE_PHOTO){
				setFullScreen();
		        setContentView(R.layout.test);
		        setViews();
		        
		        // Find the total number of cameras available
		        numberOfCameras = Camera.getNumberOfCameras();

		        // Find the ID of the default camera
		        CameraInfo cameraInfo = new CameraInfo();
		        for (int i = 0; i < numberOfCameras; i++) {
		            Camera.getCameraInfo(i, cameraInfo);
		            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
		                defaultCameraId = i;
		            }
		        }
			} 
		}
    }
    
    
    public String getFilePathFromUri(Uri uri){
		CursorLoader loader = new CursorLoader(this, uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
		Cursor cursor = loader.loadInBackground();
		cursor.moveToFirst();
		String path = cursor.getString(0);
		
		return path;
	}
    
    public Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inMutable = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
    
    
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    
	    //Log.d(TAG, "sampleSize:" + inSampleSize);
	    //Log.d(TAG, "height:" + height/inSampleSize + ", width:" + width/inSampleSize);
	    return inSampleSize;
	}
    
    
    private void setViews(){
    	relativeLayout_root = (RelativeLayout) findViewById(R.id.relativeLayout_root);
    	relativeLayout_top = (RelativeLayout) findViewById(R.id.relativeLayout_top);
    	photoPreview = (ImageView) findViewById(R.id.photo_preview);
    	button1 = (ImageButton) findViewById(R.id.button1);
    	mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
    	context = this;
    	asyncTaskCompleteListener = this;
    	
    	final Point windowSize = getWindowSize();
    	
    	ViewTreeObserver observer = relativeLayout_top.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
		        int rel_top_height = relativeLayout_top.getHeight();
		        
		        // add bottom bar
		        imageView_bot = new ImageView(context);
		    	imageView_bot.setBackgroundResource(R.color.btn_background);
		    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		    	params.leftMargin = 0;
		    	params.topMargin = rel_top_height + windowSize.x;
		    	relativeLayout_root.addView(imageView_bot, params);
		    	
		    	// add surfaceView
		    	double scale = 640.0 / 480.0;
		    	params = new RelativeLayout.LayoutParams(windowSize.x, (int)(windowSize.x * scale));
		    	params.leftMargin = 0;
		    	params.topMargin = rel_top_height;
		    	mSurfaceView.setLayoutParams(params);
		    	
		    	
		    	// add bottom capture button
		    	int radius = (windowSize.y - rel_top_height - windowSize.x) / 4;
		    	btnCapture = new ImageButton(context);
		    	btnCapture.setImageResource(R.drawable.btn_capture);
		    	btnCapture.setBackgroundResource(R.drawable.selector_btn_clicked);
		    	btnCapture.setPadding(0,0,0,0);
		    	btnCapture.setMaxWidth(300);
		    	btnCapture.setMaxHeight(300);
		    	btnCapture.setMinimumWidth(radius*2);
		    	btnCapture.setMinimumHeight(radius*2);
		    	btnCapture.setScaleType(ScaleType.FIT_CENTER);
		    	Log.d("test4", "bot height = " +(windowSize.y - rel_top_height - windowSize.x));
		    	Log.d("test4", "btn width = " + btnCapture.getMinimumWidth());
		    	Log.d("test4", "btn height = " + btnCapture.getMinimumHeight());
		    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    	params.leftMargin = windowSize.x / 2 - radius;
		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - radius;
		    	relativeLayout_root.addView(btnCapture, params);
		    	
		    	// add red circle hint view
		    	circleViewRadius = windowSize.x / 2 - 5;
		    	final CircleHintView circleHintView = new CircleHintView(context, windowSize.x/2, rel_top_height+windowSize.x/2, circleViewRadius);
		    	relativeLayout_root.addView(circleHintView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		    	
		    	btnCapture.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						myOrientationListener.rememberOrientation();
						rotation += myOrientationListener.getRememberedOrientation();
						rotation = rotation % 360;
						Log.d("test", "pic taken, rotation:" + rotation);
						mCamera.autoFocus(new AutoFocusCallback() {
							@Override
							public void onAutoFocus(boolean success, Camera camera) {
								if(success){
									mCamera.takePicture(null, null, new PictureCallback() {
										@Override
										public void onPictureTaken(byte[] data, Camera camera) {
											// TODO Auto-generated method stub
											int[] pixels = new int[windowSize.x * windowSize.x];//the size of the array is the dimensions of the sub-photo
									        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//									        Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
//									        bitmap.getPixels(pixels, 0, windowSize.x, 0, 0, windowSize.x, windowSize.x);//the stride value is (in my case) the width value
//									        bitmap = Bitmap.createBitmap(pixels, 0, windowSize.x, windowSize.x, windowSize.x, Config.ARGB_8888);//ARGB_8888 is a good quality configuration
									        
									        rawImg = BitmapFactory.decodeByteArray(data , 0, data.length);
									        rawImg.getPixels(pixels, 0, windowSize.x, 0, 0, windowSize.x, windowSize.x);//the stride value is (in my case) the width value
									        rawImg = Bitmap.createBitmap(pixels, 0, windowSize.x, windowSize.x, windowSize.x, Config.ARGB_8888);//ARGB_8888 is a good quality configuration
									        
//									        bitmap.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
//									        byte[] square = bos.toByteArray();
											
									        photoPreview.setImageBitmap(rawImg);
									        photoPreview.setRotation(rotation);
									        
									        mSurfaceView.setVisibility(View.GONE);
									        circleHintView.setVisibility(View.GONE);
									        btnCapture.setVisibility(View.GONE);
									        
									        photoPreview.setVisibility(View.VISIBLE);
									        btnReCapture.setVisibility(View.VISIBLE);
									    	btnCount.setVisibility(View.VISIBLE);
										}
									});
								} else {
									Toast.makeText(context, "對焦失敗，請重試一次", Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				});
		    	
		    	
		    	// add button recapture
		    	btnReCapture = new ImageButton(context);
		    	int buttonSize = 50;
		    	btnReCapture.setImageResource(R.drawable.btn_retaking_photo);
		    	btnReCapture.setBackgroundResource(R.drawable.selector_btn_clicked);
		    	btnReCapture.setPadding(0,0,0,0);
		    	btnReCapture.setMaxWidth(300);
		    	btnReCapture.setMaxHeight(300);
		    	btnReCapture.setMinimumWidth(buttonSize*2);
		    	btnReCapture.setMinimumHeight(buttonSize*2);
		    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    	params.leftMargin = windowSize.x / 4 - buttonSize;
		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - buttonSize;
		    	relativeLayout_root.addView(btnReCapture, params);
		    	
		    	
		    	// add button count
		    	btnCount = new ImageButton(context);
		    	btnCount.setImageResource(R.drawable.btn_count);
		    	btnCount.setBackgroundResource(R.drawable.selector_btn_clicked);
		    	btnCount.setPadding(0,0,0,0);
		    	btnCount.setMaxWidth(200);
		    	btnCount.setMaxHeight(200);
		    	buttonSize = 50;
		    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    	params.leftMargin = windowSize.x / 4 * 3 - buttonSize;
		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - buttonSize;
		    	relativeLayout_root.addView(btnCount, params);

		    	btnReCapture.setVisibility(View.GONE);
		    	btnCount.setVisibility(View.GONE);
		    	
		    	
		    	btnReCapture.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mSurfaceView.setVisibility(View.VISIBLE);
				        circleHintView.setVisibility(View.VISIBLE);
				        btnCapture.setVisibility(View.VISIBLE);
				        
				        photoPreview.setVisibility(View.GONE);
				        btnReCapture.setVisibility(View.GONE);
				    	btnCount.setVisibility(View.GONE);
					}
				});
		    	
		    	btnCount.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AsyncTaskPayload asyncTaskPayload = new AsyncTaskPayload();
		    			asyncTaskPayload.setRawImg(rawImg);
		    			new CountColonyAsyncTask(context, "系統訊息", "計算中，請稍後...", asyncTaskCompleteListener, CountColonyAsyncTask.class).execute(asyncTaskPayload);
					}
				});
		    	
		        relativeLayout_top.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		    }
        });
        
        
        
        button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        
        myOrientationListener = new CameraOrientationListener(this);
        if(myOrientationListener.canDetectOrientation())
        	myOrientationListener.enable();
    }
    
    
    class CameraOrientationListener extends OrientationEventListener {
		private int currentNormalizedOrientation;
	    private int rememberedNormalizedOrientation;
	    
		public CameraOrientationListener(Context context) {
	        super(context, SensorManager.SENSOR_DELAY_NORMAL);
	    }

		@Override
		public void onOrientationChanged(int orientation) {
			// TODO Auto-generated method stub
			if (orientation != ORIENTATION_UNKNOWN) {
	            currentNormalizedOrientation = normalize(orientation);
	        }
		}
		
		private int normalize(int degrees) {
			if (degrees > 315 || degrees <= 45) {
	            return 0;
	        } else if (degrees > 45 && degrees <= 135) {
	            return 90; 
	        } else if (degrees > 135 && degrees <= 225) {
	            return 180;
	        } else if (degrees > 225 && degrees <= 315) {
	            return 270;
	        }
	        
	        throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
		}
		
		public void rememberOrientation() {
	        rememberedNormalizedOrientation = currentNormalizedOrientation;
	    }

	    public int getRememberedOrientation() {
	        return rememberedNormalizedOrientation;
	    }
	}
    
    
    class CircleHintView extends View {
    	private int cx;
    	private int cy;
    	private int radius;
    	
		public CircleHintView(Context context, int cx, int cy, int radius) {
			super(context);
			this.cx = cx;
			this.cy = cy;
			this.radius = radius;
			
			Canvas canvas = new Canvas();
			draw(canvas);
		}

		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			Paint paint = new Paint();
//			paint.setColor(Color.RED);
			paint.setColor(0xFFEF04D6);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);
			paint.setAntiAlias(true);
			canvas.drawCircle(cx, cy, radius, paint);
		}
	}
    
    @SuppressLint("NewApi")
	private Point getWindowSize(){
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	
    	return size;
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // --------------------------------
        // TODO: 1. camera bug here
        // 	     2. camera wrong size: too long!
        Log.d("test", "onResume");
        
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Log.d("test", "set surface view");
        
        
        // Open the default i.e. the first rear facing camera.*/
        mCamera = Camera.open(defaultCameraId);
        rotation = TakePhotoActivity.setCameraDisplayOrientation(this, defaultCameraId, mCamera);
        cameraCurrentlyLocked = defaultCameraId;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("test", "onPause");
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if(previewing)
        	mCamera.stopPreview();
        
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
    	Log.d("test", "surfaceCreated()");
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.d("Test2", "width = " + w + ", height = " + h);
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	if(previewing){
    		mCamera.stopPreview();
    		previewing = false;
    	}
    	
    	if(mCamera != null){
    		Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640 ,480);
            parameters.setJpegQuality(100);
            parameters.setRotation(90);

            mCamera.setParameters(parameters);
            try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            mCamera.startPreview();
            previewing = true;
    	}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.d("test", "surfaceDestroyed()");
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
            previewing = false;
        }
    }

    
	@SuppressLint("NewApi")
	public static int setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
	     CameraInfo info = new CameraInfo();
	     Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	     
	     return result;
	 }


	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		// TODO Auto-generated method stub
		if(taskName.equals("CountColonyAsyncTask")){
//			Bitmap b = result.getRawImg();
//			photoPreview.setImageBitmap(b);
			
			Intent intent = new Intent(this, ResultActivity.class);
			
			Bitmap bitmap = result.getRawImg();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
			byte[] square = bos.toByteArray();
			intent.putExtra("pictureData", square);
			intent.putExtra("pictureRotation", rotation);
			
			startActivity(intent);
			finish();
		}
	}
}
