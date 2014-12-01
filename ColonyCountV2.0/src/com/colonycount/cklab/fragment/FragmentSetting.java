package com.colonycount.cklab.fragment;

import com.colonycount.cklab.activity.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentSetting extends Fragment {
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.layout_fragment_setting, container, false);
		TextView txtCat = (TextView) view.findViewById(R.id.textView1);
        txtCat.setText("settings");
		
		return view;
	}	
}