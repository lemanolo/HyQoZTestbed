package org.lig.hadas.aesop.qwGeneration.model;



public class StreamSubscription extends Activity implements UnaryDataOperation{
	private final ActivityType type = ActivityType.STREAM_SUBSCRIPTION;
	
	private String serviceName=null;
	private String methodName=null;
	
	///UnaryDataOperation
	private String unifiedName=null;
	private String scope;

	public StreamSubscription(String id, String unifiedName, String serviceName, String methodName) {
		super(id);
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.unifiedName = unifiedName;

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
		return String.format("%s(id=%s, serviceName=%s, methodName=%s)", 
				this.type, super.getId(), this.serviceName,this.methodName);
	}
	@Override
	public String toASMString() {
		
		return String.format("%s := %s.%s() //%s", this.unifiedName, this.serviceName, this.methodName,this.toString());
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
	public StreamSubscription clone() {
		return new StreamSubscription(this.getId(), this.unifiedName, this.serviceName, this.methodName);
	}
}
