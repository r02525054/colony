package com.colonycount.cklab.fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.GridView;
import android.widget.Toast;

import com.colonycount.cklab.activity.R;
import com.colonycount.cklab.activity.TakePhotoActivity;
import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.base.BaseAsyncTask;
import com.colonycount.cklab.crop.CropImageIntentBuilder;
import com.colonycount.cklab.crop.MediaStoreUtils;
import com.colonycount.cklab.utils.CustomGrid;

public class FragmentHome extends Fragment implements View.OnClickListener, AsyncTaskCompleteListener<Boolean>, View.OnFocusChangeListener {
	public static final int TAKE_PHOTO = 0;
	public static final int SELECT_PHOTO = 1;
	
	private static int REQUEST_PICTURE = 1;
	private static int REQUEST_CROP_PICTURE = 2;
	
	private ImageButton btn_take_photo;
    private ImageButton btn_select_photo;
    private GridView gridView;
    
    private String user_id;
    private String[] urls;
 
    private Bitmap[] photos;
    private String[] num;
    private String[] date;
    private String[] type;
    
    private AlphaAnimation buttonClickAnimation = new AlphaAnimation(1F, 0.8F);
    
    public FragmentHome(String user_id){
    	this.user_id = user_id;
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		new BaseAsyncTask(getActivity(), "系統訊息", "讀取照片中，請稍後...", this, BaseAsyncTask.class){

			@Override
			protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
				// TODO Auto-generated method stub
				AsyncTaskPayload result = new AsyncTaskPayload();
				result.putValue("result", "success");
				
				String postUrl = "http://140.112.26.221/~master11360/colony%20count/php/db_connect.php";
				String photoUrlPrefix = "http://140.112.26.221/~master11360/colony%20count";
				HttpPost httpRequest = new HttpPost(postUrl);
				List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
				addRequest(reqParams);
				String strResult = null;
				
				try {
					httpRequest.setEntity(new UrlEncodedFormEntity(reqParams, HTTP.UTF_8));
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
					
					if(httpResponse.getStatusLine().getStatusCode() == 200){
						strResult = EntityUtils.toString(httpResponse.getEntity());
						JSONObject jsonObject = new JSONObject(strResult);
						String status = jsonObject.getString("status");
						
						if(status.equals("success")){
							JSONArray jsonArray = jsonObject.getJSONArray("result");
							urls = new String[jsonArray.length()];
							photos = new Bitmap[jsonArray.length()];
							num = new String[jsonArray.length()];
							date = new String[jsonArray.length()];
							type = new String[jsonArray.length()];
							
							// TODO: change the url
							for(int i = 0; i < jsonArray.length(); i++){
								JSONObject colony = jsonArray.getJSONObject(i);
								urls[i] = photoUrlPrefix + colony.getString("img_url").substring(colony.getString("img_url").indexOf("/"));
								num[i] = colony.getString("tag_number");
								date[i] = colony.getString("tag_date");
								type[i] = colony.getString("tag_type");
							}
						} else if(status.equals("error")){
							
						}
					// TODO: get httpResponse status
					} else {
						Log.d("Test2", "not 200");
						Log.d("Test2", "getStatusCode = " + httpResponse.getStatusLine().getStatusCode());
					}
					
					// get images from urls
					if(urls != null){
						for(int i = 0; i < photos.length; i++){
							Bitmap bmImg = null;
							try {
							    HttpURLConnection conn= (HttpURLConnection) new URL(urls[i]).openConnection();
							    conn.setDoInput(true);
							    conn.connect();
							    InputStream is = conn.getInputStream();
		
							    bmImg = BitmapFactory.decodeStream(is);
						    } catch (IOException e) {
						        // TODO Auto-generated catch block
						        e.printStackTrace();
						        return null;
					        }
							
							if(bmImg != null)
								photos[i] = bmImg;
						}
					}
				} catch(Exception e) {
					Log.d(TAG, "exception: " + e.toString());
				}
				
				return result;
			}
			
			public void addRequest(List<NameValuePair> reqParams){
				reqParams.add(new BasicNameValuePair("get_img_data", "true"));
				reqParams.add(new BasicNameValuePair("user_id", user_id));
			}
		}.execute();
	}

	private void setViews(View view){
		btn_take_photo   = (ImageButton) view.findViewById(R.id.btn_take_photo);
		btn_select_photo = (ImageButton) view.findViewById(R.id.btn_select_photo);
		gridView = (GridView) view.findViewById(R.id.gridView1);
	}
	
	private void setListeners(){
		btn_take_photo.setOnClickListener(this);
		btn_select_photo.setOnClickListener(this);
		btn_take_photo.setOnFocusChangeListener(this);
		btn_select_photo.setOnFocusChangeListener(this);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		setViews(view);
		setListeners();
		
		return view;
	}
	
	@Override
	public void onClick(View view) {
		view.startAnimation(buttonClickAnimation);
		
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
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		
//		if(resultCode == Activity.RESULT_OK){
//			if(requestCode == SELECT_PHOTO){
//				intent.putExtra("requestCode", SELECT_PHOTO);
//			} 
////			else if(requestCode == TAKE_PHOTO && resultCode == Activity.RESULT_OK){
////				intent.putExtra("requestCode", TAKE_PHOTO);
////			}
//			
//			intent.setClass(getActivity(), Test.class);
//			startActivity(intent);
//		}
		
		File croppedImageFile = new File(getActivity().getFilesDir(), "test.jpg");
        if ((requestCode == REQUEST_PICTURE) && (resultCode == getActivity().RESULT_OK)) {
        	Uri croppedImage = Uri.fromFile(croppedImageFile);
        	
        	CropImageIntentBuilder cropImage = new CropImageIntentBuilder(480, 480, croppedImage);
            cropImage.setSourceImage(intent.getData());
            cropImage.setCircleCrop(true);
            cropImage.setDoFaceDetection(false);
            
            startActivity(cropImage.getIntent(getActivity()));
        }
	}

	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		// TODO Auto-generated method stub
		if(photos != null && photos.length != 0)
			setGridView();
	}
	
	public void setGridView(){
		final Context context = getActivity();
		CustomGrid adapter = new CustomGrid(context, num, date, type, photos);
		gridView.setAdapter(adapter);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
//		if(!hasFocus)
//			v.startAnimation(buttonClickAnimation);
	}
}