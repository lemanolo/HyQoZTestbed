package org.lig.hadas.aesop.qwGeneration.model;

import com.mxgraph.io.gd.mxGdNode;
import com.mxgraph.util.mxPoint;

public class Node {
	private String id;
	public Node() {}
	
	public Node(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String toString(){
		return String.format("%s", this.id);
	}
	

	
	public mxGdNode getMXGdNode(){
		mxPoint coor = new mxPoint(0,0);
		mxPoint dims = new mxPoint(0,0);
		
		mxGdNode node = new mxGdNode(this.getId(), (mxPoint)coor.clone(), (mxPoint)dims.clone());
		return node;
	}
}