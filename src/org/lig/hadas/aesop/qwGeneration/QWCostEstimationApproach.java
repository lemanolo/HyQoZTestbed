package org.lig.hadas.aesop.qwGeneration;

public enum QWCostEstimationApproach {
	PARTIALCF("partialcf"),
	FULLCF("fullcf"),
	DF("df"),
	UNREQUIRED("unrequired");
	
	private String estimationApproach;
	private QWCostEstimationApproach(String estimationApproach) {
		this.estimationApproach = estimationApproach;
	}
	public String getEstimationApproach(){
		return this.estimationApproach;
	}
	@Override
	public String toString() {
		return this.estimationApproach;
	}
	
}
