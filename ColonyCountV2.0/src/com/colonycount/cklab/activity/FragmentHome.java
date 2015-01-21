package com.colonycount.cklab.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.GetImgAsyncTask;
import com.colonycount.cklab.calendarpicker.CalendarPickerView;
import com.colonycount.cklab.calendarpicker.CalendarPickerView.SelectionMode;
import com.colonycount.cklab.crop.MediaStoreUtils;
import com.colonycount.cklab.croptest.PhotoView2;
import com.colonycount.cklab.croptest.PhotoViewAttacher2;
import com.colonycount.cklab.model.ImgSearchFilter;
import com.colonycount.cklab.rangebar.RangeBar;
import com.colonycount.cklab.utils.CustomGrid;

public class FragmentHome extends Fragment implements View.OnClickListener {
	public static final int TAKE_PHOTO = 0;
	public static final int SELECT_PHOTO = 1;
	
	private static int REQUEST_PICTURE = 1;
	private static int REQUEST_CROP_PICTURE = 2;
	
	public static int ACTION_BAR_GROUP = 0;
	public static int MENU_SEARCH = 0;
	
	protected static final String COLONY_TYPE_LIST = "colony_type_list";
	protected static final String COLONY_EXP_PARAM_LIST = "colony_exp_param_list";
	protected static final String COLONY_TYPE = "colony_type";
	protected static final String COLONY_EXP_PARAM = "colony_exp_param";
	
	private ImageButton btn_take_photo;
    private ImageButton btn_select_photo;
    private GridView gridView;
    private TextView homeMsg;
    
    private String user_id;
    private String[] urls;
 
    // gridview data
    private String[] date;
	private String[] type;
	private int[] dilution_num;
	private String[] exp_param;
	private int[] colony_num;
	private Bitmap[] images;
    
    private ImgSearchFilter searchFilter;
    
    private static boolean isMenuVisible;
    
    private HomeActivity context;
    
    private LinearLayout layout_colony_type_list;
    private List<ListItem> colony_type_list;
    private LinearLayout layout_colony_exp_param_list;
    private List<ListItem> colony_exp_param_list;
    
    public FragmentHome(String user_id, HomeActivity context){
    	this.user_id = user_id;
    	this.context = context;
    	isMenuVisible = true;
    }
    
    public FragmentHome(){
    	super();
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	private void setViews(View view){
		btn_take_photo   = (ImageButton) view.findViewById(R.id.btn_take_photo);
		btn_select_photo = (ImageButton) view.findViewById(R.id.btn_select_photo);
		gridView = (GridView) view.findViewById(R.id.gridView1);
		homeMsg = (TextView) view.findViewById(R.id.home_msg);
		
		colony_type_list = new ArrayList<ListItem>();
		colony_exp_param_list = new ArrayList<ListItem>();
		searchFilter = new ImgSearchFilter();
	}
	
	private void setListeners(){
		btn_take_photo.setOnClickListener(this);
		btn_select_photo.setOnClickListener(this);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fragment_home, container, false);
		setViews(view);
		setListeners();
		
		return view;
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.btn_take_photo:
			if(hasCameraHardware(getActivity())){
				Intent takePictureIntent = new Intent(getActivity(), TakePhotoActivity.class);
				takePictureIntent.putExtra("requestCode", TAKE_PHOTO);
				startActivity(takePictureIntent);
			} else {
				Toast.makeText(getActivity(), "don't have camera!", Toast.LENGTH_SHORT);
			}
			break;
		case R.id.btn_select_photo:
			startActivityForResult(MediaStoreUtils.getPickImageIntent(getActivity()), REQUEST_PICTURE);
			break;
		}
	}
	
	/** Check if this device has a camera */
	private boolean hasCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
//		File croppedImageFile = new File(getActivity().getFilesDir(), "test.jpg");
        if ((requestCode == REQUEST_PICTURE) && (resultCode == Activity.RESULT_OK)) {
//        	Uri croppedImage = Uri.fromFile(croppedImageFile);
//        	
//        	CropImageIntentBuilder cropImage = new CropImageIntentBuilder(480, 480, croppedImage);
//            cropImage.setSourceImage(intent.getData());
//            cropImage.setCircleCrop(true);
//            cropImage.setDoFaceDetection(false);
//            
//            startActivity(cropImage.getIntent(getActivity()));
        	
//        	Uri croppedImage = Uri.fromFile(croppedImageFile);
        	intent.setClass(getActivity(), Test.class);
        	startActivity(intent);
        }
	}
	
	public void setHomeMsg(String msg){
		homeMsg.setText(msg);
	}
	
	public void setGridView(){
		final Context context = getActivity();
		CustomGrid adapter = new CustomGrid(context, date, type, dilution_num, exp_param, colony_num, images);
		
		// TODO: nullpointer exception
		if(homeMsg != null)
			homeMsg.setVisibility(View.GONE);
		
		if(gridView != null){
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(new GridView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					View dialogContent = getActivity().getLayoutInflater().inflate(R.layout.dialog_gridview, null);
					builder.setView(dialogContent);
					builder.setCancelable(false);
					
//					WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//				    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//				    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//				    lp.horizontalMargin = 0.0f;
//				    lp.verticalMargin = 0.0f;
				    
				    final Dialog d = builder.create();
				    d.show();
//				    d.getWindow().setAttributes(lp);
				    
				    Button dialogBtnClose = (Button) dialogContent.findViewById(R.id.btn_close);
				    PhotoView2 image = (PhotoView2) dialogContent.findViewById(R.id.cropimageview);
				    image.setImageBitmap(images[0]);
				    PhotoViewAttacher2 mAttacher = new PhotoViewAttacher2(image);
				    // my code
			        mAttacher.setOnDragCallback(image);
			        mAttacher.setOnScaleCallback(image);
				    
					dialogBtnClose.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							d.dismiss();
						}
					});
				}
			});
		}
		
	}
	
	public void setGridViewData(String[] date, String[] type, int[] dilution_num, String[] exp_param, int[] colony_num, Bitmap[] images){
		this.date = date;
		this.type = type;
		this.dilution_num = dilution_num;
		this.exp_param = exp_param;
		this.colony_num = colony_num;
		this.images = images;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		MenuItem item = menu.add(ACTION_BAR_GROUP, MENU_SEARCH, 0, "搜尋菌落");
		item.setIcon(R.drawable.btn_magnifier);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item.setVisible(isMenuVisible);
	}
	
	public static void setMenuVisible(boolean isVisible){
		isMenuVisible = isVisible;
	}
	
	public void setSearchDateRange(Calendar cal1, Calendar cal2){
		searchFilter.setDateRange(cal1, cal2);
	}
	
	public void setDilutionNumberRange(int i1, int i2){
		searchFilter.setDilutionNumberRange(i1, i2);
	}
	
	public void setDisplaySearchDateRange(Button btn){
		btn.setText(searchFilter.getDateRangeString());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == MENU_SEARCH){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View dialogContent = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_view, null);
			builder.setView(dialogContent);
			builder.setCancelable(false);
			layout_colony_type_list = (LinearLayout) dialogContent.findViewById(R.id.colony_type_list);
			layout_colony_exp_param_list = (LinearLayout) dialogContent.findViewById(R.id.colony_exp_param_list);
		    
			final Button btnSearchDate = (Button) dialogContent.findViewById(R.id.btn_search_date);
			setDisplaySearchDateRange(btnSearchDate);
			btnSearchDate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final CalendarPickerView dialogView = (CalendarPickerView) getActivity().getLayoutInflater().inflate(R.layout.calendarpicker_dialog, null, false);
					final Calendar next1Month = Calendar.getInstance();
					next1Month.add(Calendar.MONTH, 1);
				    final Calendar last3Month = Calendar.getInstance();
				    last3Month.add(Calendar.MONTH, -3);
					
				    // initilize selected date
			        dialogView.init(last3Month.getTime(), next1Month.getTime()).inMode(SelectionMode.RANGE).withSelectedDates(searchFilter.getDateRange());
			        AlertDialog theDialog = new AlertDialog.Builder(getActivity())
			        	.setTitle("設定實驗日期")
			            .setView(dialogView)
			            .setNeutralButton("確定", new DialogInterface.OnClickListener() {
			                @Override 
			                public void onClick(DialogInterface dialogInterface, int i) {
			                    Calendar cal1 = Calendar.getInstance();
			                    Calendar cal2 = Calendar.getInstance();
			                    
			                    List<Date> selectedDates = dialogView.getSelectedDates();
			                    Date startDate = selectedDates.get(0);
			                    Date endDate = selectedDates.get(selectedDates.size()-1);
			                    cal1.setTime(startDate);
			                    cal2.setTime(endDate);
			                    
			                    setSearchDateRange(cal1, cal2);
			                    setDisplaySearchDateRange(btnSearchDate);
			                    dialogInterface.dismiss();
			                }
			            }).create();
			        theDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			            @Override
			            public void onShow(DialogInterface dialogInterface) {
//			                Log.d("test4", "onShow: fix the dimens!");
			                dialogView.fixDialogDimens();
			            }
			        });
			        theDialog.show();
				}
			});
			
		    Button btnSearch3Day = (Button) dialogContent.findViewById(R.id.btn_search_3day);
		    Button btnSearch1Week = (Button) dialogContent.findViewById(R.id.btn_search_1week);
		    Button btnSearch1Month = (Button) dialogContent.findViewById(R.id.btn_search_1month);
		    btnSearch3Day.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar cal1 = Calendar.getInstance();
					cal1.add(Calendar.DATE, -3);
					Calendar cal2 = Calendar.getInstance();
					
					setSearchDateRange(cal1, cal2);
                    setDisplaySearchDateRange(btnSearchDate);
				}
			});
		    btnSearch1Week.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar cal1 = Calendar.getInstance();
					cal1.add(Calendar.DATE, -7);
					Calendar cal2 = Calendar.getInstance();
					
					setSearchDateRange(cal1, cal2);
                    setDisplaySearchDateRange(btnSearchDate);
				}
			});
		    btnSearch1Month.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar cal1 = Calendar.getInstance();
					cal1.add(Calendar.MONTH, -1);
					Calendar cal2 = Calendar.getInstance();
					
					setSearchDateRange(cal1, cal2);
                    setDisplaySearchDateRange(btnSearchDate);
				}
			});
		    
		    RangeBar rangebar = (RangeBar) dialogContent.findViewById(R.id.rangebar);
		    TextView rangebarValue1 = (TextView) dialogContent.findViewById(R.id.rangebar_value_1);
		    TextView rangebarValue2 = (TextView) dialogContent.findViewById(R.id.rangebar_value_2);
		    TextView rangebarValue3 = (TextView) dialogContent.findViewById(R.id.rangebar_value_3);
		    TextView rangebarValue4 = (TextView) dialogContent.findViewById(R.id.rangebar_value_4);
		    TextView rangebarValue5 = (TextView) dialogContent.findViewById(R.id.rangebar_value_5);
		    TextView rangebarValue6 = (TextView) dialogContent.findViewById(R.id.rangebar_value_6);
		    TextView rangebarValue7 = (TextView) dialogContent.findViewById(R.id.rangebar_value_7);
		    TextView rangebarValue8 = (TextView) dialogContent.findViewById(R.id.rangebar_value_8);
		    TextView rangebarValue9 = (TextView) dialogContent.findViewById(R.id.rangebar_value_9);
		    TextView rangebarValue10 = (TextView) dialogContent.findViewById(R.id.rangebar_value_10);
		    rangebarValue1.setText(Html.fromHtml("10<sup><small>-1</small></sup>"));
		    rangebarValue2.setText(Html.fromHtml("10<sup><small>-2</small></sup>"));
		    rangebarValue3.setText(Html.fromHtml("10<sup><small>-3</small></sup>"));
		    rangebarValue4.setText(Html.fromHtml("10<sup><small>-4</small></sup>"));
		    rangebarValue5.setText(Html.fromHtml("10<sup><small>-5</small></sup>"));
		    rangebarValue6.setText(Html.fromHtml("10<sup><small>-6</small></sup>"));
		    rangebarValue7.setText(Html.fromHtml("10<sup><small>-7</small></sup>"));
		    rangebarValue8.setText(Html.fromHtml("10<sup><small>-8</small></sup>"));
		    rangebarValue9.setText(Html.fromHtml("10<sup><small>-9</small></sup>"));
		    rangebarValue10.setText(Html.fromHtml("10<sup><small>-10</small></sup>"));
		    
		    // add colony type list
		    Set<String> colonyTypeStringList = context.loadPrefStringSetData(COLONY_TYPE_LIST);
		    addDefaultListItem(COLONY_TYPE, "不限", colony_type_list, layout_colony_type_list, searchFilter.getColonyType());
		    addSavedListItem(COLONY_TYPE, colonyTypeStringList, searchFilter.getColonyType(), colony_type_list, layout_colony_type_list);
		    
		    // add colony exp param list
		    Set<String> colonyExpParamStringList = context.loadPrefStringSetData(COLONY_EXP_PARAM_LIST);
		    addDefaultListItem(COLONY_EXP_PARAM, "不限", colony_exp_param_list, layout_colony_exp_param_list, searchFilter.getExpParam());
		    addSavedListItem(COLONY_EXP_PARAM, colonyExpParamStringList, searchFilter.getExpParam(), colony_exp_param_list, layout_colony_exp_param_list);		    

		    // initialize selected dilution number range
		    List<Integer> dilutionNumberRange = searchFilter.getDilutionNumberRange();
		    int leftIndex = dilutionNumberRange.get(0) * (-1) - 1;
		    int rightIndex = dilutionNumberRange.get(1) * (-1) - 1;
		    rangebar.setThumbIndices(leftIndex, rightIndex);
		    rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
		        @Override
		        public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) { 
		            setDilutionNumberRange((leftThumbIndex+1)*(-1), (rightThumbIndex+1)*(-1));
		        }
		    });
		    
		    Button btnSearchCancel = (Button) dialogContent.findViewById(R.id.btn_search_cancel);
		    Button btnSearchOK = (Button) dialogContent.findViewById(R.id.btn_search_ok);
		    
//		    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//		    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//		    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//		    lp.horizontalMargin = 0.0f;
//		    lp.verticalMargin = 0.0f;
		    
		    final Dialog d = builder.create();
		    d.show();
//		    d.getWindow().setAttributes(lp);
		    
		    btnSearchCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cleanList();
					d.dismiss();
				}
			});
		    
		    btnSearchOK.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String date = searchFilter.getDateRangeString();
					Set<String> types = searchFilter.getColonyType();
					List<Integer> nums = searchFilter.getDilutionNumberRange();
					Set<String> params = searchFilter.getExpParam();
					
					Log.d("test4", "date = " + date);
					Iterator<String> ite = types.iterator();
					while(ite.hasNext()){
						Log.d("test4", "type = " + ite.next());
					}
					for(Integer i : nums){
						Log.d("test4", "number = " + i);
					}
					ite = params.iterator();
					while(ite.hasNext()){
						Log.d("test4", "param = " + ite.next());
					}
					
					cleanList();
					d.dismiss();
				}
			});
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void cleanList(){
		colony_type_list.clear();
		colony_exp_param_list.clear();
	}
	
	public void addDefaultListItem(String type, String value, List<ListItem> list, LinearLayout layout, Set<String> savedString){
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_listview_item, null);
		ListItem item = new ListItem(value, view, type);
		
		list.add(item);
		layout.addView(view);
		
		// search for option "不限"
		if(savedString.size() == 1 && savedString.contains("不限")){
			item.cbx.setChecked(true);
			item.cbx.setEnabled(false);
		}
	}
	
	public void addSavedListItem(String type, Set<String> stringSet, Set<String> savedString, List<ListItem> list, LinearLayout layout){
		if(stringSet != null){
	    	for(String strOfStringSet : stringSet){
    			View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_listview_item, null);
    			ListItem item = new ListItem(strOfStringSet, view, type);
    			
    			if(savedString.contains(strOfStringSet))
    				item.cbx.setChecked(true);
    			
    			list.add(item);
		    	layout.addView(view);
	    	}
	    }
	}
	
	public void checkSingleChoice(String value, List<ListItem> list){
		// if "不限" is checked, uncheck other options
		if(value.equals("不限")){
			// "不限" is the first option
			list.get(0).cbx.setEnabled(false);
			for(int i = 1; i < list.size(); i++){
				list.get(i).cbx.setChecked(false);
			}
		// if other options get checked, check first option and uncheck it if it is checked
		} else {
			if(list.get(0).cbx.isChecked()){
				list.get(0).cbx.setChecked(false);
				list.get(0).cbx.setEnabled(true);
			}
		}
	}
	
	public void addSearchColonyType(String value){
		searchFilter.addColonyType(value);
		
		if(value.equals("不限")){
			colony_type_list.get(0).cbx.setChecked(true);
			colony_type_list.get(0).cbx.setEnabled(false);
		}
	}
	
	public void removeSearchColonyType(String value){
		searchFilter.removeColonyType(value);
		
		if(searchFilter.getColonyType().size() == 0 && !value.equals("不限"))	
			addSearchColonyType("不限");
	}
	
	public void addSearchExpParam(String value){
		searchFilter.addExpParam(value);
		
		if(value.equals("不限")){
			colony_exp_param_list.get(0).cbx.setChecked(true);
			colony_exp_param_list.get(0).cbx.setEnabled(false);
		}
	}
	
	public void removeSearchExpParam(String value){
		searchFilter.removeExpParam(value);
		
		if(searchFilter.getExpParam().size() == 0 && !value.equals("不限"))
			addSearchExpParam("不限");
	}
	
	class ListItem {
		private String type;
		private String value;
		private CheckBox cbx;
		private TextView tv;
//		private View view;
		
		public ListItem(String value, View view, String type){
			cbx = (CheckBox) view.findViewById(R.id.checkBox2);
			tv = (TextView) view.findViewById(R.id.textView2);
			tv.setText(value);
//			this.view = view;
			this.value = value;
			this.type = type;
			
			cbx.setOnCheckedChangeListener(new CheckedChangeListener());
		}
		
		class CheckedChangeListener implements OnCheckedChangeListener {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					if(type.equals(COLONY_TYPE)){
						checkSingleChoice(value, colony_type_list);
						addSearchColonyType(value);
					} else if(type.equals(COLONY_EXP_PARAM)){
						checkSingleChoice(value, colony_exp_param_list);
						addSearchExpParam(value);
					}
				} else {
					if(type.equals(COLONY_TYPE))
						removeSearchColonyType(value);
					else if(type.equals(COLONY_EXP_PARAM))
						removeSearchExpParam(value);
				}
			}
		}
	}
}