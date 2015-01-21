package com.colonycount.cklab.asynctask;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.colonycount.cklab.base.BaseAsyncTask;

public class GetImgAsyncTask extends BaseAsyncTask {
	private String userId;
	
	public GetImgAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog, String userId) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls, showDialog);
		this.userId = userId;
	}
	
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		String postUrl = "http://140.112.26.221/~master11360/colony%20count/php/db_connect.php";
		String photoUrlPrefix = "http://140.112.26.221/~master11360/colony%20count";
		HttpPost httpRequest = new HttpPost(postUrl);
		List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
		addRequest(reqParams);
		String strResult = null;
		
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(reqParams, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			Log.d("test4", "status code = " + httpResponse.getStatusLine().getStatusCode());
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				strResult = EntityUtils.toString(httpResponse.getEntity());
				JSONObject jsonObject = new JSONObject(strResult);
				String status = jsonObject.getString("status");
				
				Log.d("test4", "status = " + status);
				if(status.equals("success")){
					JSONArray jsonArray = jsonObject.getJSONArray("result");
					
//					urls = new String[jsonArray.length()];
//					photos = new Bitmap[jsonArray.length()];
//					num = new String[jsonArray.length()];
//					date = new String[jsonArray.length()];
//					type = new String[jsonArray.length()];
					
					// TODO: change the url
					for(int i = 0; i < jsonArray.length(); i++){
						JSONObject colony = jsonArray.getJSONObject(i);
						
						String url = photoUrlPrefix + colony.getString("img_url").substring(colony.getString("img_url").indexOf("/"));
						
//						Log.d("test4", "url = " + url);
						
						Log.d("test4", colony.toString());
						
						
//						urls[i] = photoUrlPrefix + colony.getString("img_url").substring(colony.getString("img_url").indexOf("/"));
//						num[i] = colony.getString("tag_number");
//						date[i] = colony.getString("tag_date");
//						type[i] = colony.getString("tag_type");
					}
				} else if(status.equals("error")){
					
				}
			} else {
				Log.d("Test2", "not 200");
				Log.d("Test2", "getStatusCode = " + httpResponse.getStatusLine().getStatusCode());
			}
			
			// get images from urls
//			if(urls != null){
//				for(int i = 0; i < photos.length; i++){
//					Bitmap bmImg = null;
//					try {
//					    HttpURLConnection conn= (HttpURLConnection) new URL(urls[i]).openConnection();
//					    conn.setDoInput(true);
//					    conn.connect();
//					    InputStream is = conn.getInputStream();
//
//					    bmImg = BitmapFactory.decodeStream(is);
//				    } catch (IOException e) {
//				        // TODO Auto-generated catch block
//				        e.printStackTrace();
//				        return null;
//			        }
//					
//					if(bmImg != null)
//						photos[i] = bmImg;
//				}
//			}
		} catch(Exception e) {
			Log.d(TAG, "exception: " + e.toString());
		}
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		AsyncTaskPayload result = new AsyncTaskPayload();
		result.putValue("result", "success");
		
		return result;
	}

	public void addRequest(List<NameValuePair> reqParams){
		reqParams.add(new BasicNameValuePair("get_img_data", "true"));
		Log.d("test4", "userid = " + userId);
		reqParams.add(new BasicNameValuePair("user_id", userId));
	}
}
