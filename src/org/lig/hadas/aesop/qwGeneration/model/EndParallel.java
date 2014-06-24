package org.lig.hadas.aesop.qwGeneration.model;



public class EndParallel extends Activity{



	private final ActivityType type = ActivityType.ENDPAR;
	
	public EndParallel(String id) {
		super(id);
	}
	public ActivityType getActivityType(){
		return this.type;
	}
	@Override
	public String toString() {
		return String.format("%s(id=%s)",this.type, super.getId());
		
	}

	@Override
	public String toASMString() {
		return "endpar";
	}
	@Override
	public EndParallel clone() {
		return new EndParallel(this.getId());
	}
	
}
