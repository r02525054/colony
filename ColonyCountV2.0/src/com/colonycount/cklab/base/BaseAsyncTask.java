package com.colonycount.cklab.base;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class BaseAsyncTask extends AsyncTask<AsyncTaskPayload, Void, AsyncTaskPayload> {
	protected String TAG = BaseAsyncTask.class.getSimpleName();
	
	protected Context context;
	private ProgressDialog progressDialog;
	private String progressDialogTitle;
	private String progressDialogMsg;
	private AsyncTaskCompleteListener<Boolean> listener;
	private boolean showDialog;
	
	public BaseAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog) {
		this.context = context;
        this.progressDialogTitle = progressDialogTitle;
        this.progressDialogMsg = progressDialogMsg;
        this.listener = listener;
        this.TAG = cls.getSimpleName();
        this.showDialog = showDialog;
    }
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_DARK);
		if(showDialog){
			progressDialog.setTitle(progressDialogTitle);
			progressDialog.setMessage(progressDialogMsg);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	//			progressDialog.setMax(100);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
    }
	
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		return null;
	}

	@Override
	protected void onPostExecute(AsyncTaskPayload result) {
		if (progressDialog.isShowing()) {
        	progressDialog.dismiss();
        }
        
        listener.onTaskComplete(result, TAG);
	}
}
