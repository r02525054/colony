package com.colonycount.cklab.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;


public class ImgInfo {
	private Calendar calendar;
	private String type;
	private int dilutionNumber;
	private String expParam;
	private ArrayList<Map<String, Integer>> colonies; 
	
	
	public ImgInfo() {
		this.calendar = Calendar.getInstance();
		this.type = "";
		this.dilutionNumber = -1;
		this.expParam = "";
		colonies = new ArrayList<Map<String, Integer>>();
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getYear(){
		return calendar.get(Calendar.YEAR);
	}
	
	public int getMonth(){
		return calendar.get(Calendar.MONTH)+1;
	}
	
	public int getDay(){
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public String getDateString(){
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
		String formatted = format1.format(calendar.getTime());
		
		return formatted;
	}
	
	public Date getDate(){
		Date date = calendar.getTime();
		return date;
	}
	
	public void setDate(int year, int month, int day){
		this.calendar.set(Calendar.YEAR, year);
		this.calendar.set(Calendar.MONTH, month-1);
		this.calendar.set(Calendar.DAY_OF_MONTH, day);
	}
	
	public void setDate(Calendar c){
		this.calendar = c;
	}
	
	public int getDilutionNumber() {
		return dilutionNumber;
	}

	public void setDilutionNumber(int dilutionNumber) {
		this.dilutionNumber = dilutionNumber;
	}

	public String getExpParam() {
		return expParam;
	}

	public void setExpParam(String expParam) {
		this.expParam = expParam;
	}
	
	public void addColony(float x, float y, float r, int type){
		Map<String, Integer> colony = new HashMap<String, Integer>();
		
		if(Math.round(r) < 1)
			Log.d("Test2", x + "," + y + "," + r + "," + type);
			
		colony.put("x", (int)x);
		colony.put("y", (int)y);
		colony.put("r", Math.round(r));
		colony.put("type", type);
		
		colonies.add(colony);
	}
	
	public ArrayList<Map<String, Integer>> getColonies(){
		return colonies;
	}
}
