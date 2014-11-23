package com.colonycount.cklab.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;


public class ImgInfo {
	private int number;
	private Calendar calendar;
	private String type;
	private ArrayList<Map<String, Integer>> colonies = new ArrayList<Map<String, Integer>>();
	
	public ImgInfo(int number) {
		this.number = number;
		this.type = "";
		this.calendar = Calendar.getInstance();
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
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
		return calendar.get(Calendar.MONTH);
	}
	
	public int getDay(){
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public String getDate(){
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = format1.format(calendar.getTime());
		
		return formatted;
	}
	
	public void setDate(int year, int month, int day){
		this.calendar.set(Calendar.YEAR, year);
		this.calendar.set(Calendar.MONTH, month);
		this.calendar.set(Calendar.DAY_OF_MONTH, day);
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
