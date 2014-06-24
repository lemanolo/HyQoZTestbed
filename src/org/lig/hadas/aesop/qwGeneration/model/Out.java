package org.lig.hadas.aesop.qwGeneration.model;



public class Out extends Activity{



	private final ActivityType type = ActivityType.OUT;
	
	public Out(String id) {
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
		return "endseq";
	}
	@Override
	public Out clone() {
		return new Out(this.getId());
	}
}
