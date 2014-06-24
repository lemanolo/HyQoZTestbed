package org.lig.hadas.aesop.qwGeneration.model;



public class TupleWindow extends Activity implements UnaryDataOperation{

	private final ActivityType type = ActivityType.TUPLE_WINDOW;
	private String serviceName = null;
	private String methodName = null;
	private String windowSize = null;

	///UnaryDataOperation
	private String scope;
	private String unifiedName = null;

	public TupleWindow(String id, String unifiedName, String serviceName, String methodName, String windowSize) {
		super(id);
		this.unifiedName = unifiedName;
		this.serviceName = serviceName;
		this.methodName  = methodName;
		this.windowSize  = windowSize;
	}
	
	public String getWindowSize() {
		return windowSize;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public ActivityType getActivityType(){
		return this.type;
	}
	
	@Override
	public String toString() {
		return String.format("%s(id=%s, serviceName=%s, methodName=%s, windowSize=%s)",this.type, super.getId(), this.serviceName, this.methodName, this.windowSize);
	}

	@Override
	public String toASMString() {
		return String.format("%s := comp.tupleWindow(%s,%s) //%s",this.unifiedName, this.scope,this.windowSize, this.toString()); 
	}
	
	@Override
	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String getScope() {
		return this.scope;
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
	public TupleWindow clone() {
		return new TupleWindow(this.getId(), this.unifiedName, this.serviceName, this.methodName, this.windowSize);
	}
	
}
