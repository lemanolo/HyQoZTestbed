package org.lig.hadas.aesop.qwGeneration.model;



public class In extends Activity{



	private final ActivityType type = ActivityType.IN;
	
	public In(String id) {
		super(id);
	}
	public ActivityType getActivityType(){
		return this.type;
	}
	@Override
	public String toString() {
		return String.format("%s(id=%s)",this.type, super.getId());
	}
	
	public String toASMString(){
		return "init{\n}\n\nseq";
	}
	@Override
	public In clone() {
		return new In(this.getId());
	}
	
}
