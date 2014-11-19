package com.colonycount.cklab.model;

import java.io.Serializable;
import java.util.List;

public class DataWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Component> parliaments;

   public DataWrapper(List<Component> data) {
      this.parliaments = data;
   }

   public List<Component> getParliaments() {
      return this.parliaments;
   }
}
