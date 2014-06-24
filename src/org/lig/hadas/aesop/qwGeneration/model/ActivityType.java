package org.lig.hadas.aesop.qwGeneration.model;

public enum ActivityType {

	BATCH_RETRIEVAL("batch"), //ok
	STREAM_SUBSCRIPTION("stream"), //ok
	PROJECTION("project"), //ok
	FILTER("filter"), //ok
	FILTER_INVOKE("filter_invoke"),
	RENAME_PARAM("rename_param"), //ok
	RENAME_SERVICE("rename_service"), 
	JOIN("join"), //ok
	BIND_JOIN("bind_join"), //ok
	TUPLE_WINDOW("tuple_window"), //ok
	TIME_WINDOW("time_window"), //ok

	IN("in"), //ok
	OUT("out"), //ok
	PAR("par"), //ok
	ENDPAR("endpar"); //ok

	private String type= null;

	private ActivityType(String type) {
		this.type = type;

	}

	@Override
	public String toString(){
		return this.type;
	}

	public String getType(){
		return type;
	}

	public boolean isDataOperator(){
		if(this 	 == BATCH_RETRIEVAL ||
				this == STREAM_SUBSCRIPTION ||
				this == PROJECTION ||
				this == FILTER ||
				this == FILTER_INVOKE ||
				this == RENAME_PARAM ||
				this == RENAME_SERVICE ||
				this == JOIN ||
				this == BIND_JOIN ||
				this == TUPLE_WINDOW ||
				this == TIME_WINDOW){
			return  true;
		}
		return false;
	}
	public boolean isControlFlowOperator(){
		if(this 	 == IN ||
				this == OUT ||
				this == PAR ||
				this == ENDPAR
				){
			return  true;
		}
		return false;
	}
	public boolean isDataRetrieval(){
		if(this 	 == BATCH_RETRIEVAL ||
				this == STREAM_SUBSCRIPTION
				){
			return  true;
		}
		return false;
	}
	public boolean isDataCorrelation(){
		if(this 	 == JOIN || this == BIND_JOIN ){
			return  true;
		}
		return false;
	}

}
