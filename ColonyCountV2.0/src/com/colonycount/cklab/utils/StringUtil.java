package com.colonycount.cklab.utils;


import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class StringUtil {
	public static String getFilePathFromUri(Context context, Uri uri){
		CursorLoader loader = new CursorLoader(context, uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
		Cursor cursor = loader.loadInBackground();
		cursor.moveToFirst();
		String path = cursor.getString(0);
		
		return path;
		
	}
}