package org.lig.hadas.aesop.qwGeneration.model;



public class BindJoin extends Activity implements BinaryDataOperation{



	private final ActivityType type = ActivityType.BIND_JOIN;



	private String unifiedName=null;
	private String eqOperator=null;
	private String leftServiceName = null;
	private String leftMethodName = null;
	private String leftParamName = null;
	private String rightServiceName = null;
	private String rightMethodName = null;
	private String rightParamName = null;

	///BinaryDataOperation
	private String scope1;
	private String scope2;

	public BindJoin(String id, String unifiedName, String eqOperator, 
			String leftServiceName, String leftMethodName, String leftParamName,
			String rightServiceName, String rightMethodName, String rightParamName) {
		super(id);
		this.unifiedName 		= unifiedName; 
		this.eqOperator 		= eqOperator.replaceFirst("\\(", "").replaceFirst("\\)", "");
		this.leftServiceName 	= leftServiceName; 
		this.leftMethodName 	= leftMethodName;  
		this.leftParamName 		= leftParamName;   
		this.rightServiceName 	= rightServiceName;
		this.rightMethodName 	= rightMethodName; 
		this.rightParamName 	= rightParamName;  

	}

	public String getEqOperator() {
		return eqOperator;
	}



	public String getLeftServiceName() {
		return leftServiceName;
	}

	public String getLeftMethodName() {
		return leftMethodName;
	}

	public String getLeftParamName() {
		return leftParamName;
	}

	public String getRightServiceName() {
		return rightServiceName;
	}

	public String getRightMethodName() {
		return rightMethodName;
	}

	public String getRightParamName() {
		return rightParamName;
	}
	public ActivityType getActivityType(){
		return this.type;
	}
	@Override
	public String toString() {
		return String.format("%s(id=%s, eqOperator=%s, " +
				"leftServiceName=%s, leftMethodName=%s, leftParamName=%s," +
				"rightServiceName=%s, rightMethodName=%s, rightParamName=%s)", 
				this.type, super.getId(), this.eqOperator, 		
				this.leftServiceName, 	
				this.leftMethodName, 	
				this.leftParamName, 		
				this.rightServiceName, 	
				this.rightMethodName, 	
				this.rightParamName 	);
		
	}

	@Override
	public String toASMString() {
		return String.format("%s := comp.bindJoin(%s, %s, %s %s %s) //%s", this.unifiedName, this.scope1, this.scope2, this.leftParamName, this.eqOperator ,this.rightParamName, this.toString());
	}
	@Override
	public void setScope(String uname1, String uname2) {
		this.scope1 = uname1;
		this.scope2 = uname2;
	}
	@Override
	public String[] getScope() {
		return new String [] {this.scope1,this.scope2};
	}
	@Override
	public void setUnifiedName(String unifiedName) {
		this.unifiedName = unifiedName;
	}
	@Override
	public String getUnifiedName() {
		return unifiedName;
	}

	@Override
	public BindJoin clone() {
		
		return new BindJoin(this.getId(), this.unifiedName, this.eqOperator, this.leftServiceName, this.leftMethodName, this.leftParamName, this.rightServiceName, this.rightMethodName, this.rightParamName);
	}
}
