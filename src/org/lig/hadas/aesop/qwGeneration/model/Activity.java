package org.lig.hadas.aesop.qwGeneration.model;




public abstract class  Activity  extends Node{
	
	public Activity(String id) {
		super(id);
	}

	public boolean equals(Object o){
		Activity activity = (Activity)o;
		if(activity.getId().equals(super.getId()))
			return true;
		return false;
	}
	
	public abstract ActivityType getActivityType();
	public abstract String toASMString();
	
	public abstract Activity clone();

}