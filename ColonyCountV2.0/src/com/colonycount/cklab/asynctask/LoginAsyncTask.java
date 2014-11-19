package com.colonycount.cklab.asynctask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.colonycount.cklab.base.BaseAsyncTask;

public class LoginAsyncTask extends BaseAsyncTask {
	private String postUrl = "http://140.112.26.221/~master11360/colony%20count/php/db_connect.php";
	private Map<String,String> request;
	
	public LoginAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		// TODO Auto-generated method stub
		HttpPost httpRequest = new HttpPost(postUrl);
		List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
		addRequest(reqParams);
		String strResult = null;
		
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(reqParams, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				strResult = EntityUtils.toString(httpResponse.getEntity());
				Log.d(TAG, "post result = " + strResult);
			}
		} catch(Exception e) {
			Log.d(TAG, "exception: " + e.toString());
		}
		
		
		AsyncTaskPayload result = new AsyncTaskPayload();
		result.putValue("result", strResult);
		
		return result;
	}

	public void setRequest(Map<String,String> request){
		this.request = request;
	}
	
	public void addRequest(List<NameValuePair> reqParams){
		Set<String> keys = request.keySet();
		Iterator<String> it = keys.iterator();
		for(int i = 0; i < request.size(); i++){
			String key = it.next();
			reqParams.add(new BasicNameValuePair(key, request.get(key)));
		}
	}
}
