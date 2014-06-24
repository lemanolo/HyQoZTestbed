package org.lig.hadas.aesop.qwGeneration.model;



public class Filter extends Activity implements UnaryDataOperation{



	private final ActivityType type = ActivityType.FILTER;



	private String eqOperator=null;
	private String leftServiceName = null;
	private String leftMethodName = null;
	private String leftParamName = null;
	private String rightServiceName = null;
	private String rightMethodName = null;
	private String rightParamName = null;

	@SuppressWarnings("unused")
	private boolean leftSideIsConstant = false;
	@SuppressWarnings("unused")
	private boolean rightSideIsConstant = false;


	////UnaryDataOperation
	private String scope;
	private String unifiedName;

	public Filter(String id, String eqOperator, 
			String leftServiceName, String leftMethodName, String leftParamName,
			String rightServiceName, String rightMethodName, String rightParamName) {
		super(id);
		this.eqOperator 		= eqOperator.replaceFirst("\\(", "").replaceFirst("\\)", "");
		this.leftServiceName 	= leftServiceName; 
		this.leftMethodName 	= leftMethodName;  
		this.leftParamName 		= leftParamName;   
		this.rightServiceName 	= rightServiceName;
		this.rightMethodName 	= rightMethodName; 
		this.rightParamName 	= rightParamName;  


		this.leftSideIsConstant	 = leftServiceName.equals("null")  && leftMethodName.equals("null");

		this.rightSideIsConstant = rightServiceName.equals("null") && rightMethodName.equals("null");
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
	public ActivityType getActivityType(){
		return this.type;
	}
	@Override
	public String toASMString() {
		
		return String.format("%s := comp.selection(%s, %s %s %s) //%s", unifiedName, scope, this.leftParamName, this.eqOperator, this.rightParamName, this.toString());
	}

	@Override
	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public void setUnifiedName(String uname) {
		this.unifiedName = uname;
	}

	@Override
	public String getUnifiedName() {
		return this.unifiedName;
	}

	@Override
	public Filter clone() {
		return new Filter(this.getId(), this.eqOperator, this.leftServiceName, this.leftMethodName, this.leftParamName, this.rightServiceName, this.rightMethodName, this.rightParamName);
	}
}
