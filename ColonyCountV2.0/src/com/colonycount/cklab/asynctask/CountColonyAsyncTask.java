package com.colonycount.cklab.asynctask;

import java.io.FileOutputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import com.colonycount.cklab.asynctask.base.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.base.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.base.BaseAsyncTask;
import com.colonycount.cklab.config.Config;
import com.colonycount.cklab.model.Component;
import com.colonycount.cklab.utils.CustomKmeans2;

public class CountColonyAsyncTask extends BaseAsyncTask {
	private Context context;
	
	public CountColonyAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls, showDialog);
		
		this.context = context;
	}

	/**
	 * 
	 */
	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		AsyncTaskPayload resultPayload = new AsyncTaskPayload();
		
		// output image with size 1024x1024
		Bitmap croppedImage = params[0].getRawImg();
		
		// create down-scale image with size 512x512 for counting colony
		Bitmap countImage = Bitmap.createScaledBitmap(croppedImage, Config.COUNT_IMAGE_WIDTH, Config.COUNT_IMAGE_HEIGHT, false);
		
		// save file to pass to another activity
		try {
			String filename = "bitmap.png";
		    FileOutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
		    croppedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
		    stream.close();
		    
		    if(croppedImage != null && !croppedImage.isRecycled()){
		    	croppedImage.recycle();
		    	croppedImage = null;
		    }
		    
		    resultPayload.putValue("image", filename);
		} catch (Exception e) {
			resultPayload.putValue("result", "error");
			resultPayload.putValue("msg", e.toString());
			return resultPayload;
		}
		
		// my algorithm
//		ColonyModel algorithm = new ColonyModel(b);
		CustomKmeans2 algorithm = new CustomKmeans2(countImage, 3);
		List<Component> components = algorithm.count();
		resultPayload.putValue("result", "success");
		resultPayload.setComponents(components);
		
		// test
		resultPayload.setRawImg(algorithm.getResult());
		
		return resultPayload;
	}
}
