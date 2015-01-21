package com.colonycount.cklab.activity;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colonycount.cklab.activity.R.id;
import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.SaveImgAsyncTask;
import com.colonycount.cklab.base.GPlusClientActivity;
import com.colonycount.cklab.calendarpicker.CalendarPickerView;
import com.colonycount.cklab.config.Config;
import com.colonycount.cklab.croptest.HighlightView;
import com.colonycount.cklab.croptest.PhotoView2;
import com.colonycount.cklab.croptest.PhotoViewAttacher2;
import com.colonycount.cklab.croptest.PhotoViewAttacher2.OnMatrixChangedListener;
import com.colonycount.cklab.croptest.PhotoViewAttacher2.OnPhotoTapListener;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.model.ImgInfo;
import com.colonycount.cklab.rangebar.CustomSeekBar;

public class Test2 extends GPlusClientActivity implements View.OnClickListener, CustomSeekBar.OnSeekBarChangeListener, AsyncTaskCompleteListener<Boolean> {
	static final String PHOTO_TAP_TOAST_STRING = "(x, y): (%d, %d)";
    static final int DEFAULT_COLONY_RADIUS = 20;
    
	private Context context;
	
	private AsyncTaskCompleteListener<Boolean> asyncTaskListener;
	
	private PhotoViewAttacher2 mAttacher;
	
	private ImgInfo imgInfo;
	
	private Bitmap mBitmap;
	private Bitmap mCopyBitmap;
	private PhotoView2 image;
	
	private TextView text_result;
	private TextView text_action_msg;
	private TextView showRedColony;
    private TextView showGreenColony;
    private TextView showPurpleColony;
    private boolean isRedColonyShow = true;
    private boolean isGreenColonyShow = false;
    private boolean isPurpleColonyShow = true;
    private ImageButton btn_close;
	private ImageButton btn_set_tag;
	private ImageButton btn_circle_add;
	private ImageButton btn_circle_sub;
	private ImageButton btn_save_or_ok;
	private LinearLayout action_bot_bar;
	private RelativeLayout msg_bot_bar;
	private RelativeLayout text_info;
	private RelativeLayout text_msg;
	private Dialog dialogSetTag;
	
	// for colony type list
	private LinearLayout container_add_colony_type;
	private LinearLayout layout_colony_type_list;
	private List<AppendListItem> colonyTypeListItems = new ArrayList<AppendListItem>();
	private List<ShowListItem> colonyTypeList = new ArrayList<ShowListItem>();
	
	// for colony exp params
	private LinearLayout container_add_experiment_param;
	private LinearLayout layout_colony_exp_param_list;
	private List<AppendListItem> colonyExpParamListItems = new ArrayList<AppendListItem>();
	private List<ShowListItem> colonyExpParamList = new ArrayList<ShowListItem>();
	
	
	private Button btn_set_tag_date;
	
	public State state = State.VIEW;
	
	public enum State {
    	VIEW, ADD, SUB, SET_TAG
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setFullScreen();
		setContentView(R.layout.test2);
		setViews();
		setListeners();
//		setColony();
		
		String filename = getIntent().getStringExtra("image");
		try {
		    FileInputStream is = this.openFileInput(filename);
		    mBitmap = BitmapFactory.decodeStream(is);
		    mCopyBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
		    is.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
//		byte[] data = getIntent().getByteArrayExtra("pictureData");
//		int rotation = getIntent().getIntExtra("pictureRotation", -1);
//		mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		image.setImageBitmap(mCopyBitmap);
		
		// The MAGIC happens here!
        mAttacher = new PhotoViewAttacher2(image);
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());
        mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
        
        // my code
        mAttacher.setOnDragCallback(image);
        mAttacher.setOnScaleCallback(image);
	}
	
	private class MatrixChangeListener implements OnMatrixChangedListener {
        @Override
        public void onMatrixChanged(RectF rect) {
//        	float left = rect.left;
//        	float top = rect.top;
//        	float right = rect.right;
//        	float bottom = rect.bottom;
//        	Log.d("test4", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
        }
    }
	
	public void setViews(){
		context 		 = this;
		asyncTaskListener = this;
		image 			 = (PhotoView2) 	findViewById(R.id.cropimageview);
		showRedColony    = (TextView) 		findViewById(R.id.show_red_colony);
		showGreenColony  = (TextView) 		findViewById(R.id.show_green_colony);
		showPurpleColony = (TextView) 		findViewById(R.id.show_purple_colony);
		btn_close        = (ImageButton)    findViewById(R.id.btn_close);
		btn_save_or_ok   = (ImageButton)    findViewById(R.id.btn_save_or_ok);
		btn_circle_add   = (ImageButton)    findViewById(R.id.btn_add);
		btn_circle_sub   = (ImageButton)    findViewById(R.id.btn_sub);
		btn_set_tag      = (ImageButton)    findViewById(R.id.btn_edit);
		action_bot_bar   = (LinearLayout)   findViewById(R.id.action_bot_bar);
		msg_bot_bar      = (RelativeLayout) findViewById(R.id.msg_bot_bar);
		text_info 		 = (RelativeLayout) findViewById(R.id.text_info);
		text_msg 		 = (RelativeLayout) findViewById(R.id.text_msg);
		text_action_msg  = (TextView) 		findViewById(R.id.text_action_msg);
		text_result 	 = (TextView) 		findViewById(R.id.text_result);
		
		imgInfo = new ImgInfo();
	}
	
	private void setListeners(){
		btn_close.setOnClickListener(this);
		btn_save_or_ok.setOnClickListener(this);
		btn_circle_add.setOnClickListener(this);
		btn_circle_sub.setOnClickListener(this);
		btn_set_tag.setOnClickListener(this);
	}
	
	public void checkSingleChoice(View view, LinearLayout showLayout, List<ShowListItem> showList){
		if(view != null){
			int index = showLayout.indexOfChild(view);
			
			for(int i = 0; i < showList.size(); i++){
				if(i == index){
					continue;
				}
				
				showList.get(i).cbx.setChecked(false);
			}
		}
	}
	
	public void removeShowListItem(LinearLayout showLayout, List<ShowListItem> showList, View view, String type, String value){
		int index = showLayout.indexOfChild(view);
		showLayout.removeView(view);
		showList.remove(index);
		removePrefStringSetData(type, value);
	}
	
	public void setImgColonyType(String colonyType){
		imgInfo.setType(colonyType);
	}
	
	public void setImgExpParam(String expParam){
		imgInfo.setExpParam(expParam);
	}
	
	public void setImgExpDate(int year, int month, int day){
		imgInfo.setDate(year, month, day);
	}
	
	public void setImgExpDate(Calendar c){
		imgInfo.setDate(c);
	}
	
	public void setImgDilution(int number){
		imgInfo.setDilutionNumber(number);
	}
	
	private class PhotoTapListener implements OnPhotoTapListener {
        @Override
        public void onPhotoTap(View view, float xPerc, float yPerc, float x, float y) {
        	int xx = (int)(xPerc * mBitmap.getWidth());
        	int yy = (int)(yPerc * mBitmap.getHeight());
        	int color = mBitmap.getPixel(xx, yy);
        	
        	int centerX = Config.OUTPUT_IMAGE_WIDTH / 2;
        	int centerY = Config.OUTPUT_IMAGE_HEIGHT / 2;
        	int distance = (int) Math.sqrt(Math.pow(xx - centerX, 2) + Math.pow(yy - centerY, 2));
        	if(distance <= Config.OUTPUT_IMAGE_WIDTH / 2){
        		
        		if(state == State.ADD){
//        			Pixel hitPixel = new Pixel(xx, yy, Color.red(color), Color.green(color), Color.blue(color));
//            		AsyncTaskPayload payload = new AsyncTaskPayload();
//            		payload.setRawImg(mBitmap);
//            		payload.setHitPixel(hitPixel);
//            		new AddColonyAsyncTask(context, "", "新增菌落，請稍後...", asyncTaskListener, AddColonyAsyncTask.class).execute(payload);
        			
        			addColonyTempView(xx, yy, DEFAULT_COLONY_RADIUS);
            	} else if(state == State.SUB){
            		List<HighlightView> colonyList = image.getColonyList();
            		for(int i = 0; i < colonyList.size(); i++){
            			HighlightView hv = colonyList.get(i);
            			if(hv.getHit(x, y) != HighlightView.GROW_NONE){
            				addRemoveColonyTempView(hv);
            				break;
            			}
            		}
            	}
        	}
        }
    }
	
	public void addColonyTempView(int x, int y, int r){
		HighlightView hv = getHighlightView(x, y, r);
        image.addColonyTempView(hv);
	}
	
	public void addRemoveColonyTempView(HighlightView hv){
		image.addRemoveColonyTempView(hv);
	}
	
	public void cancelAddColonyView(){
		image.cancelAddColonyView();
	}
	
	public void cancelRemoveColonyView(){
		image.cancelRemoveColonyView();
	}
	
	public void addColonyView(){
		image.addColonyView();
	}
	
	public void removeColonyView(){
		image.removeColonyView();
	}
	
	public HighlightView getHighlightView(int x, int y, int r){
		HighlightView hv = new HighlightView(image);
		
		int width = mCopyBitmap.getWidth();
        int height = mCopyBitmap.getHeight();
        
        Rect imageRect = new Rect(0, 0, width, height);
        boolean mCircleCrop = true;
        Matrix mImageMatrix = image.getImageMatrix();
        
        RectF colonyRect = new RectF(x-r, y-r, x+r, y+r);
        hv.setup(mImageMatrix, imageRect, colonyRect, mCircleCrop, true);
        
        return hv;
	}

	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_close:
			if(state == State.ADD || state == State.SUB){
				if(state == State.ADD){
					cancelAddColonyView();
				} else {
					cancelRemoveColonyView();
				}
				
				state = State.VIEW;
				updateUI();
			} else if(state == State.VIEW){
				finish();
			}
			break;
		case R.id.btn_save_or_ok:
			if(state == State.ADD || state == State.SUB){
				if(state == State.ADD){
					addColonyView();
				} else {
					removeColonyView();
				}
				
				state = State.VIEW;
				updateUI();
			} else if(state == State.VIEW){
				saveImg();
			}
			break;
		case R.id.btn_add:
			state = State.ADD;
			updateUI();
			break;
		case R.id.btn_sub:
			state = State.SUB;
			updateUI();
			break;
		case R.id.btn_edit:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View dialogContent = getLayoutInflater().inflate(R.layout.dialog_add_tag_view, null);
			builder.setView(dialogContent);
			builder.setCancelable(false);
			
//		    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//		    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//		    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		    
		    dialogSetTag = builder.create();
		    setDialogDetails(dialogContent);
		    dialogSetTag.show();
//		    dialogSetTag.getWindow().setAttributes(lp);
			break;
		case R.id.btn_set_tag_ok:
			Log.d("test4", "date = " + imgInfo.getDateString());
			Log.d("test4", "type = " + imgInfo.getType());
			Log.d("test4", "dilution = " + imgInfo.getDilutionNumber());
			Log.d("test4", "exp param = " + imgInfo.getExpParam());
			cleanList();
			dialogSetTag.dismiss();
			break;
		case R.id.btn_set_tag_date:
			final CalendarPickerView dialogView = (CalendarPickerView) getLayoutInflater().inflate(R.layout.calendarpicker_dialog, null, false);
			final Calendar next1Month = Calendar.getInstance();
			next1Month.add(Calendar.MONTH, 1);
		    final Calendar last3Month = Calendar.getInstance();
		    last3Month.add(Calendar.MONTH, -3);
			
	        dialogView.init(last3Month.getTime(), next1Month.getTime()).withSelectedDate(imgInfo.getDate());
	        AlertDialog theDialog = new AlertDialog.Builder(this)
	        	.setTitle("設定實驗日期")
	            .setView(dialogView)
	            .setNeutralButton("確定", new DialogInterface.OnClickListener() {
	                @Override 
	                public void onClick(DialogInterface dialogInterface, int i) {
	                    Calendar cal = Calendar.getInstance();
	                    cal.setTime(dialogView.getSelectedDate());
	                    
//	                    int year = cal.get(Calendar.YEAR);
//	                    int month = cal.get(Calendar.MONTH);
//	                    int day = cal.get(Calendar.DAY_OF_MONTH);
	                    
	                    setImgExpDate(cal);
	                    setDisplayExpDay();
	                    dialogInterface.dismiss();
	                }
	            }).create();
	        theDialog.setOnShowListener(new DialogInterface.OnShowListener() {
	            @Override
	            public void onShow(DialogInterface dialogInterface) {
//	                Log.d("test4", "onShow: fix the dimens!");
	                dialogView.fixDialogDimens();
	            }
	        });
	        theDialog.show();
			break;
		}
	}
	
	public void cleanList(){
		colonyTypeListItems.clear();
		colonyTypeList.clear();
		colonyExpParamListItems.clear();
		colonyExpParamList.clear();
	}
	
	private void setDialogDetails(View dialogContent) {
		container_add_colony_type = (LinearLayout) dialogContent.findViewById(R.id.container_add_colony_type);
		container_add_experiment_param = (LinearLayout) dialogContent.findViewById(R.id.container_add_experiment_param);
		layout_colony_type_list   = (LinearLayout) dialogContent.findViewById(R.id.colony_type_list);
		layout_colony_exp_param_list = (LinearLayout) dialogContent.findViewById(R.id.colony_exp_param_list);
		CustomSeekBar seekbar = (CustomSeekBar) dialogContent.findViewById(R.id.seek_bar);
	    TextView seekbarValue1 = (TextView) dialogContent.findViewById(R.id.seekbar_value_1);
	    TextView seekbarValue2 = (TextView) dialogContent.findViewById(R.id.seekbar_value_2);
	    TextView seekbarValue3 = (TextView) dialogContent.findViewById(R.id.seekbar_value_3);
	    TextView seekbarValue4 = (TextView) dialogContent.findViewById(R.id.seekbar_value_4);
	    TextView seekbarValue5 = (TextView) dialogContent.findViewById(R.id.seekbar_value_5);
	    TextView seekbarValue6 = (TextView) dialogContent.findViewById(R.id.seekbar_value_6);
	    TextView seekbarValue7 = (TextView) dialogContent.findViewById(R.id.seekbar_value_7);
	    TextView seekbarValue8 = (TextView) dialogContent.findViewById(R.id.seekbar_value_8);
	    TextView seekbarValue9 = (TextView) dialogContent.findViewById(R.id.seekbar_value_9);
	    TextView seekbarValue10 = (TextView) dialogContent.findViewById(R.id.seekbar_value_10);
	    seekbarValue1.setText(Html.fromHtml("10<sup><small>-1</small></sup>"));
	    seekbarValue2.setText(Html.fromHtml("10<sup><small>-2</small></sup>"));
	    seekbarValue3.setText(Html.fromHtml("10<sup><small>-3</small></sup>"));
	    seekbarValue4.setText(Html.fromHtml("10<sup><small>-4</small></sup>"));
	    seekbarValue5.setText(Html.fromHtml("10<sup><small>-5</small></sup>"));
	    seekbarValue6.setText(Html.fromHtml("10<sup><small>-6</small></sup>"));
	    seekbarValue7.setText(Html.fromHtml("10<sup><small>-7</small></sup>"));
	    seekbarValue8.setText(Html.fromHtml("10<sup><small>-8</small></sup>"));
	    seekbarValue9.setText(Html.fromHtml("10<sup><small>-9</small></sup>"));
	    seekbarValue10.setText(Html.fromHtml("10<sup><small>-10</small></sup>"));
	    Button btn_set_tag_ok = (Button) dialogContent.findViewById(R.id.btn_set_tag_ok);
	    btn_set_tag_date = (Button) dialogContent.findViewById(id.btn_set_tag_date);
	    
	    seekbar.setOnRangeBarChangeListener(this);
	    btn_set_tag_ok.setOnClickListener(this);
	    btn_set_tag_date.setOnClickListener(this);
	    
	    // get saved value from image info
	    int dilutionNumber = imgInfo.getDilutionNumber();
	    String savedColonyType = imgInfo.getType();
	    String savedColonyExpParam = imgInfo.getExpParam();
	    
	    // set value to views
	    seekbar.setThumbIndex(-1 * dilutionNumber - 1);
	    setDisplayExpDay();
	    
	    // set type
	    Set<String> colonyTypeStringList = loadPrefStringSetData(COLONY_TYPE_LIST);
	    addSavedListItem(COLONY_TYPE, colonyTypeStringList, savedColonyType, layout_colony_type_list, colonyTypeList);
	    addListItem(new AppendListItem(getLayoutInflater(), COLONY_TYPE), container_add_colony_type, colonyTypeListItems);
	    
	    // set exp params
	    Set<String> colonyExpParamStringList = loadPrefStringSetData(COLONY_EXP_PARAM_LIST);
	    addSavedListItem(COLONY_EXP_PARAM, colonyExpParamStringList, savedColonyExpParam, layout_colony_exp_param_list, colonyExpParamList);
	    addListItem(new AppendListItem(getLayoutInflater(), COLONY_EXP_PARAM), container_add_experiment_param, colonyExpParamListItems);
	}
	
	public void addSavedListItem(String type, Set<String> stringSet, String cmpString, LinearLayout showLayout, List<ShowListItem> showList){
		if(stringSet != null){
		    for(String s : stringSet){
		    	View view = getLayoutInflater().inflate(R.layout.dialog_add_tag_listview_item, null);
		    	ShowListItem item = new ShowListItem(s, view, type);
		    	
		    	Log.d("test4", "s = " + s);
		    	if(cmpString.equals(s))
		    		item.cbx.setChecked(true);
		    	
		    	showLayout.addView(view);
		    	showList.add(item);
		    }
	    }
	}
	
	public void setDisplayExpDay(){
		btn_set_tag_date.setText(imgInfo.getDateString());		
	}
	
	public void addListItem(AppendListItem item, LinearLayout layout, List<AppendListItem> list){
		layout.addView(item.getView());
		list.add(item);
	}
	
	public void removeListItem(LinearLayout layout, List<AppendListItem> list, LinearLayout showLayout, List<ShowListItem> showList, ShowListItem showListItem, View showView){
		if(showView != null){
			list.remove(0);
			layout.removeViewAt(0);
			
	    	showLayout.addView(showView);
	    	showList.add(showListItem);
		} else {
			list.remove(list.size()-1);
			layout.removeViewAt(list.size()-1);
		}
	}
	
	public void updateUI(){
		setTitleBar();
		setBotBar();
		setTextMsg();
		setImageState();
		image.invalidate();
	}
	
	public void setImageState() {
		image.setState(state);
		mAttacher.setState(state);
	}

	public void saveImg(){
		// raw image
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mBitmap.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
		byte[] data_raw = bos.toByteArray();
		
		// counted image
		// TODO: draw counted image
		bos = new ByteArrayOutputStream();
		mBitmap.compress(CompressFormat.JPEG, 100, bos);//100 is the best quality possibe
		byte[] data_counted = bos.toByteArray();
		
		new SaveImgAsyncTask(context, "系統訊息", "儲存中，請稍後...", this, SaveImgAsyncTask.class, true, data_raw, data_counted, loadPrefStringData(USER_ACCOUNT), loadPrefStringData(USER_ID), imgInfo).execute();
	}

	private void setTitleBar() {
		if(state == State.ADD || state == State.SUB){
			if(state == State.ADD){
				text_result.setText("新增菌落");
			} else if(state == State.SUB){
				text_result.setText("刪除菌落");
			}
			
			btn_save_or_ok.setImageResource(R.drawable.btn_check);
		} else if(state == State.VIEW){
//			text_result.setText("菌落總數:" + image.rViews.size() + "個");
			text_result.setText("菌落總數:  個");
			btn_save_or_ok.setImageResource(R.drawable.btn_save2);
		}
	}
	
	private void setBotBar() {
		if(state == State.ADD || state == State.SUB){
			action_bot_bar.setVisibility(View.INVISIBLE);
			msg_bot_bar.setVisibility(View.VISIBLE);
		} else if(state == State.VIEW){
			msg_bot_bar.setVisibility(View.INVISIBLE);
			action_bot_bar.setVisibility(View.VISIBLE);
		}
	}
	
	private void setTextMsg() {
		if(state == State.ADD || state == State.SUB){
			text_info.setVisibility(View.INVISIBLE);
			text_msg.setVisibility(View.INVISIBLE);
			
			if(state == State.ADD)
				text_action_msg.setText("點擊螢幕上未標示到的菌落來新增菌落");
			else
				text_action_msg.setText("點擊螢幕上標示錯誤的菌落來刪除菌落");
		} else if(state == State.VIEW){
			text_info.setVisibility(View.VISIBLE);
			text_msg.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onIndexChangeListener(CustomSeekBar seekBar, int index) {
		setImgDilution(-1 * (index+1));
	}

	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		if(taskName.equals("AddColonyAsyncTask")){
			Component component = result.getComponent();
//			int area = component.getArea();
//			int centerX = component.getCenterX();
//			int centerY = component.getCenterY();
//			int r = (int) component.getRadius();
//			Log.d("test4", "area = " + area + ", centerX = " + centerX + ", centerY = " + centerY + ", r = " + r);
//			
//			Canvas canvas = new Canvas(mCopyBitmap);
//			Paint paint = new Paint();
//			paint.setColor(Color.RED);
//			paint.setStyle(Style.STROKE);
//			paint.setAntiAlias(true);
//			canvas.drawCircle(centerX, centerY, r, paint);
			
//			image.addColonyView(new RedColonyView(component.getCenterX(), component.getCenterY(), (int) component.getRadius()));
//			image.addColonyTempView(new HighlightView(ctx));
//			image.invalidate();
		} else if(taskName.equals("SaveImgAsyncTask")){
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
			finish();
		}
	}

	
	/**
	 * 
	 * @author ming
	 * colony type list item to accept user's input options and append new list to enter another option
	 */
	class AppendListItem {
		private String type;
		private EditText et;
		private boolean hasAddedList = false;
		private AppendEditTextWatcher textWatcher;
		private EditTextFocusChangeListener focusChangeListener;
		
		public AppendListItem(LayoutInflater layoutInflater, String type){
			this.type = type;
			
			// add edittext
			et = (EditText) layoutInflater.inflate(R.layout.edittext_add_colony_type, null);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, 10);
			et.setLayoutParams(params);
			
			// add textwatcher
			textWatcher = new AppendEditTextWatcher();
			et.addTextChangedListener(textWatcher);
			
			// add focus change listener
			focusChangeListener = new EditTextFocusChangeListener();
			et.setOnFocusChangeListener(focusChangeListener);
		}
		
		class AppendEditTextWatcher implements TextWatcher {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if(!s.toString().equals("") && !hasAddedList){
					if(type.equals(COLONY_TYPE))
						addListItem(new AppendListItem(getLayoutInflater(), COLONY_TYPE), container_add_colony_type, colonyTypeListItems);
					else if(type.equals(COLONY_EXP_PARAM))
						addListItem(new AppendListItem(getLayoutInflater(), COLONY_EXP_PARAM), container_add_experiment_param, colonyExpParamListItems);
					
					hasAddedList = true;
				} else if(s.toString().equals("") && hasAddedList){
					if(type.equals(COLONY_TYPE))
						removeListItem(container_add_colony_type, colonyTypeListItems, null, null, null, null);
					else if(type.equals(COLONY_EXP_PARAM))
						removeListItem(container_add_experiment_param, colonyExpParamListItems, null, null, null, null);
					
					hasAddedList = false;
				}
			}
		}
		
		class EditTextFocusChangeListener implements View.OnFocusChangeListener {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus && !et.getText().toString().equals("")) {
					String value = et.getText().toString();
					View view = getLayoutInflater().inflate(R.layout.dialog_add_tag_listview_item, null);
					ShowListItem showListItem = new ShowListItem(value, view, type);
					
					savePrefStringSetData(type, value);
					if(type.equals(COLONY_TYPE))
						removeListItem(container_add_colony_type, colonyTypeListItems, layout_colony_type_list, colonyTypeList, showListItem, view);
					else if(type.equals(COLONY_EXP_PARAM))
						removeListItem(container_add_experiment_param, colonyExpParamListItems, layout_colony_exp_param_list, colonyExpParamList, showListItem, view);
					
			    }
			}
		}
		
		public EditText getView(){
			return et;
		}
	}
	
	/**
	 * 
	 * @author ming
	 *
	 */
	class ShowListItem {
		private View view;
		private String type;
		private String value;
		private CheckBox cbx;
		private TextView tv;
		private Button delete;
		
		public ShowListItem(String value, View view, String type){
			cbx = (CheckBox) view.findViewById(R.id.checkBox1);
			tv = (TextView) view.findViewById(R.id.textView1);
			delete = (Button) view.findViewById(R.id.button1);
			tv.setText(value);
			this.view = view;
			this.value = value;
			this.type = type;
			
			cbx.setOnCheckedChangeListener(new CheckedChangeListener());
			delete.setOnClickListener(new BtnClickListener());
		}
		
		class CheckedChangeListener implements OnCheckedChangeListener {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					if(type.equals(COLONY_TYPE)){
						checkSingleChoice(view, layout_colony_type_list, colonyTypeList);
						setImgColonyType(tv.getText().toString());
					} else if(type.equals(COLONY_EXP_PARAM)){
						checkSingleChoice(view, layout_colony_exp_param_list, colonyExpParamList);
						setImgExpParam(tv.getText().toString());
					}
				} else {
					if(type.equals(COLONY_TYPE))
						setImgColonyType("");
					else if(type.equals(COLONY_EXP_PARAM))
						setImgExpParam("");
				}
			}
		}
		
		class BtnClickListener implements View.OnClickListener {
			@Override
			public void onClick(View v) {
				if(type.equals(COLONY_TYPE))
					removeShowListItem(layout_colony_type_list, colonyTypeList, view, type, value);
				else if(type.equals(COLONY_EXP_PARAM)){
					removeShowListItem(layout_colony_exp_param_list, colonyExpParamList, view, type, value);
				}
			}
		}
	}
}