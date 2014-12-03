package com.colonycount.cklab.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends FragmentActivity {
	/**
	 * Debug name for logcat
	 */
	protected String TAG = getClass().getSimpleName();
	/**
	 * Save user login state
	 * Value: boolean
	 */
	protected static final String LOGGED_IN = "logged_in";
	/**
	 * If logged in, save user account
	 * Value: String
	 */
	protected static final String USER_ACCOUNT = "user_account";
	/**
	 * If logged in, save user id
	 * Value: String
	 */
	protected static final String USER_ID = "user_id";
	
	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;
	
	/**
	 * �N����x�s�bSharedPreferences
	 * @param key
	 * @param value
	 */
	protected void savePrefData(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * �N����x�s�bSharedPreferences
	 * @param key
	 * @param value
	 */
	protected void savePrefData(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	
	protected void savePrefData(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}
	
	/**
	 * �qSharedPreferencesŪ����
	 * @param key
	 * @return String �Y��Ƭ��šA�h�^��null
	 */
	protected String loadPrefStringData(String key) {
		return settings.getString(key, null);		
	}
	
	/**
	 * �qSharedPreferencesŪ����
	 * @param key
	 * @return boolean �Y��Ƭ��šA�h�^��false
	 */
	protected boolean loadPrefBooleanData(String key) {
		return settings.getBoolean(key, false);		
	}
	
	/**
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	protected int loadPrefIntData(String key, int defValue){
		return settings.getInt(key, defValue);
	}
	
	protected void removePrefData(String key){
		editor.remove(key);
		editor.commit();
	}
	
	/**
	 * set activity to full screen
	 */
	protected void setFullScreen(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = settings.edit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
}
