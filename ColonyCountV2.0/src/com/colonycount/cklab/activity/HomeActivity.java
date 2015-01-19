package com.colonycount.cklab.activity;

import java.util.Set;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.colonycount.cklab.asynctask.AsyncTaskCompleteListener;
import com.colonycount.cklab.asynctask.AsyncTaskPayload;
import com.colonycount.cklab.asynctask.GetImgAsyncTask;
import com.colonycount.cklab.base.GPlusClientActivity;
import com.colonycount.cklab.fragment.FragmentDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.Plus;

public class HomeActivity extends GPlusClientActivity implements AsyncTaskCompleteListener<Boolean> {
	private DrawerLayout layDrawer;
    private ListView lstDrawer;
    
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    private int nowPage;
    
    private FragmentHome    fragmentHome;
    private FragmentSetting fragmentSettings; 
    
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
        	if(fragmentHome == null)
        		fragmentHome = new FragmentHome(loadPrefStringData(USER_ID), this); 
        	
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentHome).commit();
			String title = getResources().getStringArray(R.array.drawer_menu)[0];
			mTitle = title;
		    getActionBar().setTitle(title);
		    nowPage = 0;
		}
        
        new GetImgAsyncTask(this, "", "", this, GetImgAsyncTask.class, false).execute();
        
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
                FragmentHome.setMenuVisible(true);
                
                supportInvalidateOptionsMenu(); 
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                FragmentHome.setMenuVisible(false);
                
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
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		String title = getResources().getStringArray(R.array.drawer_menu)[position];
		
//		TextView tv = (TextView) lstDrawer.getChildAt(position);
//		tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
		
	    switch (position) {
	    case 0:
	    	if(nowPage != 0){
	    		if(fragmentHome == null)
	    			fragmentHome = new FragmentHome(loadPrefStringData(USER_ID), this);
			    // -----
			    fragmentTransaction.replace(R.id.content_frame, fragmentHome);
//			    fragmentTransaction.addToBackStack("home");
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
	    		if(fragmentSettings == null)
	    			fragmentSettings = new FragmentSetting();
		        
			    fragmentTransaction.replace(R.id.content_frame, fragmentSettings);
//			    fragmentTransaction.addToBackStack("home");
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
//	    	Log.d("test", "user account = " + loadPrefStringData(USER_ACCOUNT));
//	    	Log.d("test", "user id = " + loadPrefStringData(USER_ID));
	    	
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
	        return;
	    }
	}

	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/* Logout */
		Log.d(TAG, "onConnectionFailed");
		
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	private void setPage(int page){
		nowPage = page;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	public Set<String> loadPrefStringSetData(String key){
		return super.loadPrefStringSetData(key);
	}
	
	@Override
	public void onTaskComplete(AsyncTaskPayload result, String taskName) {
		String resultStr = result.getValue("result");
		if(taskName.equals("GetImgAsyncTask")){
			if(resultStr.equals("success")){
				
				// TODO: add gridView data after selecting DB
				// 
				
//				fragmentHome.setGridViewData(date, type, dilution_num, exp_param, colony_num, images);
				
				fragmentHome.setGridViewData(null, null, null, null, null, null);
				fragmentHome.setGridView();
			}
			else
				fragmentHome.setHomeMsg("您尚未擁有任何菌落資料，請選擇「拍攝菌落」或「選取照片」開始計算菌落!");
		}
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