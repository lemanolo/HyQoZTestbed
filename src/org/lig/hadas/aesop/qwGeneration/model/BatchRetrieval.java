package org.lig.hadas.aesop.qwGeneration.model;



public class BatchRetrieval extends Activity implements UnaryDataOperation{
	private final ActivityType type = ActivityType.BATCH_RETRIEVAL;
	
	private String serviceName=null;
	private String methodName=null;
	
	///UnaryDataOperation
	private String unifiedName=null;
	private String scope;



	private String boundedAttribute;

	private String value;

	public BatchRetrieval(String id, String unifiedName, String serviceName, String methodName, String boundedAttribute, String value) {
		super(id);
		this.serviceName = serviceName;
		this.methodName  = methodName;
		this.unifiedName = unifiedName;
		this.boundedAttribute = boundedAttribute;
		this.value = value;

	}

	public ActivityType getActivityType(){
		return this.type;
	}

	@Override
	public String toString() {
		return String.format("%s(id=%s, serviceName=%s, methodName=%s, boundedAtt=%s, value= %s)", 
				this.type, super.getId(), this.serviceName,this.methodName, this.boundedAttribute, this.value);
	}

	public String toASMString() {
		
		return String.format("%s := %s.%s() //%s", this.unifiedName,this.serviceName,this.methodName,this.toString()); //TODO unifiedName
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
	
	public String getServiceName() {
		return serviceName;
	}
	public String getMethodName() {
		return methodName;
	}
	
	public String getBoundedAttribute() {
		return boundedAttribute;
	}
	public String getValue() {
		return value;
	}

	@Override
	public BatchRetrieval clone() {
		return new BatchRetrieval(this.getId(), this.unifiedName, this.serviceName, this.methodName, this.boundedAttribute, this.value);
	}
	
}
