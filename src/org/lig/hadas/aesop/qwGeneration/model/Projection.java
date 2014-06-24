package org.lig.hadas.aesop.qwGeneration.model;

import java.util.ArrayList;
import java.util.Iterator;



public class Projection extends Activity implements UnaryDataOperation{
	private final ActivityType type = ActivityType.PROJECTION;
	
	private ArrayList<?> projectionList=null;


	///UnaryDataOperation
	private String scope;
	private String unifiedName;
	
	public Projection(String id, ArrayList<?> projectionList) {
		super(id);
		this.projectionList = projectionList;

	}

	
	public ArrayList<?> getProjectionList() {
		return this.projectionList;
	}
	
	public ActivityType getActivityType(){
		return this.type;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		String paramList="";

		for (Iterator<?> iterator = (Iterator<?>) this.projectionList.iterator(); iterator.hasNext();) {
			ArrayList<String> al = (ArrayList<String>) iterator.next();
			paramList+=String.format("[serviceName=%s, methodName=%s, parameterName=%s]", al.get(0),al.get(1),al.get(2));
			if(iterator.hasNext())
				paramList+=", ";
		}
		return String.format("%s(id=%s, [%s])", 
				this.type, super.getId(), paramList);
	}
	@SuppressWarnings("unchecked")
	@Override
	public String toASMString() {
		
		String paramList="";
		for (Iterator<?> iterator = (Iterator<?>) this.projectionList.iterator(); iterator.hasNext();) {
			ArrayList<String> al = (ArrayList<String>) iterator.next();
			paramList+=String.format("%s.%s.%s", al.get(0),al.get(1),al.get(2));

			if(iterator.hasNext())
				paramList+=", ";
		}
		return String.format("%s := %s(%s, %s) //%s", this.unifiedName, super.getId(), this.scope, paramList, this.toString()); //TODO 
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
	public Projection clone() {
		return new Projection(this.getId(), this.projectionList);
	}
}
