package org.lig.hadas.aesop.qwGeneration.model;

import java.util.ArrayList;
import java.util.Iterator;



public class FilterInvoke extends Activity implements UnaryDataOperation{



	private final ActivityType type = ActivityType.FILTER_INVOKE;


	private String serviceName = null;
	private String methodName  = null;
	private ArrayList<?> parameterList = null;

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

	private ArrayList<String> parameters = new ArrayList<String>();

	////UnaryDataOperation
	private String scope;
	private String unifiedName;

	@SuppressWarnings("unchecked")
	public FilterInvoke(String id, 
			String serviceName, String methodName, ArrayList<?> parameterList,
			String eqOperator, 
			String leftServiceName, String leftMethodName, String leftParamName,
			String rightServiceName, String rightMethodName, String rightParamName) {
		super(id);

		this.serviceName 		= serviceName;
		this.methodName  		= methodName;
		this.parameterList 		= parameterList;
		this.parameters.addAll((ArrayList<String>)this.parameterList);


		//		String tmp = this.parameterList.trim().substring(1);
		//		tmp = tmp.substring(0,tmp.lastIndexOf("]"));
		//		StringTokenizer tokenizer = new StringTokenizer(tmp,",");
		//		while(tokenizer.hasMoreElements()){
		//			this.parameters.add((String) tokenizer.nextElement());
		//		}


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

	public String getServiceName() {
		return serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public ArrayList<?> getParameterList() {
		return parameterList;
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
		String serviceInvocation=String.format("%s.%s(", this.serviceName,this.methodName);
		for (Iterator<String> iterator = this.parameters.iterator(); iterator.hasNext();) {
			String param = (String) iterator.next();
			serviceInvocation+=param;
			if(iterator.hasNext())
				serviceInvocation+=", ";
		}
		serviceInvocation+=")";

		return String.format("%s := comp.comp.funCallSelection(%s, %s %s %s) //%s", unifiedName, scope, serviceInvocation, this.eqOperator, this.rightParamName, this.toString());
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
	public FilterInvoke clone() {
		return new FilterInvoke(this.getId(), this.serviceName, this.methodName, this.parameterList, this.eqOperator, this.leftServiceName, this.leftMethodName, this.leftParamName, this.rightServiceName, this.rightMethodName, this.rightParamName);
	}
}
