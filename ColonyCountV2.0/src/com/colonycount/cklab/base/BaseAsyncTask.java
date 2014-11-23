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
	
	public BaseAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls) {
		this.context = context;
        this.progressDialogTitle = progressDialogTitle;
        this.progressDialogMsg = progressDialogMsg;
        this.listener = listener;
        this.TAG = cls.getSimpleName();
    }
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_DARK);
		progressDialog.setTitle(progressDialogTitle);
		progressDialog.setMessage(progressDialogMsg);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			progressDialog.setMax(100);
		progressDialog.setCancelable(false);
		progressDialog.show();
    }
	
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		// TODO Auto-generated method stub
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
