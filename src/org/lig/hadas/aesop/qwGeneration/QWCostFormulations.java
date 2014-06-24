package org.lig.hadas.aesop.qwGeneration;

public enum QWCostFormulations {
	RUNTIME("runtime"),
	BUILDTIME("buildtime");
	private String formulation;
	QWCostFormulations(String formulation) {
		this.formulation = formulation;
	}
	public String getFormulation(){
		return this.formulation;
	}
	
}
