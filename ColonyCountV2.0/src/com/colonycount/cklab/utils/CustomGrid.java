package com.colonycount.cklab.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.colonycount.cklab.activity.R;

public class CustomGrid extends BaseAdapter {
	private Context mContext;
	private final String[] num;
	private final String[] date;
	private final String[] type;
	private final Bitmap[] images;

	public CustomGrid(Context mContext, String[] num, String[] date, String[] type, Bitmap[] Images) {
		this.mContext = mContext;
		this.num = num;
		this.date = date;
		this.type = type;
		this.images = Images;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		View grid;
//		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		if (convertView == null) {
//			grid = new View(mContext);
//			grid = inflater.inflate(R.layout.custom_grid, null);
//			TextView textView = (TextView) grid.findViewById(R.id.textView1);
//			ImageView imageView = (ImageView) grid.findViewById(R.id.imageView1);
//			textView.setText(texts[position]);
//			imageView.setImageBitmap(images[position]);
//		} else {
//			grid = (View) convertView;
//		}
//		
//		return grid;
		
		View v = convertView;
        ImageView picture;
        TextView tNum;
        TextView tDate;
        TextView tType;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if(v == null) {
            v = inflater.inflate(R.layout.custom_grid, parent, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text1, v.findViewById(R.id.text1));
            v.setTag(R.id.text2, v.findViewById(R.id.text2));
            v.setTag(R.id.text3, v.findViewById(R.id.text3));
        }

        picture = (ImageView) v.getTag(R.id.picture);
        tNum = (TextView) v.getTag(R.id.text1);
        tDate = (TextView) v.getTag(R.id.text2);
        tType = (TextView) v.getTag(R.id.text3);
        
        picture.setImageBitmap(images[position]);
        tNum.setText(num[position]);
        tDate.setText(date[position]);
        tType.setText(type[position]);

        return v;
	}
}
