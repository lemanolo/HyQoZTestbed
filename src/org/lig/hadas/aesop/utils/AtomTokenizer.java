package org.lig.hadas.aesop.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class AtomTokenizer  implements Enumeration<Object>{
	private int idx = 0;
	private String atomName;
	private String atom;
	private int parentheses=0;
	private boolean moreElements=false;
	private Object nextElement=null;

	public AtomTokenizer(String atom) {
		this.atom = atom;
		int i=0;
		if(( i = this.atom.indexOf('('))>0){
			this.parentheses++;
			this.idx = i;
		}
		else
			this.idx=this.atom.length();

		this.atomName = this.atom.substring(0, this.idx);
		this.idx++;
		this.moveToNextElement();

	}

	private synchronized void moveToNextElement(){
		int square_brackets=0;
		this.moreElements=false;
		this.nextElement = null;
		int initIdx=idx;
		boolean is_a_list = false;
		for (; idx <this.atom.length() ; idx ++){
			if(this.parentheses==0){
				this.moreElements=false;
				break;
			}
			switch (this.atom.charAt(idx)) {
			case '[':
				is_a_list=true;
				square_brackets++;
				break;
			case ']':
				square_brackets--;
				break;
			case'(':
				this.parentheses++;
				break;
			case')':
				this.parentheses--;
				if(this.parentheses==0 && square_brackets ==0)
					this.moreElements=true;
				break;
			case ',':
				if(square_brackets ==0)
					this.moreElements=true;
				break;

			}
			if(this.moreElements==true)
				break;
		}
		if(this.moreElements){
			if(is_a_list){
				System.out.println("in atomTokenizer: "+atom.substring(initIdx, idx));

				this.nextElement=this.toArrayList(atom.substring(initIdx, idx));
				printList((ArrayList<?>)this.nextElement,0);
				is_a_list=false;
			}
			else{
				this.nextElement = atom.substring(initIdx, idx);
			}
			idx++;
		}
		else
			this.nextElement = null;

	}
	private void printList(ArrayList<?> al2, int indent) {
		String tabs="";
		for(int i=indent;i>0;i--)
			tabs+="\t";

		for (Iterator<?> iterator = al2.iterator(); iterator
				.hasNext();) {
			Object obj = (Object) iterator.next();
			if (obj instanceof String) {
				String str = (String) obj;
				System.out.println(tabs+str);
			}else if (obj instanceof ArrayList<?>) {
				ArrayList<?> al = (ArrayList<?>) obj;
				printList(al,indent+1);
			}

		}

	}

	//	@Override
	//	public String nextElement() {
	//		if(this.hasMoreElements()){
	//			String ret = this.nextElement;
	//			moveToNextElement();
	//			return ret;
	//		}
	//		return null;
	//	}
	@Override
	public Object nextElement() {
		if(this.hasMoreElements()){
			Object ret = this.nextElement;
			moveToNextElement();
			return ret;
		}
		return null;
	}

	private ArrayList<Object> toArrayList(String listString){
		listString=listString.trim();
		int square_brackets=0;

		ArrayList<Object> resulted_al= new ArrayList<Object>();
		String buffer="";
		for(int i=0;i<listString.length();i++){
			char c = listString.charAt(i);
			switch (c) {
			case '[':
				square_brackets++;
				if(square_brackets>1)
					buffer+=c;
				break;
			case ']':
				square_brackets--;
				if(square_brackets==0){
					if(!buffer.equals("")){
						resulted_al.add(buffer);
						buffer="";
					}
				}else{
					buffer+=c;
					ArrayList<Object> al = toArrayList(buffer);
					resulted_al.add(al);
					buffer="";
				}
				break;
			case ',':
				if(square_brackets<=1){
					if(!buffer.equals("")){
						resulted_al.add(buffer);
						buffer="";
					}
				}else
					buffer+=c;
				break;
			default:
				buffer+=c;
				break;
			}
		}
		return resulted_al;
	}

	@Override
	public boolean hasMoreElements() {
		return this.moreElements;
	}

	public String getAtomName() {
		return this.atomName;
	}
	public static void main(String[] args) {
		AtomTokenizer tokenizer = new AtomTokenizer("activity(join,join_acg,location_friends_uinterests,(=),friends,friends,nickname,uinterests,uinterests,nickname)");
		Object next;
		System.out.println("atomName: "+tokenizer.getAtomName());

		while(tokenizer.hasMoreElements()){
			next=tokenizer.nextElement();
			System.out.println(next);
		}
	}
}
