package com.colonycount.cklab.activity;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colonycount.cklab.activity.base.GPlusClientActivity;
import com.colonycount.cklab.asynctask.CountColonyAsyncTask;
import com.colonycount.cklab.asynctask.base.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.base.AsyncTaskPayload;
import com.colonycount.cklab.config.Config;
import com.colonycount.cklab.model.DataWrapper;

public class TakePhotoActivity extends GPlusClientActivity implements SurfaceHolder.Callback, AsyncTaskCompleteListener<Boolean>{
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
    // The first rear facing camera
    int defaultCameraId;
    
    private SurfaceView mSurfaceView;
    private RelativeLayout relativeLayout_root;
    private RelativeLayout relativeLayout_top;
    private RelativeLayout relativeLayout_take_photo_bot_layout;
    private RelativeLayout relativeLayout_take_photo_done_bot_layout;
    
    private ImageView photoPreview;
    private ImageButton btnClose;
    private ImageButton btnCapture;
    private ImageButton btnReCapture;
    private ImageButton btnCount;
    private TextView titleMsg;
    
    private AsyncTaskCompleteListener<Boolean> asyncTaskCompleteListener;
    private Bitmap rawImg;
    
    private int rotation;
    private boolean previewing;
    private int circleViewRadius;
    
    public static final int TAKE_PHOTO = 0;
    public static final int SELECT_PHOTO = 1;
	
    private CameraOrientationListener myOrientationListener;
    
    SurfaceHolder mHolder;
    Size mPreviewSize;
    
    private Context context;
    
    private Activity activity;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		int requestCode = intent.getIntExtra("requestCode", -1);
		if(requestCode != -1){
			if(requestCode == TAKE_PHOTO){
				setFullScreen();
		        setContentView(R.layout.layout_take_photo_preview);
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
    	relativeLayout_take_photo_bot_layout = (RelativeLayout) findViewById(R.id.take_photo_bot_layout);
    	relativeLayout_take_photo_done_bot_layout = (RelativeLayout) findViewById(R.id.take_photo_done_bot_layout);
    	
    	titleMsg = (TextView) findViewById(R.id.title_msg);
    	photoPreview = (ImageView) findViewById(R.id.photo_preview);
    	btnClose = (ImageButton) findViewById(R.id.btn_close);
    	btnCapture = (ImageButton) findViewById(R.id.btn_capture);
    	btnReCapture = (ImageButton) findViewById(R.id.btn_recapture);
    	btnCount = (ImageButton) findViewById(R.id.btn_count);
    	
    	mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
    	context = this;
    	asyncTaskCompleteListener = this;
    	activity = this;
    	
    	final Point windowSize = getWindowSize();
    	
    	ViewTreeObserver observer = relativeLayout_top.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
		        int rel_top_height = relativeLayout_top.getHeight();
		        
		    	// add surfaceView
		        if(mCamera == null){
			        mCamera = Camera.open(defaultCameraId);
			        rotation = TakePhotoActivity.setCameraDisplayOrientation(activity, defaultCameraId, mCamera);
		        }
		        
		        Camera.Parameters parameters = mCamera.getParameters();
		        final Camera.Size optPicSize = getOptimalSize(parameters.getSupportedPictureSizes(), 1280, 960);
	            Camera.Size optPreviewSize = getOptimalSize(parameters.getSupportedPreviewSizes(), optPicSize.width, optPicSize.height);
	            
		        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		        double scale = (double)optPreviewSize.width / (double)optPreviewSize.height;
		    	params = new RelativeLayout.LayoutParams(windowSize.x, (int)(windowSize.x * scale));
		    	params.leftMargin = 0;
		    	params.topMargin = rel_top_height;
		    	mSurfaceView.setLayoutParams(params);
		    	
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
//											int[] pixels = new int[windowSize.x * windowSize.x];//the size of the array is the dimensions of the sub-photo
//									        rawImg = BitmapFactory.decodeByteArray(data , 0, data.length);
//									        rawImg.getPixels(pixels, 0, windowSize.x, 0, 0, windowSize.x, windowSize.x);//the stride value is (in my case) the width value
//									        rawImg = Bitmap.createBitmap(pixels, 0, windowSize.x, windowSize.x, windowSize.x, Config.ARGB_8888);//ARGB_8888 is a good quality configuration
											
											int stride = Math.min(optPicSize.width, optPicSize.height);
											double radiusScale = stride / (double)windowSize.x;
											stride = (int) (circleViewRadius * 2 * radiusScale);
											int[] pixels = new int[stride * stride]; //the size of the array is the dimensions of the sub-photo
									        rawImg = BitmapFactory.decodeByteArray(data , 0, data.length);
									        rawImg.getPixels(pixels, 0, stride, (int)(5*radiusScale), (int)(5*radiusScale), stride, stride);//the stride value is (in my case) the width value
									        rawImg.recycle();
									        // create square bitmap
									        Bitmap temp = Bitmap.createBitmap(pixels, 0, stride, stride, stride, Bitmap.Config.ARGB_8888);//ARGB_8888 is a good quality configuration
									        // scale to 1024x1024
									        Bitmap temp2 = Bitmap.createScaledBitmap(temp, Config.OUTPUT_IMAGE_WIDTH, Config.OUTPUT_IMAGE_HEIGHT, false);
									        // release memory
									        temp.recycle();
									        temp = null;
									        
									        // crop to circle
									        rawImg = Bitmap.createBitmap(temp2.getWidth(), temp2.getHeight(), Bitmap.Config.ARGB_8888);
									        rawImg.eraseColor(Color.BLACK);
									        Path path = new Path();
									        path.addCircle(temp2.getWidth()/2, temp2.getHeight()/2, temp2.getWidth()/2, Path.Direction.CCW);
									        Canvas canvas = new Canvas(rawImg);
									        canvas.clipPath(path);
									        canvas.drawBitmap(temp2, 0, 0, null);
									        
									        // release memory
									        temp2.recycle();
									        temp2 = null;
//									        Log.d("test4", "rawImg width = " + rawImg.getWidth() + ", rawImg height = " + rawImg.getHeight());
									        
									        photoPreview.setImageBitmap(rawImg);
									        photoPreview.setRotation(rotation);
									        
									        mSurfaceView.setVisibility(View.GONE);
									        circleHintView.setVisibility(View.GONE);
									        relativeLayout_take_photo_bot_layout.setVisibility(View.GONE);
									        
									        photoPreview.setVisibility(View.VISIBLE);
									        relativeLayout_take_photo_done_bot_layout.setVisibility(View.VISIBLE);
									        
									    	titleMsg.setText("選擇重新拍攝或開始計算菌落");
										}
									});
								} else {
									Toast.makeText(context, "對焦失敗，請重試一次", Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				});
		    	
		    	btnReCapture.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						rawImg.recycle();
						rawImg = null;
						
						mSurfaceView.setVisibility(View.VISIBLE);
				        circleHintView.setVisibility(View.VISIBLE);
				        relativeLayout_take_photo_bot_layout.setVisibility(View.VISIBLE);
				        
				        photoPreview.setVisibility(View.GONE);
				        relativeLayout_take_photo_done_bot_layout.setVisibility(View.GONE);
				    	
				    	titleMsg.setText("將圓圈對準培養皿拍攝照片");
					}
				});
		    	
		    	btnCount.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AsyncTaskPayload asyncTaskPayload = new AsyncTaskPayload();
						// rotate rawImg with value "rotation"
						Matrix matrix = new Matrix();
					    matrix.postRotate(rotation);
					    rawImg = Bitmap.createBitmap(rawImg, 0, 0, rawImg.getWidth(), rawImg.getHeight(), matrix, true); 
		    			asyncTaskPayload.setRawImg(rawImg);
		    			new CountColonyAsyncTask(context, "系統訊息", "計算中，請稍後...", asyncTaskCompleteListener, CountColonyAsyncTask.class, true).execute(asyncTaskPayload);
					}
				});
		    	
		        relativeLayout_top.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		    }
        });
        
        btnClose.setOnClickListener(new View.OnClickListener() {
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
			Paint paint = new Paint();
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

    @SuppressWarnings("deprecation")
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
        if(mCamera == null){
	        mCamera = Camera.open(defaultCameraId);
	        rotation = TakePhotoActivity.setCameraDisplayOrientation(this, defaultCameraId, mCamera);
        }
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
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	if(previewing){
    		mCamera.stopPreview();
    		previewing = false;
    	}
    	
    	if(mCamera != null){
    		Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            
            Camera.Size optPicSize = getOptimalSize(parameters.getSupportedPictureSizes(), 1280, 960);
            parameters.setPictureSize(optPicSize.width, optPicSize.height);
            Camera.Size optPreviewSize = getOptimalSize(parameters.getSupportedPreviewSizes(), optPicSize.width, optPicSize.height);
            parameters.setPreviewSize(optPreviewSize.width, optPreviewSize.height);
            
            parameters.setJpegQuality(100);
            parameters.setRotation(90);
            
            mCamera.setParameters(parameters);
            try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
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
    
    
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h){
    	double previewRatio = (double)w / (double)h;
    	double minDiff = Double.MAX_VALUE;
    	double minPixelDiff = Double.MAX_VALUE;
    	
    	Camera.Size curSize = null;
    	for(Camera.Size s : sizes){
    		double curRation = (double)s.width / (double)s.height;
    		if(Math.abs(curRation - previewRatio) < minDiff){
    			minDiff = Math.abs(curRation - previewRatio);
    			curSize = s;
    		} else if(Math.abs(curRation - previewRatio) == minDiff){
    			if(Math.abs((s.width * s.height) - 1024*1024) < minPixelDiff){
    				minPixelDiff = Math.abs((s.width * s.height) - 1024*1024);
    				curSize = s;
    			}
    		}
    		
    		if(curSize != null)
    			Log.d("test4", "size width = " + curSize.width + ", height = " + curSize.height);
    	}
    	
    	return curSize;
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
		if(taskName.equals("CountColonyAsyncTask")){
			if(result.getValue("result").equals("success")){
				if(rawImg != null && !rawImg.isRecycled()){
			    	rawImg.recycle();
			    	rawImg = null;
			    }
				
				Intent intent = new Intent(this, ResultActivity.class);
				intent.putExtra("image", result.getValue("image"));
				intent.putExtra("imageComponent",  new DataWrapper(result.getComponents()));
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(this, "count colony error", Toast.LENGTH_LONG).show();
			}
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		
		if(rawImg != null && !rawImg.isRecycled()){
			rawImg.recycle();
			rawImg = null;
		}
	}
}
