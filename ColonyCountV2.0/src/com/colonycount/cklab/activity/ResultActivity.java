package com.colonycount.cklab.activity;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.widget.ImageView.ScaleType;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.SaveAndUploadImageAsyncTask;
import com.colonycount.cklab.base.GPlusClientActivity;
import com.colonycount.cklab.crop.CropImageView2;
import com.colonycount.cklab.crop.HighlightView;
import com.colonycount.cklab.crop.HighlightView2;
import com.colonycount.cklab.crop.HighlightView3;
import com.colonycount.cklab.crop.HighlightView4;
import com.colonycount.cklab.crop.HighlightView5;
import com.colonycount.cklab.model.CancelView;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.DataWrapper;
import com.colonycount.cklab.model.ImgInfo;
import com.colonycount.cklab.rangebar.RangeBar;

public class ResultActivity extends GPlusClientActivity implements View.OnClickListener, AsyncTaskCompleteListener<Boolean> {
	private ImageView imageView_bot;
	private ImageButton left;
	private RelativeLayout rel_top;
	private RelativeLayout rel_root;
	private Context context;
	private TextView text_result;
	
	private ImageButton btn_set_tag;
	private ImageButton btn_circle_add;
	private ImageButton btn_circle_sub;
	
	private CropImageView2 image;
	private Bitmap mBitmap;
	private Bitmap mBitmapShow;
	private Bitmap mBitmapShowSub;
	public HighlightView mCrop;
	public boolean mWaitingToPick; // Whether we are wait the user to pick a face.
    public boolean mSaving;  // Whether the "save" button is already clicked.
    
    public int hvCount;
    public State state = State.VIEW;
    
    private TextView showRedColony;
    private TextView showGreenColony;
    private TextView showPurpleColony;
    private boolean isRedColonyShow = true;
    private boolean isGreenColonyShow = true;
    private boolean isPurpleColonyShow = true;
    
    public enum State {
    	VIEW, ADD, SUB, SET_TAG
    }
	
    public HighlightView2 mCrop2;
    private ZoomControls zoomControls;
//    private ImageButton btn_set_tag;
    private ImgInfo imgInfo;
    private ImageButton btn_save_or_ok;
    
    private int areaThreshold = 4;
	private double shapeFactorThreshold = 0.5;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setFullScreen();
		setContentView(R.layout.layout_result);
		setViews();
		setListeners();
		setColony();
	}
	
	
	private void setViews(){
		imgInfo = new ImgInfo(loadPrefIntData("imgNumber", 1));
		left = (ImageButton) findViewById(R.id.btn_result_cancel);
		btn_save_or_ok = (ImageButton) findViewById(R.id.btn_save_or_ok);
		image = (CropImageView2) findViewById(R.id.image2);
		rel_top = (RelativeLayout) findViewById(R.id.relativeLayout_top2);
		rel_root = (RelativeLayout) findViewById(R.id.rel_root2);
		zoomControls = (ZoomControls) findViewById(R.id.zoomControls1);
		zoomControls.setVisibility(View.INVISIBLE);
		context = this;

		showRedColony = (TextView) findViewById(R.id.show_red_colony);
		showGreenColony = (TextView) findViewById(R.id.show_green_colony);
		showPurpleColony = (TextView) findViewById(R.id.show_purple_colony);
		text_result = (TextView) findViewById(R.id.text_result);
		
		byte[] data = getIntent().getByteArrayExtra("pictureData");
		int rotation = getIntent().getIntExtra("pictureRotation", -1);
		mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		// the bitmap able to write
		mBitmapShow = mBitmap.copy(Config.ARGB_8888, true);
		
		if(mBitmapShow != null)
			image.setImageBitmapResetBase(mBitmapShow, true);
		
		Log.d("test", "rotation = " + rotation);
		if(rotation != -1)
			image.setRotation(rotation);
		
		ViewTreeObserver observer = rel_top.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				final Point windowSize = getWindowSize();
				int rel_top_height = rel_top.getHeight();
				
				// my code
				image.setTopPadding(rel_top_height);
				
//				imageView_bot = new ImageView(context);
//		    	imageView_bot.setBackgroundResource(R.drawable.shape_camera_top_bar);
		    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		    	params.leftMargin = 0;
//		    	params.topMargin = rel_top_height + windowSize.x;
//		    	rel_root.addView(imageView_bot, params);
		    	
		    	// add text info
//		    	LinearLayout textContainer = (LinearLayout) findViewById(R.id.container_show_colony);
//		    	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		    	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		    	params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.image2);
//		    	rel_root.addView(textContainer, params);
		    	
//		    	int imageRadius = 40;
//		    	btn_circle_add = new ImageButton(context);
//		    	btn_circle_add.setImageResource(R.drawable.btn_add);
//		    	btn_circle_add.setBackgroundResource(R.drawable.selector_btn_clicked);
//		    	btn_circle_add.setPadding(0,0,0,0);
//		    	btn_circle_add.setScaleType(ScaleType.FIT_CENTER);
//		    	
//		    	btn_circle_sub = new ImageButton(context);
//		    	btn_circle_sub.setImageResource(R.drawable.btn_sub);
//		    	btn_circle_sub.setBackgroundResource(R.drawable.selector_btn_clicked);
//		    	btn_circle_sub.setPadding(0,0,0,0);
//		    	btn_circle_sub.setScaleType(ScaleType.FIT_CENTER);
//		    	
//		    	btn_set_tag = new ImageButton(context);
//		    	btn_set_tag.setImageResource(R.drawable.btn_edit);
//		    	btn_set_tag.setBackgroundResource(R.drawable.selector_btn_clicked);
//		    	btn_set_tag.setPadding(0,0,0,0);
//		    	btn_set_tag.setScaleType(ScaleType.FIT_CENTER);
//		    	
//		    	params = new RelativeLayout.LayoutParams(2*imageRadius, 2*imageRadius);
//		    	params.leftMargin = windowSize.x / 6 - imageRadius;
//		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - imageRadius;
//		    	rel_root.addView(btn_circle_add, params);
//		    	
//		    	params = new RelativeLayout.LayoutParams(2*imageRadius, 2*imageRadius);
//		    	params.leftMargin = windowSize.x / 6 * 3 - imageRadius;
//		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - imageRadius;
//		    	rel_root.addView(btn_circle_sub, params);
//		    	
//		    	params = new RelativeLayout.LayoutParams(2*imageRadius, 2*imageRadius);
//		    	params.leftMargin = windowSize.x / 6 * 5 - imageRadius;
//		    	params.topMargin = rel_top_height + windowSize.x + (windowSize.y - rel_top_height - windowSize.x) / 2 - imageRadius;
//		    	rel_root.addView(btn_set_tag, params);
		    	
//		    	btn_circle_add.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						state = State.ADD;
//						setTitleBar();
//						setFuncBtn();
//						
////						setSaveDoneBtn();
////						addHighlightView();
////	                    image.invalidate();
////						
////	                    mCrop = image.mHighlightViews.get(image.mHighlightViews.size()-1);
////                        mCrop.setFocus(true);
////                        setDisabledBtn();
//					}
//				});
		    	
//		    	btn_circle_sub.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						state = State.SUB;
//						setTitleBar();
//						setFuncBtn();
//						
////						zoomControls.setVisibility(View.VISIBLE);
////						addHighlightView3(0, 0, mBitmapShow.getWidth(), mBitmapShow.getHeight());
////						setSaveDoneBtn();
////						setDisabledBtn();
////						drawHighlightImage();
//					}
//				});
		    	
//		    	btn_set_tag.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
////						Log.d("test", "button save");
////						if(state == State.ADD || state == State.SUB){
////							doDone();
////						} else if(state == State.VIEW){
////							saveImg();
////						}
//						
//						AlertDialog.Builder builder = new AlertDialog.Builder(context);
//						View dialogContent = getLayoutInflater().inflate(R.layout.dialog_add_tag_view, null);
//						builder.setView(dialogContent);
//					    
////					    Button btnSearchStartDate = (Button) dialogContent.findViewById(R.id.btn_search_start_date);
////					    Button btnSearchEndDate = (Button) dialogContent.findViewById(R.id.btn_search_end_date);
////					    btnSearchStartDate.setOnClickListener(new View.OnClickListener() {
////							@Override
////							public void onClick(View v) {
////								// TODO Auto-generated method stub
////								Calendar c = Calendar.getInstance();
////								
////								final DatePickerDialog dateDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
////								    boolean fired = false;
////								    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
////								        if (fired == true) {
////								            return;
////								        } else {
////								            fired = true;
////								        }
////								    }
////								}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
////								dateDialog.show();
////							}
////						});
////					    btnSearchEndDate.setOnClickListener(new View.OnClickListener() {
////							@Override
////							public void onClick(View v) {
////								// TODO Auto-generated method stub
////								
////							}
////						});
//					    
////					    RangeBar rangebar = (RangeBar) dialogContent.findViewById(R.id.rangebar);
////					    TextView rangebarValue1 = (TextView) dialogContent.findViewById(R.id.rangebar_value_1);
////					    TextView rangebarValue2 = (TextView) dialogContent.findViewById(R.id.rangebar_value_2);
////					    TextView rangebarValue3 = (TextView) dialogContent.findViewById(R.id.rangebar_value_3);
////					    TextView rangebarValue4 = (TextView) dialogContent.findViewById(R.id.rangebar_value_4);
////					    TextView rangebarValue5 = (TextView) dialogContent.findViewById(R.id.rangebar_value_5);
////					    TextView rangebarValue6 = (TextView) dialogContent.findViewById(R.id.rangebar_value_6);
////					    TextView rangebarValue7 = (TextView) dialogContent.findViewById(R.id.rangebar_value_7);
////					    rangebarValue1.setText(Html.fromHtml("10<sup><small>-1</small></sup>"));
////					    rangebarValue2.setText(Html.fromHtml("10<sup><small>-2</small></sup>"));
////					    rangebarValue3.setText(Html.fromHtml("10<sup><small>-3</small></sup>"));
////					    rangebarValue4.setText(Html.fromHtml("10<sup><small>-4</small></sup>"));
////					    rangebarValue5.setText(Html.fromHtml("10<sup><small>-5</small></sup>"));
////					    rangebarValue6.setText(Html.fromHtml("10<sup><small>-6</small></sup>"));
////					    rangebarValue7.setText(Html.fromHtml("10<sup><small>-7</small></sup>"));
////					    rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
////					        @Override
////					        public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) { 
////					            Log.d("Test2", "left = " + leftThumbIndex + ", right = " + rightThumbIndex);
////					        }
////					    });
//					    
//					    Button btnSetTagCancel = (Button) dialogContent.findViewById(R.id.btn_set_tag_cancel);
//					    Button btnSetTagOK = (Button) dialogContent.findViewById(R.id.btn_set_tag_ok);
//					    
//					    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//					    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//					    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//					    
//					    final Dialog d = builder.create();
//					    d.show();
//					    d.getWindow().setAttributes(lp);
//					    btnSetTagCancel.setOnClickListener(new View.OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								d.dismiss();
//							}
//						});
//					    
//					    btnSetTagOK.setOnClickListener(new View.OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								d.dismiss();
//							}
//						});
//					}
//		    	});
		    	
		    	rel_root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
//		btn_set_tag.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				PopupMenu popupMenu = new PopupMenu(context, btn_set_tag);
//			    popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
//			    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//					@Override
//					public boolean onMenuItemClick(final MenuItem item) {
//						if(item.getItemId() == R.id.popupmenu_num || item.getItemId() == R.id.popupmenu_type){
//							AlertDialog.Builder builder = new AlertDialog.Builder(context);
//							final EditText input = new EditText(context);
//							if(item.getItemId() == R.id.popupmenu_num){
//								builder.setTitle("設定編號");
//								input.setInputType(InputType.TYPE_CLASS_NUMBER);
//								input.setText(imgInfo.getNumber()+"");
//							} else {
//								builder.setTitle("設定菌種");
//								input.setText(imgInfo.getType());
//							}
//							
//							builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
//						        public void onClick(DialogInterface dialog, int whichButton) {
//						        	if(item.getItemId() == R.id.popupmenu_num)
//						        		imgInfo.setNumber(Integer.parseInt(input.getText().toString()));
//						        	else
//						        		imgInfo.setType(input.getText().toString());
//						        }
//						    });
//							
//							builder.setView(input);
//						    builder.setCancelable(false);
//						    AlertDialog dialog = builder.create();
//						    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//						    dialog.show();
//						} else if(item.getItemId() == R.id.popupmenu_date){
//							final DatePickerDialog dateDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
//							    boolean fired = false;
//							    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
//							        Log.i("PEW PEW", "Double fire check");
//							        if (fired == true) {
//							            Log.i("PEW PEW", "Double fire occured. Silently-ish returning");
//							            return;
//							        } else {
//							            //first time fired
//							            fired = true;
//							        }
//							        //Normal date picking logic goes here
//							        imgInfo.setDate(year, monthOfYear, dayOfMonth);
//							    }
//							}, imgInfo.getYear(), imgInfo.getMonth(), imgInfo.getDay());
//							dateDialog.setCancelable(false);
//							dateDialog.show();
//						}
//							
//						return true;
//					}
//			    });
//			    popupMenu.show();
//			}
//		});
	}
	
	private void setListeners(){
		left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: 設定每一個highlight circle的scale
				float nowScale = image.getScale();
				float maxZoom = image.getMaxZoom();
				float target = (float)(nowScale * 1.25);
				
				if(nowScale == 3F)
					return;
				
				if(target > maxZoom)
					target = 3F;
				
				image.zoomTo(target, image.showView.getCenterX(), image.showView.getCenterY(), 200F);
				
				// TODO:修正位置
				addHighlightView3(image.showView.getCenterX()-mBitmapShow.getWidth()/target/2,
								  image.showView.getCenterY()-mBitmapShow.getHeight()/target/2,
								  image.showView.getCenterX()+mBitmapShow.getWidth()/target/2,
								  image.showView.getCenterY()+mBitmapShow.getHeight()/target/2);
			}
		});
		
		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				float nowScale = image.getScale();
				float target = (float)(nowScale * 0.8);
				
				if(nowScale == 1F)
					return;
				
				if(target < 1F)
					target = 1F;
					
				
				image.zoomTo(target, image.showView.getCenterX(), image.showView.getCenterY(), 200F);
				
				// TODO:修正位置
				addHighlightView3(image.showView.getCenterX()-mBitmapShow.getWidth()/target/2,
								  image.showView.getCenterY()-mBitmapShow.getHeight()/target/2,
								  image.showView.getCenterX()+mBitmapShow.getWidth()/target/2,
								  image.showView.getCenterY()+mBitmapShow.getHeight()/target/2);
			}
		});
		showRedColony.setOnClickListener(this);
		showGreenColony.setOnClickListener(this);
		showPurpleColony.setOnClickListener(this);
	}
	
	public void setColony(){
		DataWrapper dw = (DataWrapper) getIntent().getSerializableExtra("pictureComponent");
		// TODO: check bug - nullpointerexception
		List<Component> components = dw.getParliaments();
		
		for(int i = 0; i < components.size(); i++){
			Component component = components.get(i);
			if(component.getArea() >= areaThreshold && component.getShapeFactor() >= shapeFactorThreshold){
				addHighlightView4((float)component.getCenterX(), (float)component.getCenterY(), (float)component.getRadius());
			} else {
				addHighlightView5((float)component.getCenterX(), (float)component.getCenterY(), (float)component.getRadius());
			}
			
			Log.d("test", "x, y = " + component.getCenterX() + ", " + component.getCenterY() + ", r = " + component.getRadius());
		}
		image.invalidate();
		
		Log.d("test4", "red count = " + image.rViews.size());
		Log.d("test4", "green count = " + image.gViews.size());
		Log.d("test4", "purple count = " + image.myViews.size());
		int rCount = 0, gCount = 0, pCount = 0;
		for(int i = 0; i < image.cViews.size(); i++){
			if(image.cViews.get(i).getType() == CancelView.Type.RED)
				rCount++;
			if(image.cViews.get(i).getType() == CancelView.Type.GREEN)
				gCount++;
			if(image.cViews.get(i).getType() == CancelView.Type.PURPLE)
				pCount++;
		}
		Log.d("test4", "cancelView red count = " + rCount);
		Log.d("test4", "cancelView green count = " + gCount);
		Log.d("test4", "cancelView purple count = " + pCount);
		
		setTextInfo(image.rViews.size(), image.gViews.size(), image.myViews.size());
	}
	
	
	public void setTextInfo(int rCount, int gCount, int pCount){
		text_result.setText("菌落總數:" + rCount + "個");
		showRedColony.setText("菌落:" + rCount);
		showGreenColony.setText("非菌落:" + gCount);
		showPurpleColony.setText("新增:" + pCount);
	}
	
	public void setTitleBar(){
		if(state == State.ADD || state == State.SUB){
			if(state == State.ADD){
				text_result.setText("新增菌落");
			} else if(state == State.SUB){
				text_result.setText("刪除菌落");
			}
			
			// set cancel button
			left.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					state = State.VIEW;
					setTitleBar();
				}
			});
			
			// set ok button
			btn_save_or_ok.setImageResource(R.drawable.btn_check);
			btn_save_or_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					state = State.VIEW;
					setTitleBar();
					// TODO:
					// do save change
				}
			});
		} else if(state == State.VIEW){
			text_result.setText("菌落總數:" + image.rViews.size() + "個");
			// set cancel button
			left.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			
			// set save button
			btn_save_or_ok.setImageResource(R.drawable.btn_save2);
			btn_save_or_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					saveImg();
				}
			});
			
			btn_circle_add.setVisibility(View.VISIBLE);
			btn_circle_sub.setVisibility(View.VISIBLE);
			btn_set_tag.setVisibility(View.VISIBLE);
		}
	}
	
	public void setFuncBtn(){
		if(state == State.ADD || state == State.SUB){
			btn_circle_add.setVisibility(View.GONE);
			btn_circle_sub.setVisibility(View.GONE);
			btn_set_tag.setVisibility(View.GONE);
		} 
	}
	
	
	public void saveImg(){
		// save the image number to sharedpref
		savePrefData("imgNumber", imgInfo.getNumber());
		
		// upload raw image
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mBitmap.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
		byte[] data_raw = bos.toByteArray();
		
		// upload counted image
		// draw counted image
		Canvas canvas = new Canvas(mBitmapShow);
		Paint purplePaint = new Paint();
		purplePaint.setStrokeWidth(2F);
		purplePaint.setStyle(Paint.Style.STROKE);
		purplePaint.setAntiAlias(true);
		purplePaint.setColor(0xFFEF04D6);
		
		Paint greenPaint = new Paint();
		greenPaint.setStrokeWidth(2F);
		greenPaint.setStyle(Paint.Style.STROKE);
		greenPaint.setAntiAlias(true);
		greenPaint.setColor(Color.GREEN);
		
		Paint redPaint = new Paint();
		redPaint.setStrokeWidth(2F);
		redPaint.setStyle(Paint.Style.STROKE);
		redPaint.setAntiAlias(true);
		redPaint.setColor(Color.RED);
		
		// TODO: add enum for colony type
		// colony type:
		// 1: red
		// 2: green
		// 3: purple
		// 4: cancel red
		// 5: cancel green
		// 6: cancel purple
		for(HighlightView2 hv2 : image.myViews){
			canvas.drawCircle(hv2.getCenterX(), hv2.getCenterY(), hv2.getRadius(), purplePaint);
			imgInfo.addColony(hv2.getCenterX(), hv2.getCenterY(), hv2.getRadius(), 3);
		}
		
		for(HighlightView4 hv4 : image.rViews){
			canvas.drawCircle(hv4.getCenterX(), hv4.getCenterY(), hv4.getRadius(), redPaint);
			imgInfo.addColony(hv4.getCenterX(), hv4.getCenterY(), hv4.getRadius(), 1);
		}
		
		for(HighlightView5 hv5 : image.gViews){
			canvas.drawCircle(hv5.getCenterX(), hv5.getCenterY(), hv5.getRadius(), greenPaint);
			imgInfo.addColony(hv5.getCenterX(), hv5.getCenterY(), hv5.getRadius(), 2);
		}
		
		Log.d("test4", "red count = " + image.rViews.size());
		Log.d("test4", "green count = " + image.gViews.size());
		Log.d("test4", "purple count = " + image.myViews.size());
		int rCount = 0, gCount = 0, pCount = 0;
		for(CancelView cv: image.cViews){
			int type = 0;
			if(cv.getType() == CancelView.Type.RED){
				type = 4;
				rCount++;
			}
			else if(cv.getType() == CancelView.Type.GREEN){
				type = 5;
				gCount++;
			}
			else if(cv.getType() == CancelView.Type.PURPLE){
				type = 6;
				pCount++;
			}
			
			imgInfo.addColony(cv.getX(), cv.getY(), cv.getR(), type);
		}
		Log.d("test4", "cancelView red count = " + rCount);
		Log.d("test4", "cancelView green count = " + gCount);
		Log.d("test4", "cancelView purple count = " + pCount);
		
		bos = new ByteArrayOutputStream();
		mBitmapShow.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
		byte[] data_counted = bos.toByteArray();
		new SaveAndUploadImageAsyncTask(context, "系統訊息", "儲存中，請稍後...", this, SaveAndUploadImageAsyncTask.class, data_raw, data_counted, loadPrefStringData(USER_ACCOUNT), loadPrefStringData(USER_ID), imgInfo).execute();
	}
	
	
	// TODO: how to display after clicking "Done" and restore the highlightViews
	public void doDone(){
		if(state == State.ADD){
			for(int i = 0; i < image.mHighlightViews.size(); i++){
				HighlightView hv = image.mHighlightViews.get(i);
				float centerX = hv.getCenterX();
				float centerY = hv.getCenterY();
				float radius = hv.getRadius();
				
				addHighlightView2(centerX, centerY, radius);
			}
			
			image.mHighlightViews.clear();
			image.invalidate();
			image.zoomTo(1F, image.getWidth()/2, image.getHeight()/2, 300F);
			setTextInfo(image.rViews.size(), image.gViews.size(), image.myViews.size());
		} else if(state == State.SUB){
			zoomControls.setVisibility(View.INVISIBLE);
			image.removeHighlightView3();
			drawNormalImage();
			setTextInfo(image.rViews.size(), image.gViews.size(), image.myViews.size());
		}
		
		state = State.VIEW;
		setSaveDoneBtn();
		
		Log.d("test4", "red count = " + image.rViews.size());
		Log.d("test4", "green count = " + image.gViews.size());
		Log.d("test4", "purple count = " + image.myViews.size());
		int rCount = 0, gCount = 0, pCount = 0;
		for(int i = 0; i < image.cViews.size(); i++){
			if(image.cViews.get(i).getType() == CancelView.Type.RED)
				rCount++;
			if(image.cViews.get(i).getType() == CancelView.Type.GREEN)
				gCount++;
			if(image.cViews.get(i).getType() == CancelView.Type.PURPLE)
				pCount++;
		}
		Log.d("test4", "cancelView red count = " + rCount);
		Log.d("test4", "cancelView green count = " + gCount);
		Log.d("test4", "cancelView purple count = " + pCount);
	}
	
	
	@SuppressLint("NewApi")
	private Point getWindowSize(){
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	
    	return size;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.show_red_colony:
			isRedColonyShow = !isRedColonyShow;
			for(int i = 0; i < image.rViews.size(); i++){
				HighlightView4 rView = image.rViews.get(i);
				rView.setHidden(!rView.getHidden());
			}
			
			image.invalidate();
			break;
		case R.id.show_green_colony:
			isGreenColonyShow = !isGreenColonyShow;
			for(int i = 0; i < image.gViews.size(); i++){
				HighlightView5 gView = image.gViews.get(i);
				gView.setHidden(!gView.getHidden());
			}
			
			image.invalidate();
			break;
		case R.id.show_purple_colony:
			isPurpleColonyShow = !isPurpleColonyShow; 
			for(int i = 0; i < image.myViews.size(); i++){
				HighlightView2 myView = image.myViews.get(i);
				myView.setHidden(!myView.getHidden());
			}
			
			image.invalidate();
			break;
		}
	}
	
	
	public void setTextColor(){
		
	}
	
    
    public void setSaveDoneBtn(){
    	if(state == State.ADD || state == State.SUB){
//    		btn_save.setText("Done");
    	} else if(state == State.VIEW){
//    		btn_save.setText("Save");
    	}
    	setEnabledBtn();
    }
    
    public void setDisabledBtn(){
    	btn_circle_add.setEnabled(false);
    	btn_circle_sub.setEnabled(false);
    }
    
    public void setEnabledBtn(){
    	btn_circle_add.setEnabled(true);
    	btn_circle_sub.setEnabled(true);
    }
    
    public void addHighlightView(){
    	HighlightView hv = new HighlightView(image);
    	
    	int width = mBitmapShow.getWidth();
        int height = mBitmapShow.getHeight();
        Rect imageRect = new Rect(0, 0, width, height);
        
        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;
        int cropHeight = cropWidth;
        
        // TODO: our change
        // some config change
        int mAspectX = 1;
        int mAspectY = 1;
        boolean mCircleCrop = true;
        Matrix mImageMatrix = image.getImageMatrix();
        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }
        
        float x = (width - cropWidth) / 2;
        float y = (height - cropHeight) / 2;
        
        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
        hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
        
        // my code
        hv.setCenterX(x + cropWidth / 2);
        hv.setCenterY(y + cropHeight / 2);
        float radius = cropWidth / 2;
        hv.setRadius(radius);
        
        image.add(hv);
    }
    
    
    public void addHighlightView2(float centerX, float centerY, float radius){
    	HighlightView2 hv = new HighlightView2(image);
    	
    	int width = mBitmapShow.getWidth();
        int height = mBitmapShow.getHeight();
        Rect imageRect = new Rect(0, 0, width, height);
        
        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;
        int cropHeight = cropWidth;
        
        // TODO: our change
        // some config change
        int mAspectX = 1;
        int mAspectY = 1;
        boolean mCircleCrop = true;
        Matrix mImageMatrix = image.getImageMatrix();
        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }
        
        if(!isPurpleColonyShow)
        	hv.setHidden(true);
        
        RectF cropRect = new RectF(centerX-radius, centerY-radius, centerX+radius, centerY+radius);
        hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
        hv.setCenterX(centerX);
        hv.setCenterY(centerY);
        hv.setRadius(radius);
        
        image.add(hv);
    }
    
    // HighlightView3 for display show view
    public void addHighlightView3(float left, float top, float right, float bottom){
    	HighlightView3 hv3 = new HighlightView3(image);
    	
    	int width = mBitmapShow.getWidth();
        int height = mBitmapShow.getHeight();
        Rect imageRect = new Rect(0, 0, width, height);
        
        // make the default size about 4/5 of the width or height
//        int cropWidth = width;
//        int cropHeight = cropWidth;
        
        int mAspectX = 1;
        int mAspectY = 1;
        boolean mCircleCrop = false;
        Matrix mImageMatrix = image.getImageMatrix();
//        if (mAspectX != 0 && mAspectY != 0) {
//            if (mAspectX > mAspectY) {
//                cropHeight = cropWidth * mAspectY / mAspectX;
//            } else {
//                cropWidth = cropHeight * mAspectX / mAspectY;
//            }
//        }
        
//        float centerX = (width - cropWidth) / 2;
//        float y = (height - cropHeight) / 2;
        
        RectF cropRect = new RectF(left, top, right, bottom);
        hv3.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
        
        // my code
        hv3.setCenterX((left+right)/2);
        hv3.setCenterY((top+bottom)/2);
        
        image.add(hv3);
//        image.invalidate();
//        image.showView.setFocus(true);
    }
    
    public void addHighlightView4(float centerX, float centerY, float radius){
    	HighlightView4 hv = new HighlightView4(image);
    	
    	int width = mBitmapShow.getWidth();
        int height = mBitmapShow.getHeight();
        Rect imageRect = new Rect(0, 0, width, height);
        
        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;
        int cropHeight = cropWidth;
        
        // TODO: our change
        // some config change
        int mAspectX = 1;
        int mAspectY = 1;
        boolean mCircleCrop = true;
        Matrix mImageMatrix = image.getImageMatrix();
        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }
        
        RectF cropRect = new RectF(centerX-radius, centerY-radius, centerX+radius, centerY+radius);
        hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
        hv.setCenterX(centerX);
        hv.setCenterY(centerY);
        hv.setRadius(radius);
        
        image.add(hv);
    }
    
    public void addHighlightView5(float centerX, float centerY, float radius){
    	HighlightView5 hv = new HighlightView5(image);
    	
    	int width = mBitmapShow.getWidth();
        int height = mBitmapShow.getHeight();
        Rect imageRect = new Rect(0, 0, width, height);
        
        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;
        int cropHeight = cropWidth;
        
        // TODO: our change
        // some config change
        int mAspectX = 1;
        int mAspectY = 1;
        boolean mCircleCrop = true;
        Matrix mImageMatrix = image.getImageMatrix();
        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }
        
        RectF cropRect = new RectF(centerX-radius, centerY-radius, centerX+radius, centerY+radius);
        hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
        hv.setCenterX(centerX);
        hv.setCenterY(centerY);
        hv.setRadius(radius);
        
        image.add(hv);
    }

	public State getState() {
		return state;
	}
	
	public void drawHighlightImage(){
		if(mBitmapShowSub != null)
			mBitmapShowSub.recycle();
		
		mBitmapShowSub = mBitmap.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mBitmapShowSub);
		canvas.save();
		
		Path path = new Path();
		for(int i = 0; i < image.myViews.size(); i++){
			HighlightView2 hv2 = image.myViews.get(i);
			float centerX = hv2.getCenterX();
			float centerY = hv2.getCenterY();
			float radius = hv2.getRadius();
			path.addCircle(centerX, centerY, radius, Path.Direction.CW);
		}
		Paint outlinePaint = new Paint();
		outlinePaint.setStrokeWidth(2F);
		outlinePaint.setStyle(Paint.Style.STROKE);
		outlinePaint.setAntiAlias(true);
		outlinePaint.setColor(0xFFEF04D6);
		
		Paint mFocusPaint = new Paint();
		mFocusPaint.setARGB(125, 50, 50, 50);
		
		Rect viewDrawingRect = new Rect(0, 0, mBitmapShow.getWidth(), mBitmapShow.getHeight());
		canvas.clipPath(path, Region.Op.DIFFERENCE);
        
		
		path.reset();
        outlinePaint.setColor(Color.RED);
//        canvas.save();
		for(int i = 0; i < image.rViews.size(); i++){
			HighlightView4 hv4 = image.rViews.get(i);
			float centerX = hv4.getCenterX();
			float centerY = hv4.getCenterY();
			float radius = hv4.getRadius();
			path.addCircle(centerX, centerY, radius, Path.Direction.CW);
		}
		canvas.clipPath(path, Region.Op.DIFFERENCE);
//        canvas.drawRect(viewDrawingRect, mFocusPaint);
//        canvas.restore();
//        canvas.drawPath(path, outlinePaint);
		
        
        path.reset();
        outlinePaint.setColor(Color.GREEN);
//        canvas.save();
		for(int i = 0; i < image.gViews.size(); i++){
			HighlightView5 hv5 = image.gViews.get(i);
			float centerX = hv5.getCenterX();
			float centerY = hv5.getCenterY();
			float radius = hv5.getRadius();
			path.addCircle(centerX, centerY, radius, Path.Direction.CW);
		}
		canvas.clipPath(path, Region.Op.DIFFERENCE);
//        canvas.drawRect(viewDrawingRect, mFocusPaint);
//        canvas.restore();
		canvas.drawRect(viewDrawingRect, mFocusPaint);
        canvas.restore();
//        canvas.drawPath(path, outlinePaint);
		
		
        mBitmapShow = mBitmapShowSub.copy(Config.ARGB_8888, true);
//        image.setImageBitmapResetBase(mBitmapShow, true);
        image.setImageBitmap(mBitmapShow);
		image.invalidate();
	}
	
	public void drawNormalImage(){
		image.zoomTo(1F, image.getWidth()/2, image.getHeight()/2, 300F);
		
		if(mBitmapShowSub != null)
			mBitmapShowSub.recycle();
		
		mBitmapShowSub = mBitmap.copy(Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mBitmapShowSub);
		canvas.save();
		
		Path path = new Path();
		for(int i = 0; i < image.myViews.size(); i++){
			HighlightView2 hv2 = image.myViews.get(i);
			float centerX = hv2.getCenterX();
			float centerY = hv2.getCenterY();
			float radius = hv2.getRadius();
			path.addCircle(centerX, centerY, radius, Path.Direction.CW);
		}

		Paint outlinePaint = new Paint();
		outlinePaint.setStrokeWidth(2F);
		outlinePaint.setStyle(Paint.Style.STROKE);
		outlinePaint.setAntiAlias(true);
		outlinePaint.setColor(0xFFEF04D6);
		
        canvas.restore();
        canvas.drawPath(path, outlinePaint);
		
        mBitmapShow = mBitmapShowSub.copy(Config.ARGB_8888, true);
        image.setImageBitmapResetBase(mBitmapShow, true);
		image.invalidate();
	}


	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		// TODO Auto-generated method stub
		if(taskName.equals("SaveAndUploadImageAsyncTask") && result.getValue("result").equals("success")){
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(this, "上傳圖片失敗，請重試一次", Toast.LENGTH_SHORT).show();
		}
	}
}