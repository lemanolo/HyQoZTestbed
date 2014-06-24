package org.lig.hadas.aesop.qwGeneration.model;



public class RenameParameter extends Activity implements UnaryDataOperation{
	private final ActivityType type = ActivityType.RENAME_PARAM;

	private String serviceName=null;
	private String methodName=null;
	private String parameterName = null;
	private String alias = null;

	///UnaryDataOperation
	private String scope;
	private String unifiedName;

	public RenameParameter(String id, String serviceName, String methodName, String parameterName, String alias) {
		super(id);
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.parameterName = parameterName;
		this.alias = alias;

	}
	public String getServiceName() {
		return serviceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public String getParameterName() {
		return parameterName;
	}
	public String getAlias() {
		return alias;
	}
	
	public ActivityType getActivityType(){
		return this.type;
	}
	
	@Override
	public String toString() {
		return String.format("%s(id=%s, serviceName=%s, methodName=%s, parameterName=%s,alias=%s)", 
				this.type, super.getId(), this.serviceName,this.methodName,this.parameterName,this.alias);
	}
	@Override
	public String toASMString() {
		return String.format("// %s := comp.renameParameter(%s.%s,%s) //%s", this.alias, this.unifiedName,this.parameterName, this.alias, this.toString()); //TODO 
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
	public RenameParameter clone() {
		return new RenameParameter(this.getId(), this.serviceName, this.methodName, this.parameterName, this.alias);
	}
}
