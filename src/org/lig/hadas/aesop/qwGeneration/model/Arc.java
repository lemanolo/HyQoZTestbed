package org.lig.hadas.aesop.qwGeneration.model;


import com.mxgraph.io.gd.mxGdEdge;


public class Arc extends Node{


	private Activity left;
	private Activity right;
	

	public Arc() {
		super("arc");
	}
	
	public Arc(Activity left, Activity right) {
		super("arc");
		this.left	= left;
		this.right	= right;
	}

	public Activity getLeft() {
		return left;
	}

	public void setLeft(Activity left) {
		this.left = left;
	}

	public Activity getRight() {
		return right;
	}

	public void setRight(Activity right) {
		this.right = right;
	}
	
	public String toString(){
		return String.format("%s(%s, %s)", super.toString(), this.left.toString(), this.right.toString());
	}

	public String toStringShort(){
		return String.format("%s(%s, %s)", super.toString(), this.left.getId(), this.right.getId());
	}
	
	public mxGdEdge getMXGdEdge(){
		mxGdEdge arc = new mxGdEdge(left.getId(), right.getId());
		return arc;
	}
}
