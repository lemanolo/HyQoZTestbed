package org.lig.hadas.aesop.qwGeneration.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.lig.hadas.aesop.utils.RandomString;

public class QueryWorkflow {

	Set<Activity> activities;

	MultiValueMap arcs;
	MultiValueMap arcs_inverted;

	//MultiValueMap arcs_temp;
	Activity in;

	Activity out;
	Activity init;
	Activity finish;

	static RandomString stringGenerator = new RandomString(3);

	public QueryWorkflow() {
		this.activities = new HashSet<Activity>();
		this.arcs = new MultiValueMap();
		this.arcs_inverted = new MultiValueMap();

	}


	QueryWorkflow(Set<Activity> activities, MultiValueMap arcs){
		this.activities = activities;
		this.arcs = arcs;
		this.arcs_inverted = new MultiValueMap();
	}


	public Activity getIn() {
		return in;
	}


	public Activity getOut() {
		return out;
	}

	public void addArc(Arc arc){
		if(arc.getLeft() instanceof In)
			this.in = arc.getLeft();
		if(arc.getRight() instanceof Out)
			this.out = arc.getRight();

		this.arcs.put(arc.getLeft().getId(),arc);
		this.arcs_inverted.put(arc.getRight().getId(),arc);

		boolean go = true;
		for (Activity activity : this.activities) {
			if(arc.getLeft().equals(activity)){
				go=false;
				break;
			}
		}
		if(go)
			this.activities.add(arc.getLeft());

		go=true;
		for (Activity activity : this.activities) {
			if(arc.getRight().equals(activity)){
				go=false;
				break;
			}
		}
		if(go)
			this.activities.add(arc.getRight());


	}

	private Activity seqActivity(Activity activity, StringBuilder string, int level){
		String tabs = "";
		for(int i=0; i< level;i++) tabs+="\t";
		if(!(activity instanceof Out))
			string.append(tabs+activity.toASMString()+"\n");
		else{
			string.append(activity.toASMString()+"\n");
			return activity;
		}

		Arc arc =  (Arc) (this.arcs.getCollection(activity.getId()).toArray())[0];
		Activity rightActivity = arc.getRight();
		if(rightActivity instanceof EndParallel)
			return rightActivity;

		if( rightActivity instanceof Parallel)   
			return parActivity(rightActivity, string, level+1);

		return seqActivity(rightActivity, string, level);
	}

	private Activity parActivity(Activity activity, StringBuilder string,int level){
		String tabs = "";
		for(int i=0; i< level;i++) tabs+="\t";
		string.append(tabs+activity.toASMString()+"\n");

		@SuppressWarnings("unchecked")
		Collection<Arc> arcs = this.arcs.getCollection(activity.getId());
		Activity act=null;
		for (Arc arc : arcs) { //this loop iterates until all the branches of Parallel has been traversed
			//When the traversal reaches and EndParallel by seqActivity method, the recursivity stops
			//Thus it is assumed that when the activities of this loop finish its because they have reach a EndParallel
			Activity rightActivity = arc.getRight();
			string.append(tabs+"\tseq\n");
			if( rightActivity instanceof Parallel)   
				act =parActivity(rightActivity, string, level+2);
			else{
				act = seqActivity(rightActivity, string, level+2);
			}
			string.append(tabs+"\tendseq\n");
		}
		return seqActivity(act, string,level);
	}
	public String toASMString(){
		StringBuilder string = new StringBuilder();

		this.seqActivity(this.in,string,0);
		return string.toString();
	}





	public String toString(){
		String result="";
		result+="V = {";
		for (Iterator<Activity> iterator = this.activities.iterator(); iterator.hasNext();) {
			Activity activity = (Activity) iterator.next();
			result+=activity.toString();
			if(iterator.hasNext())
				result+=", ";
		}
		result+="}";

		result+="\nE = {";
		for (@SuppressWarnings("unchecked")
		Iterator<Arc> iterator = this.arcs.values().iterator(); iterator.hasNext();) {
			Arc arc = (Arc) iterator.next();
			result+=arc.toString();
			if(iterator.hasNext())
				result+=", ";
		}
		result+="}";
		return result;
	}

	public String toStringShort(){
		String result="";
		result+="V = {";
		for (Iterator<Activity> iterator = this.activities.iterator(); iterator.hasNext();) {
			Activity activity = (Activity) iterator.next();
			result+=activity.getId();
			if(iterator.hasNext())
				result+=", ";
		}
		result+="}";

		result+="\nE = {";
		for (@SuppressWarnings("unchecked")
		Iterator<Arc> iterator = this.arcs.values().iterator(); iterator.hasNext();) {
			Arc arc = (Arc) iterator.next();
			result+=arc.toStringShort();
			if(iterator.hasNext())
				result+=", ";
		}
		result+="}";
		return result;
	}

	public String toFunctor(){
		HashSet<String> A = new HashSet<String>();
		HashSet<String> P = new HashSet<String>();
		HashSet<String> C = new HashSet<String>();
		HashSet<String> V = new HashSet<String>();
		HashSet<String> E  = new HashSet<String>();
		String result="qw( [";
		for (Iterator<Activity> iterator = this.activities.iterator(); iterator.hasNext();) {
			Activity activity = (Activity) iterator.next();
			if (   activity instanceof Parallel  
					|| activity instanceof EndParallel
					|| activity instanceof In
					|| activity instanceof Out) {
				if(   activity instanceof Parallel  || activity instanceof EndParallel)
					P.add(activity.getId());
				if (   activity instanceof In || activity instanceof Out)
					C.add(activity.getId());
			}else
				A.add(activity.getId());
			V.add(activity.getId());
		}

		for (@SuppressWarnings("unchecked")
		Iterator<String> iterator = this.arcs.keySet().iterator(); iterator.hasNext();) {
			String idActivity = (String) iterator.next();

			for (@SuppressWarnings("unchecked")
			Iterator<Arc> iterator2 = this.arcs.getCollection(idActivity).iterator(); iterator2.hasNext();) {
				Arc arc = (Arc) iterator2.next();
				E.add(arc.toStringShort());
			}
		}
		result = "qw([";
		for (Iterator<String> iterator2 = A.iterator(); iterator2.hasNext();) {
			String id = (String) iterator2.next();
			result+=id;
			if(iterator2.hasNext()) result+=", ";
		}
		result+="], \n\t[";
		for (Iterator<String> iterator2 = P.iterator(); iterator2.hasNext();) {
			String id = (String) iterator2.next();
			result+=id;
			if(iterator2.hasNext()) result+=", ";
		}
		result+="], \n\t[";
		for (Iterator<String> iterator2 = V.iterator(); iterator2.hasNext();) {
			String id = (String) iterator2.next();
			result+=id;
			if(iterator2.hasNext()) result+=", ";
		}
		result+="], \n\t[";
		for (Iterator<String> iterator2 = E.iterator(); iterator2.hasNext();) {
			String id = (String) iterator2.next();
			result+=id;
			if(iterator2.hasNext()) result+=", ";
		}
		result+="], \n\t";

		for (Iterator<String> iterator2 = C.iterator(); iterator2.hasNext();) {
			String id = (String) iterator2.next();
			result+=id;
			if(iterator2.hasNext()) result+=", ";
		}
		result+=",qw).";
		return result;
	}
	public MultiValueMap getArcs(){
		return this.arcs;
	}
	MultiValueMap getInvertedArcs(){
		return this.arcs_inverted;
	}

	public void defineDataFlow(){
		WalkerListener listener = new WalkerListener() {
			ArrayList<String> unifiedNames = new ArrayList<String>();

			RandomString stringGenerator = new RandomString(3);
			@Override
			public void activityFound(Activity activity) {

				if(activity.getActivityType().isControlFlowOperator())
					return;

				//				System.out.print(activity.getId()+"\t");				
				String uname = ""; 
				if(activity.getActivityType().isDataRetrieval()){
					UnaryDataOperation retrieval =(UnaryDataOperation)activity;
					retrieval.setScope("");

					uname = retrieval.getUnifiedName();
					this.unifiedNames.add(uname);

					//					System.out.println(retrieval.getUnifiedName()+"\t\t"+retrieval.getScope());
				}else if(activity.getActivityType().isDataCorrelation()){
					BinaryDataOperation dataOperation =(BinaryDataOperation)activity;

					String scope1 = this.unifiedNames.remove(this.unifiedNames.size()-1);
					String scope2 = this.unifiedNames.remove(this.unifiedNames.size()-1);
					dataOperation.setScope(scope1, scope2);

					uname = scope1+"_"+scope2;
					this.unifiedNames.add(uname);
					dataOperation.setUnifiedName(uname);

					//					System.out.println(dataOperation.getUnifiedName()+"\t\t"+dataOperation.getScope()[0]+"\t\t"+dataOperation.getScope()[1]);

				}else if(activity.getActivityType().isDataOperator()){
					UnaryDataOperation dataOperation =(UnaryDataOperation)activity;
					String scope = this.unifiedNames.remove(this.unifiedNames.size()-1);
					dataOperation.setScope(scope);

					uname = scope+this.stringGenerator.nextString();
					dataOperation.setUnifiedName(uname);

					this.unifiedNames.add(uname);

					//					System.out.println(dataOperation.getUnifiedName()+"\t\t"+dataOperation.getScope());
				}


			}
		};

		Walker walker = new Walker(this, listener);
		walker.walk();
	}


	public interface WalkerListener {
		public void activityFound(Activity activity);
	}
	public class Walker {
		WalkerListener listener;
		QueryWorkflow qw;

		public Walker(QueryWorkflow qw, WalkerListener listener) {
			this.qw 		= qw;
			this.listener 	= listener;
		}

		public void walk(){
			this.walkSeqActivity(qw.in);
		}

		private Activity walkSeqActivity(Activity activity){

			if(!(activity instanceof Out))
				this.listener.activityFound(activity);
			else{
				this.listener.activityFound(activity);
				return activity;
			}

			Arc arc =  (Arc) (this.qw.getArcs().getCollection(activity.getId()).toArray())[0];
			Activity rightActivity = arc.getRight();
			if(rightActivity instanceof EndParallel)
				return rightActivity;
			if( rightActivity instanceof Parallel)   
				return walkParActivity(rightActivity);

			return walkSeqActivity(rightActivity);
		}

		private Activity walkParActivity(Activity activity){
			@SuppressWarnings("unchecked")
			Collection<Arc> arcs = this.qw.getArcs().getCollection(activity.getId());
			Activity act=null;
			for (Arc arc : arcs) { //this loop iterates until all the branches of Parallel has been traversed
				//When the traversal reaches and EndParallel by seqActivity method, the recursivity stops
				//Thus it is assumed that when the activities of this loop finish its because they have reach a EndParallel
				Activity rightActivity = arc.getRight();
				if( rightActivity instanceof Parallel)   
					act =walkParActivity(rightActivity);
				else{
					act = walkSeqActivity(rightActivity);
				}
			}
			return walkSeqActivity(act);
		}

	}

	public Set<Activity> getActivities() {
		return activities;
	}
	public boolean containsActivityWithId(String id){
		for (Activity activity : activities) {
			if(activity.getId() == id)
				return true;
		}
		return false;
	}
	public Activity getActivityWithId(String id){
		for (Activity activity : activities) {
			if(activity.getId() == id)
				return activity;
		}
		return null;
	}

	public Activity getMostRightActivity(Activity from){
		if(this.arcs.containsKey(from.getId()))
			for (@SuppressWarnings("unchecked")
			Iterator <Arc>iterator = this.arcs.getCollection(from.getId()).iterator(); iterator.hasNext();) {
				Arc arc = (Arc) iterator.next();
				return getMostRightActivity(arc.getRight());
			}
		return from;
	}

	public static String generateToken(){
		return stringGenerator.nextString();
	}

	public ArrayList<Activity> getSuceeding(Activity a){
		ArrayList<Activity> succ = new ArrayList<Activity>();
		if(this.arcs.containsKey(a.getId())){
			for (@SuppressWarnings("unchecked")
			Iterator <Arc>iterator = this.arcs.getCollection(a.getId()).iterator(); iterator.hasNext();) {
				Arc arc = (Arc) iterator.next();
				succ.add(arc.getRight());
			}
		}
		return succ;
	}
	public ArrayList<Activity> getPreceding(Activity a){ 
		ArrayList<Activity> prec = new ArrayList<Activity>();
		if(this.arcs_inverted.containsKey(a.getId())){
			for (@SuppressWarnings("unchecked")
			Iterator <Arc>iterator = this.arcs_inverted.getCollection(a.getId()).iterator(); iterator.hasNext();) {
				Arc arc = (Arc) iterator.next();
				prec.add(arc.getLeft());
			}
		}
		return prec;
	}
	public static QueryWorkflow merge(QueryWorkflow qw1, QueryWorkflow qw2){
		QueryWorkflow newQW = null;


		ArrayList<Activity> lastActivities1  = qw1.getPreceding(qw1.out);
		ArrayList<Activity> firstActivities2 = qw2.getSuceeding(qw2.in);

		if(lastActivities1.size()==1 && firstActivities2.size()==1){
			newQW = new QueryWorkflow();
			if(qw1.getArcs().size()>1){
				for (@SuppressWarnings("unchecked")
				Iterator<Arc> iterator = qw1.getArcs().values().iterator(); iterator.hasNext();) {

					Arc arc = (Arc) iterator.next();
					if( !(arc.getRight() instanceof Out)){
						newQW.addArc(arc);
					}
				}
			}
			if(qw2.getArcs().size()>1){
				for (@SuppressWarnings("unchecked")
				Iterator<Arc> iterator = qw2.getArcs().values().iterator(); iterator.hasNext();) {
					Arc arc = (Arc) iterator.next();
					if( !(arc.getLeft() instanceof In)){
						newQW.addArc(arc);
					}
				}
			}
			Arc newArc = new Arc(lastActivities1.get(0), firstActivities2.get(0));
			newQW.addArc(newArc);
		}
		return newQW;
	}
}


