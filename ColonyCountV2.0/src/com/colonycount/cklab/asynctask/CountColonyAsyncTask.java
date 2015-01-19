package com.colonycount.cklab.asynctask;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import com.colonycount.cklab.algorithm.ColonyModel;
import com.colonycount.cklab.algorithm.CustomKmeans;
import com.colonycount.cklab.base.BaseAsyncTask;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.utils.CustomKmeans2;

public class CountColonyAsyncTask extends BaseAsyncTask {

	public CountColonyAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls, showDialog);
	}

	/**
	 * 
	 */
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		Bitmap b = params[0].getRawImg();
		// my algorithm
//		ColonyModel algorithm = new ColonyModel(b);
		
		
//		CustomKmeans2 algorithm = new CustomKmeans2(b, 3);
		// -----------------
		
//		List<Component> components = algorithm.count();
//		algorithm.count();
		AsyncTaskPayload resultPayload = new AsyncTaskPayload();
//		resultPayload.setComponents(components);
		resultPayload.setRawImg(params[0].getRawImg());
		
		return resultPayload;
	}
}
