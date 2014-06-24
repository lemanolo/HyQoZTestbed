package org.lig.hadas.aesop.qwGeneration.model;



public class Parallel extends Activity{



	private final ActivityType type = ActivityType.PAR;
	
	public Parallel(String id) {
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
		return "par";
	}
	@Override
	public Parallel clone() {
		return new Parallel(this.getId());
	}
}
