package com.colonycount.cklab.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;

public class BitmapUtil {
	/**
	 * 
	 * @param path
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inMutable = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	/**
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    
	    //Log.d(TAG, "sampleSize:" + inSampleSize);
	    //Log.d(TAG, "height:" + height/inSampleSize + ", width:" + width/inSampleSize);
	    return inSampleSize;
	}
	
	
	/**
	 * 
	 * @param contentResolver
	 * @param photoUri
	 * @return
	 */
	public static int getOrientation(ContentResolver contentResolver, Uri photoUri) {
	    Cursor cursor = contentResolver.query(photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
	    
	    if (cursor.getCount() != 1) {
	        return -1;
	    }

	    cursor.moveToFirst();
	    return cursor.getInt(0);
	}
	
	/**
	 * 
	 */
	public static Bitmap createForegroundImage(Bitmap source, int radius){
		Bitmap temp = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		temp.eraseColor(Color.BLACK);
		Canvas canvas = new Canvas(temp);
		Path path = new Path();
		
		// nned to change the centre
		path.addCircle(source.getWidth()/2, source.getHeight()/2, radius, Path.Direction.CCW);
		canvas.clipPath(path);
		
		Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
		canvas.drawBitmap(source, rect, rect, null);
		
		return temp;
	}
	
	/**
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int rgb2gray(int r, int g, int b){
		return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
	}
}
