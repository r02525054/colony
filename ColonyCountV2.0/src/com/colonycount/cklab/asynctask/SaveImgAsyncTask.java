package com.colonycount.cklab.asynctask;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.colonycount.cklab.base.BaseAsyncTask;
import com.colonycount.cklab.model.ImgInfo;

public class SaveImgAsyncTask extends BaseAsyncTask {
	private String filePath_raw;
	private String filePath_count;
	private String fileName_raw;
	private String fileName_conut;
	
	private byte[] data_raw;
	private byte[] data_count;
	private String user_account;
	private String user_id;
	private ImgInfo imgInfo;
	
	public SaveImgAsyncTask(Context context, String progressDialogTitle, String progressDialogMsg, AsyncTaskCompleteListener<Boolean> listener, Class<?> cls, boolean showDialog, byte[] data_raw, byte[] data_counted, String user_account, String user_id, ImgInfo imgInfo) {
		super(context, progressDialogTitle, progressDialogMsg, listener, cls, showDialog);
		this.data_raw = data_raw;
		this.data_count = data_counted;
		this.user_account = user_account;
		this.user_id = user_id;
		this.imgInfo = imgInfo;
	}

	@Override
	protected AsyncTaskPayload doInBackground(AsyncTaskPayload... params) {
		AsyncTaskPayload result = new AsyncTaskPayload();
		result.putValue("result", "success");
		
//		if(data_raw != null){
//			if(!saveColonyImage(data_raw, "raw"))
//				result.putValue("result", "fail");
//			if(!saveColonyImage(data_count, "count"));
//				result.putValue("result", "fail");
//		}
		
		if(!uploadColonyImage(data_raw, data_count))
			result.putValue("result", "fail");
			
		if(!insertData2DB())
			result.putValue("result", "fail");
		
		return result;
	}
	
//	private boolean saveColonyImage(byte[] data, String type){
//		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ColonyCount");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d(TAG, "failed to create directory");
//                return false;
//            }
//        }
//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN).format(new Date());
//        File pictureFile = null;
//        if(type.equals("raw")) {
//        	filePath_raw = mediaStorageDir.getPath() + File.separator + timeStamp + "_" + type + ".jpg";
//        	pictureFile = new File(filePath_raw);
//        } else if(type.equals("count")){
//        	filePath_count = mediaStorageDir.getPath() + File.separator + timeStamp + "_" + type + ".jpg";
//        	pictureFile = new File(filePath_count);
//        }
//        
//        if(pictureFile == null){
//        	Log.d(TAG, "Error creating media file, check storage permissions: ");
//        	return false;
//        } 
//        
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "File not found: " + e.getMessage());
//            return false;
//        } catch (IOException e) {
//            Log.d(TAG, "Error accessing file: " + e.getMessage());
//            return false;
//        }
//        
//        Log.d(TAG, "save colony image successfully!");
//        return true;
//	}
	
	private boolean uploadColonyImage(byte[] data_raw, byte[] date_count){
		String uploadServerUri = "http://140.112.26.221/~master11360/colony%20count/php/upload_test.php";
		
//		String uploadServerUri = "http://140.112.26.221/~master11360/colony%20count/php/db_connect.php";
        
        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String lineEnd = "\r\n";
        String delimiter = "--";
        String boundary = "*****";
        
        try {
        	ByteArrayInputStream baInputStream_raw = new ByteArrayInputStream(data_raw);
        	ByteArrayInputStream baInputStream_count = new ByteArrayInputStream(data_count);
        	
        	// open a URL connection to server
			URL url = new URL(uploadServerUri);
			conn = (HttpURLConnection) url.openConnection(); // Open a HTTP connection to the URL
			
			connectForMultipart(conn, url, boundary);
			dos = new DataOutputStream(conn.getOutputStream());
			// set post field
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN).format(new Date());
			fileName_raw = timeStamp + "_raw.jpg";
			fileName_conut = timeStamp + "_count.jpg";
			
			addFormPart("upload_image", "upload_image", delimiter, boundary, lineEnd, dos);
			addFormPart("user_account", user_account, delimiter, boundary, lineEnd, dos);
			addFormPart("user_id", user_id, delimiter, boundary, lineEnd, dos);
			addFilePart("file_raw", fileName_raw, delimiter, boundary, lineEnd, dos, baInputStream_raw);
			addFilePart("file_count", fileName_conut, delimiter, boundary, lineEnd, dos, baInputStream_count);
			
			// Responses from the server (code and message)
			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();
			
			Log.d(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
			if (serverResponseCode == 200) {
				String serverResponse = convertResponseToString(conn);
				Log.d(TAG, "success, response: " + serverResponse);
			} else {
				
				return false;
			}
			
			// close the streams //
//			fileInputStream_raw.close();
//			fileInputStream_count.close();
			
			baInputStream_raw.close();
			baInputStream_count.close();
			dos.flush();
			dos.close();
        } catch (MalformedURLException ex) {  
            Log.e(TAG, "Upload file to server error: " + ex.getMessage(), ex);  
        } catch (Exception e) {
            Log.e(TAG, "Upload file to server Exception : " + e.getMessage(), e);  
        }
        
		return true;
	}
	
	private boolean insertData2DB(){
		String postUrl = "http://140.112.26.221/~master11360/colony%20count/php/db_connect.php";
		HttpPost httpRequest = new HttpPost(postUrl);
		List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
		
		// TODO: check bug
		// to prevent bug: params still add on last request params
		reqParams.clear();
		addRequest(reqParams);
		String strResult = null;
		
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(reqParams, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			strResult = EntityUtils.toString(httpResponse.getEntity());
			Log.d("Test2", "post result = " + strResult);
			
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				Log.d("Test2", "success post to DB");
//				String temp[] = strResult.split(";");
//				
//				for(int i = 0; i < temp.length; i++){
//					Log.d("Test2", temp[i]);
//				}
			} else {
				Log.d("Test2", "not 200");
				Log.d("Test2", "getStatusCode = " + httpResponse.getStatusLine().getStatusCode());
			}
		} catch(Exception e) {
			Log.d(TAG, "exception: " + e.toString());
		}
		
		return true;
	}
	
	public void addRequest(List<NameValuePair> reqParams){
		reqParams.add(new BasicNameValuePair("insert_data", "true"));
		
		// image params
		reqParams.add(new BasicNameValuePair("user_id", user_id));
		reqParams.add(new BasicNameValuePair("img_name_count", fileName_conut));
		reqParams.add(new BasicNameValuePair("img_name_raw", fileName_raw));
		
		// tag params
//		reqParams.add(new BasicNameValuePair("tag_number", imgInfo.getNumber()+""));
		reqParams.add(new BasicNameValuePair("tag_date", imgInfo.getDateString()));
		reqParams.add(new BasicNameValuePair("tag_type", imgInfo.getType()));
		
		// colony params
		ArrayList<Map<String, Integer>> colonies = imgInfo.getColonies();
		StringBuilder colonyParam = new StringBuilder();
		for(int i = 0; i < colonies.size(); i++){
			Map<String, Integer> colony = colonies.get(i);
			if(i != colonies.size() - 1)
				colonyParam.append(colony.get("x") + "," + colony.get("y") + "," + colony.get("r") + "," + colony.get("type") + ";");
			else
				colonyParam.append(colony.get("x") + "," + colony.get("y") + "," + colony.get("r") + "," + colony.get("type"));
		}
		reqParams.add(new BasicNameValuePair("colony_param", colonyParam.toString()));
	}
	
	public void connectForMultipart(HttpURLConnection con, URL url, String boundary) throws Exception {
		con.setDoInput(true); // Allow Inputs
		con.setDoOutput(true); // Allow Outputs
		con.setUseCaches(false); // Don't use a Cached Copy
		con.setRequestMethod("POST");
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("ENCTYPE", "multipart/form-data");
		con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	}
	
	
	private void addFormPart(String paramName, String value, String delimiter, String boundary, String lineEnd, DataOutputStream dos) throws Exception {
		dos.writeBytes((delimiter + boundary + lineEnd));
		dos.writeBytes("Content-Type: text/plain" + lineEnd);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"" + lineEnd);
		dos.writeBytes(lineEnd + value + lineEnd);
	}
	
//	public void addFilePart(String paramName, String fileName, String delimiter, String boundary, String lineEnd, DataOutputStream dos, FileInputStream fileInputStream) throws Exception {
//		dos.writeBytes(delimiter + boundary + lineEnd);
//		dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"" + lineEnd);
//		dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
//		dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
//		dos.writeBytes(lineEnd);
//		
//		int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024; 
//		
//        bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
//        bufferSize = Math.min(bytesAvailable, maxBufferSize);
//        buffer = new byte[bufferSize];
//		
//     // read file and write it into form...
//        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//        
//        while (bytesRead > 0) {
//        	Log.d("Test2", "white bytes");
//            dos.write(buffer, 0, bufferSize);
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);               
//        }
//         
//        // send multipart form data necesssary after file data...
//        dos.writeBytes(lineEnd);
//        dos.writeBytes(delimiter + boundary + delimiter + lineEnd);
//	}
	
	
	public void addFilePart(String paramName, String fileName, String delimiter, String boundary, String lineEnd, DataOutputStream dos, ByteArrayInputStream byteArrayInputStream) throws Exception {
		dos.writeBytes(delimiter + boundary + lineEnd);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName +  "\"; filename=\"" + fileName + "\"" + lineEnd);
		dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
		dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
		dos.writeBytes(lineEnd);
		
		int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024; 
		
        bytesAvailable = byteArrayInputStream.available(); // create a buffer of  maximum size
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];
		
     // read file and write it into form...
        bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
        
        while (bytesRead > 0) {
        	Log.d("Test2", "white bytes");
            dos.write(buffer, 0, bufferSize);
            bytesAvailable = byteArrayInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);               
        }
         
        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(delimiter + boundary + delimiter + lineEnd);
	}
	
	
	public String convertResponseToString(HttpURLConnection conn) throws IllegalStateException, IOException{
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = conn.getInputStream();
        int contentLength = conn.getContentLength();
             
        if (contentLength < 0){
        	
        } else {
           byte[] data = new byte[512];
           int len = 0;
           try {
               while (-1 != (len = inputStream.read(data))) {
                   buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer�K..
               }
               inputStream.close(); // closing the stream�K..
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        
        return buffer.toString();
   }
}
