package com.colonycount.cklab.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.colonycount.cklab.activity.R;
import com.colonycount.cklab.callback.BitmapDecodedListener;
import com.colonycount.cklab.libs.webCacheeImageView.WebCachedImageView;

public class CustomGrid extends BaseAdapter {
	private String[] date;
	private String[] colony_num;
	private String[] img_urls;
	private LayoutInflater inflater;
	private BitmapDecodedListener bitmapDecodedListener;

	public CustomGrid(Context mContext, String[] date, String[] colony_num, String[] img_urls, BitmapDecodedListener bitmapDecodedListener) {
		this.date = date;
		this.colony_num = colony_num;
		this.img_urls = img_urls;
		inflater = LayoutInflater.from(mContext);
		this.bitmapDecodedListener = bitmapDecodedListener;
	}

	@Override
	public int getCount() {
		if(img_urls == null)
			return 0;
		
		return img_urls.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GridItem gridItem;
		
        if(convertView == null) {
        	convertView = inflater.inflate(R.layout.custom_grid, null);
            
        	WebCachedImageView img = (WebCachedImageView) convertView.findViewById(R.id.picture);
        	TextView expDate = (TextView) convertView.findViewById(R.id.date);
        	TextView num = (TextView) convertView.findViewById(R.id.colony_num);
        	gridItem = new GridItem(img, expDate, num);
        	
        	img.setBitmapDecodedListener(bitmapDecodedListener);
        	img.setImageUrl(img_urls[position]);
        	num.setText(colony_num[position]);
        	expDate.setText(date[position]);
        	
        	convertView.setTag(gridItem);
        } else {
        	gridItem = (GridItem) convertView.getTag();
        }

        return convertView;
	}
	
	
	class GridItem {
		WebCachedImageView img;
		TextView date;
		TextView num;
		
		public GridItem(WebCachedImageView img, TextView date, TextView num){
			this.img = img;
			this.date = date;
			this.num = num;
		}
	}
}
