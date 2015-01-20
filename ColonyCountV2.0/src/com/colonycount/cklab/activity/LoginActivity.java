package com.colonycount.cklab.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.LoginAsyncTask;
import com.colonycount.cklab.base.GPlusClientActivity;
import com.colonycount.cklab.fragment.FragmentDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.AgeRange;
import com.google.android.gms.plus.model.people.Person.Image;
import com.google.android.gms.plus.model.people.Person.Name;
import com.google.android.gms.plus.model.people.Person.Organizations;

public class LoginActivity extends GPlusClientActivity implements View.OnClickListener, AsyncTaskCompleteListener<Boolean> {
	private SignInButton mSignInButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		setViews();
		setListeners();

		
		if (savedInstanceState != null) {
			mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	
	private void setViews(){
		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
	}
	
	
	private void setListeners(){
		mSignInButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (!mGoogleApiClient.isConnecting()) {
			// We only process button clicks when GoogleApiClient is not transitioning between connected and not connected.
			switch (v.getId()) {
			case R.id.sign_in_button:
				mGoogleApiClient.connect();
				mSignInProgress = STATE_SIGN_IN;
				break;
			}
		}
	}

	/*
	 * onConnected is called when our Activity successfully connects to Google
	 * Play services. onConnected indicates that an account was selected on the
	 * device, that the selected account has granted any requested permissions
	 * to our app and that we were able to establish a service connection to
	 * Google Play services.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Reaching onConnected means we consider the user signed in.
		Log.d(TAG, "onConnected");
		
		// Retrieve some profile information to personalize our app for the user.
		Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		
		Name name = currentUser.getName();
		AgeRange ageRange = currentUser.getAgeRange();
		Image image = currentUser.getImage();
		List<Organizations> organizations = currentUser.getOrganizations();
		
		// Retrive person info
		String email 		   = Plus.AccountApi.getAccountName(mGoogleApiClient);
		String id 			   = currentUser.getId();
		String displayName     = currentUser.getDisplayName();
		int    gender 		   = currentUser.getGender();
		String aboutMe 		   = currentUser.getAboutMe();
		String birthday 	   = currentUser.getBirthday();
		String currentLocation = currentUser.getCurrentLocation();
		String language        = currentUser.getLanguage();
		String nickname		   = currentUser.getNickname();
		String url 			   = currentUser.getUrl();
		
		String familyName	   = "";
		String givenName	   = "";
		if(name != null){
			familyName = name.getFamilyName();
			givenName = name.getGivenName();
		}
		
		int ageMin = -1;
		int ageMax = -1;
		if(ageRange != null){
			ageMin 		   = ageRange.getMin();
			ageMax 		   = ageRange.getMax();
		}
		
		String imageUrl = "";
		if(image != null){
			imageUrl = image.getUrl();
		}
		
		String orgs            = "";
		
		if(organizations != null){
			for(int i = 0; i < organizations.size(); i++){
				if(i == organizations.size()-1)
					orgs += organizations.get(i).toString();
				else 
					orgs += organizations.get(i).toString() + ",, ";
			}
		}
		
		Map<String, String> request = new HashMap<String, String>();
		request.put("email", email);
		request.put("id", id);
		request.put("displayName", displayName);
		request.put("gender", gender+"");
		request.put("aboutMe", aboutMe);
		request.put("birthday", birthday);
		request.put("currentLocation", currentLocation);
		request.put("language", language);
		request.put("nickname", nickname);
		request.put("url", url);
		request.put("familyName", familyName);
		request.put("givenName", givenName);
		request.put("ageMin", ageMin+"");
		request.put("ageMax", ageMax+"");
		request.put("imageUrl", imageUrl);
		request.put("orgs", orgs);
		
		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
		
//		Log.d("test", "user account = " + loadPrefStringData(USER_ACCOUNT));
//    	Log.d("test", "user id = " + loadPrefStringData(USER_ID));
		
		// save logged in information
		savePrefData(LOGGED_IN, true);
		savePrefData(USER_ACCOUNT, email);
		savePrefData(USER_ID, id);
		
		LoginAsyncTask dbTask = new LoginAsyncTask(this, "系統訊息", "登入中，請稍後...", this, LoginAsyncTask.class);
		request.put("check_user", email);
		dbTask.setRequest(request);
		dbTask.execute();
	}

	
	/*
	 * onConnectionFailed is called when our Activity could not connect to
	 * Google Play services. onConnectionFailed indicates that the user needs to
	 * select an account, grant permissions or resolve an error in order to sign
	 * in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes
		// might be returned in onConnectionFailed.
		Log.d(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
		
		if (mSignInProgress != STATE_IN_PROGRESS) {
			// We do not have an intent in progress so we should store the
			// latest error resolution intent for use when the sign in button is clicked.
			mSignInIntent = result.getResolution();
			mSignInError = result.getErrorCode();
			
			if (mSignInProgress == STATE_SIGN_IN) {
				Log.d(TAG, "onConnectionFailed: STATE_SIGN_IN");
				// STATE_SIGN_IN indicates the user already clicked the sign in
				// button so we should continue processing errors until the user is
				// signed in or they click cancel.
				resolveSignInError();
			}
		}
	}

	
	/*
	 * Starts an appropriate intent or dialog for user interaction to resolve
	 * the current error preventing the user from being signed in. This could be
	 * a dialog allowing the user to select an account, an activity allowing the
	 * user to consent to the permissions being requested by your app, a setting
	 * to enable device networking, etc.
	 */
	private void resolveSignInError() {
		Log.d(TAG, "resolveSignInError");
		
		if (mSignInIntent != null) {
			// We have an intent which will allow our user to sign in or
			// resolve an error. For example if the user needs to
			// select an account to sign in with, or if they need to consent
			// to the permissions your app is requesting.

			try {
				// Send the pending intent that we stored on the most recent
				// OnConnectionFailed callback. This will allow the user to
				// resolve the error currently preventing our connection to
				// Google Play services.
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.d(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
				// The intent was canceled before it was sent. Attempt to
				// connect to get an updated ConnectionResult.
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			// Google Play services wasn't able to provide an intent for some
			// error types, so we show the default Google Play services error
			// dialog which may still start an intent on our behalf if the
			// user can resolve the issue.
			Resources resources = getResources();
			new FragmentDialog(null, 
					           resources.getString(R.string.google_login_error_msg), 
					           0, 
					           resources.getString(R.string.google_dialog_close), 
					           null, 
					           null, 
					           null).show(getSupportFragmentManager(), "dialog");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RC_SIGN_IN:
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "onActivityResult: RESULT_OK");
				// If the error resolution was successful we should continue
				// processing errors.
				mSignInProgress = STATE_SIGN_IN;
				
				// Update the user interface to reflect that the user is signed in.
				mSignInButton.setEnabled(false);
			} else {
				// If the error resolution was not successful or the user
				// canceled, we should stop processing errors.
				mSignInProgress = STATE_DEFAULT;
			}

			if (!mGoogleApiClient.isConnecting()) {
				// If Google Play services resolved the issue with a dialog then
				// onStart is not called so we need to re-attempt connection
				// here.
				mGoogleApiClient.connect();
			}
			break;
		}
	}


	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(TAG, "onConnectionSuspended");
		// The connection to Google Play services was lost for some reason.
		// We call connect() to attempt to re-establish the connection or get a
		// ConnectionResult that we can attempt to resolve.
		mGoogleApiClient.connect();
	}

	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		String status = result.getValue("status");
		String msg = result.getValue("msg");
		
		if(status.equals("success")){
			if(taskName.equals("LoginAsyncTask") && msg.equals("success")){
				Intent intent = new Intent(this, HomeActivity.class);
				startActivity(intent);
				finish();
			}
		} else {
			Toast.makeText(this, "error: " + msg, Toast.LENGTH_LONG).show();
		}
	}
}