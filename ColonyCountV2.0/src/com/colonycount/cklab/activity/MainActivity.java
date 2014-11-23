package com.colonycount.cklab.activity;

import com.colonycount.cklab.base.BaseActivity;

import android.content.Intent;
import android.util.Log;

public class MainActivity extends BaseActivity {
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		Log.d(TAG, "onStart");
		Log.d(TAG, "LOGGED_IN:" + loadPrefBooleanData(LOGGED_IN));
		
		Intent intent = new Intent();
		// not logged in
		if(!loadPrefBooleanData(LOGGED_IN)){
			intent.setClass(MainActivity.this, LoginActivity.class);
		// logged in
		} else {
			intent.setClass(MainActivity.this, HomeActivity.class);
		}
		startActivity(intent);
		finish();
	}
}