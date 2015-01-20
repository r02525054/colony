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
	
	// colony info
	private final String[] date;
	private final String[] type;
	private final int[] dilution_num;
	private final String[] exp_param;
	private final int[] colony_num;
	private final Bitmap[] images;

	public CustomGrid(Context mContext, String[] date, String[] type, int[] dilution_num, String[] exp_param, int[] colony_num, Bitmap[] images) {
		this.mContext = mContext;
		this.date = date;
		this.type = type;
		this.dilution_num = dilution_num;
		this.exp_param = exp_param;
		this.colony_num = colony_num;
		this.images = images;
	}

	@Override
	public int getCount() {
		if(images == null)
			return 0;
		
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ImageView picture;
        TextView tNum;
        TextView tDate;
        TextView tType;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if(v == null) {
            v = inflater.inflate(R.layout.custom_grid, parent, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
//            v.setTag(R.id.text1, v.findViewById(R.id.text1));
//            v.setTag(R.id.text2, v.findViewById(R.id.text2));
//            v.setTag(R.id.text3, v.findViewById(R.id.text3));
        }

        picture = (ImageView) v.getTag(R.id.picture);
//        tNum = (TextView) v.getTag(R.id.text1);
//        tDate = (TextView) v.getTag(R.id.text2);
//        tType = (TextView) v.getTag(R.id.text3);
        
        picture.setImageBitmap(images[position]);
//        tNum.setText(num[position]);
//        tDate.setText(date[position]);
//        tType.setText(type[position]);

        return v;
	}
}
