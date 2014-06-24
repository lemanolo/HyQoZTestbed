package org.lig.hadas.aesop.qwGeneration;

import org.apache.commons.codec.digest.DigestUtils;

public class QueryWorkflowContext{
	private String id=null;
	private String signature=null;
	public QueryWorkflowContext(String qwSignature) {
		this.signature = qwSignature;
		this.id = DigestUtils.md5Hex(this.signature);
	}
	public String getSignature(){
		return this.signature;
	}
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryWorkflowContext other = (QueryWorkflowContext) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

}
