package com.colonycount.cklab.activity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.colonycount.cklab.base.GPlusClientActivity;
import com.colonycount.cklab.fragment.FragmentDialog;
import com.colonycount.cklab.fragment.FragmentHome;
import com.colonycount.cklab.fragment.FragmentSetting;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.Plus;

public class HomeActivity extends GPlusClientActivity {
	private DrawerLayout layDrawer;
    private ListView lstDrawer;
    
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    private int nowPage;
    
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {  
        @Override  
        public void onManagerConnected(int status) {  
            switch (status) {  
                case LoaderCallbackInterface.SUCCESS:
                	break;  
                default:
                    super.onManagerConnected(status);  
                    break;  
            }  
        }  
    };  
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mOpenCVCallBack)){
	        Log.d("debug", "Cannot connect to OpenCV Manager");
	    } 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		initActionBar();
        initDrawer();
        initDrawerList();
        
        if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FragmentHome(loadPrefStringData(USER_ID))).commit();
			String title = getResources().getStringArray(R.array.drawer_menu)[0];
			mTitle = title;
		    getActionBar().setTitle(title);
		    nowPage = 0;
		}
        
        /*int wantedPosition = 0; // Whatever position you're looking for
        int firstPosition = lstDrawer.getFirstVisiblePosition() - lstDrawer.getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = wantedPosition - firstPosition;
        // Say, first visible position is 8, you want position 10, wantedChild will now be 2
        // So that means your view is child #2 in the ViewGroup:
        if (wantedChild < 0 || wantedChild >= lstDrawer.getChildCount()) {
          Log.w(TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
          return;
        }
        // Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
        View wantedView = lstDrawer.getChildAt(wantedChild);*/
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void initActionBar(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }
	
	private void initDrawer(){
        setContentView(R.layout.drawer);

        layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lstDrawer = (ListView) findViewById(R.id.left_drawer);
        // TODO: add shadow
//        layDrawer.setDrawerShadow(R.drawable.shadow, GravityCompat.START);
        
        mTitle = mDrawerTitle = getTitle();
        drawerToggle = new ActionBarDrawerToggle(
                this, 
                layDrawer,
                R.drawable.drawer_menu,
                R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                
                supportInvalidateOptionsMenu(); 
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                
                supportInvalidateOptionsMenu();
            }
        };
        drawerToggle.syncState();

        layDrawer.setDrawerListener(drawerToggle);
    }
	
	private void initDrawerList(){
        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);
//        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.drawer_list_item, drawer_menu);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer_menu);
        lstDrawer.setAdapter(adapter);
        lstDrawer.setOnItemClickListener(new DrawerItemClickListener());
    }
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    //home
	    if (drawerToggle.onOptionsItemSelected(item)) {
	        return true;
	    }
	    
	    //action buttons
	    switch (item.getItemId()) {
	    case R.id.actionbar_menu:
	        //....
	        break;
	    default:
	        break;
	    }

	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	
	private void selectItem(int position) {
		Fragment fragment = null;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		String title = getResources().getStringArray(R.array.drawer_menu)[position];
		
//		TextView tv = (TextView) lstDrawer.getChildAt(position);
//		tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
		
	    switch (position) {
	    case 0:
	    	if(nowPage != 0){
	    		fragment = new FragmentHome(loadPrefStringData(USER_ID));
			    // -----
			    fragmentTransaction.replace(R.id.content_frame, fragment);
			    fragmentTransaction.addToBackStack("home");
			    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			    fragmentTransaction.commit();

			    lstDrawer.setItemChecked(position, true);
			    mTitle = title;
			    getActionBar().setTitle(title);
			    layDrawer.closeDrawer(lstDrawer);
			    
			    setPage(0);
	    	}
	    	break;
	    case 1:
	    	if(nowPage != 1){
	    		fragment = new FragmentSetting();
		        
		        // -----
			    fragmentTransaction.replace(R.id.content_frame, fragment);
			    fragmentTransaction.addToBackStack("home");
			    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			    fragmentTransaction.commit();

			    lstDrawer.setItemChecked(position, true);
			    mTitle = title;
			    getActionBar().setTitle(title);
			    layDrawer.closeDrawer(lstDrawer);
			    
			    setPage(1);
	    	}
	    	break;
	    case 2:
	    	Log.d("test", "user account = " + loadPrefStringData(USER_ACCOUNT));
	    	Log.d("test", "user id = " + loadPrefStringData(USER_ID));
	    	
	    	Resources resources = getResources();
	    	new FragmentDialog(resources.getString(R.string.logout_title), 
	    					   resources.getString(R.string.logout_msg),
	    					   0, 
	    					   resources.getString(R.string.btn_ok), 
	    					   resources.getString(R.string.btn_cancel), 
	    					   new DialogInterface.OnClickListener() {
								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (mGoogleApiClient.isConnected()) {
											savePrefData(LOGGED_IN, false);
											removePrefData(USER_ACCOUNT);
											removePrefData(USER_ID);
											
											Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
											mGoogleApiClient.disconnect();
											mGoogleApiClient.connect();
										}
									}
								}, 
	    					   null).show(getSupportFragmentManager(), "dialog");
	    	break;
	    default:
	        //�����S���s���@���������������Afragment ���O null���A������������������^
	        return;
	    }
	}

	
//	@Override
//	public void onConnectionFailed(ConnectionResult result) {
//		/* Logout */
//		Log.d(TAG, "onConnectionFailed");
//		
//		Intent i = new Intent(this, LoginActivity.class);
//		startActivity(i);
//		finish();
//		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//	}
	
	private void setPage(int page){
		nowPage = page;
	}

	
	
//	class CustomArrayAdapter extends ArrayAdapter<String> {
//
//		public CustomArrayAdapter(Context context, int resource, String[] objects) {
//			super(context, resource, objects);
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			// TODO Auto-generated method stub
//			LayoutInflater inflater = getLayoutInflater();
//			View row = inflater.inflate(R.layout.row, parent, false);
//			TextView label = (TextView) row.findViewById(R.id.weekofday);
//			
//			return tv;
//		}
//	}
}