package com.colonycount.cklab.asynctask;

public interface AsyncTaskCompleteListener<T> {
	public void onTaskComplete(AsyncTaskPayload result, String taskName);
}
