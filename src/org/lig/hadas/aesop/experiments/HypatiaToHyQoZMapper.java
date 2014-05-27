package org.lig.hadas.aesop.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.lig.hadas.aesop.qwderivation.activity.BatchRetrieval;
import org.lig.hadas.aesop.qwderivation.activity.BindJoin;
import org.lig.hadas.aesop.qwderivation.activity.EndParallel;
import org.lig.hadas.aesop.qwderivation.activity.Filter;
import org.lig.hadas.aesop.qwderivation.activity.In;
import org.lig.hadas.aesop.qwderivation.activity.Join;
import org.lig.hadas.aesop.qwderivation.activity.Out;
import org.lig.hadas.aesop.qwderivation.activity.Parallel;
import org.lig.hadas.aesop.qwderivation.activity.Projection;
import org.lig.hadas.aesop.qwderivation.model.Activity;
import org.lig.hadas.aesop.qwderivation.model.Arc;
import org.lig.hadas.aesop.qwderivation.model.QueryWorkflow;
import org.lig.hadas.hybridqp.BindJoinQEPOp;
import org.lig.hadas.hybridqp.BindSelectionQEPOp;
import org.lig.hadas.hybridqp.JoinQEPOp;
import org.lig.hadas.hybridqp.OnDemandDataServiceQEPOp;
import org.lig.hadas.hybridqp.OutputQEPOp;
import org.lig.hadas.hybridqp.ProjectionQEPOp;
import org.lig.hadas.hybridqp.QEPNode;
import org.lig.hadas.hybridqp.SelectionQEPOp;
import org.lig.hadas.hybridqp.Log.Log;


public class HypatiaToHyQoZMapper {

	public static QueryWorkflow getQW(QEPNode node){

		MultiValueMap nodesToActivities = new MultiValueMap(); 
		getActivities(node, nodesToActivities);
//		@SuppressWarnings("unchecked")
//		Collection<Activity> coll=nodesToActivities.values();
		return toQueryWorkflow(node, nodesToActivities,0);
	}
 

	private static QueryWorkflow toQueryWorkflow(QEPNode node, MultiValueMap nodesToActivities, int indent){
		QueryWorkflow qw = null;
		if(node != null){
			qw = new QueryWorkflow();
			Activity lastActivity    = null;
			Activity currentActivity  = null;
			Activity firstActivity = null;
			@SuppressWarnings("unchecked")
			Collection<Activity> activities = nodesToActivities.getCollection(node);

			boolean first=true;
			Arc newArc;
			if(activities!=null){
				for (Iterator<Activity> iterator = activities.iterator(); iterator
						.hasNext();) {
					firstActivity = currentActivity;
					currentActivity = (Activity) iterator.next();
					if(currentActivity==null) continue;

					if(first){ 
						first=!first;
						lastActivity=currentActivity;
					}
					else{
						newArc = new Arc(currentActivity,firstActivity);
						qw.addArc(newArc);
					}
				}
			}
			firstActivity=currentActivity;
			//			Log.out.print(indent+" ");for(int i=0;i<indent;i++)Log.out.print("   ");
			//			Log.out.println("node: "+node.getOperator()+"\tact: "+(currentActivity!=null?currentActivity.getId():null));
			In in = new In("in");
			Out out = new Out("out");
			if(firstActivity==null){//the node has no related activities (e.g. OUTPUT)
				qw.addArc(new Arc(in,out));
			}
			else{
				qw.addArc(new Arc(in,firstActivity));
				qw.addArc(new Arc(lastActivity,out));
			}
			HashSet<QueryWorkflow> childs = new HashSet<QueryWorkflow>();

			if(node.getNChildren()==1){
				QueryWorkflow qwChild = toQueryWorkflow(node.getChild(0), nodesToActivities,indent+1);
				childs.add(qwChild);
			}
			else if(node.getNChildren()>1){
				for(int j=0; j<node.getNChildren(); j++){
					QEPNode child = node.getChild(j);
					QueryWorkflow qwChild = toQueryWorkflow(child, nodesToActivities,indent+1);
					if(qwChild.getActivities().size()==2)//if only has in/out activities
						continue;
					childs.add(qwChild);
				}
			}
			if(childs.size()==1){
				QueryWorkflow uniqueChild = (QueryWorkflow) (childs.toArray())[0];
				//Log.out.println("if(childs.size()==1){+"+uniqueChild);
				qw = QueryWorkflow.merge(uniqueChild, qw);
				childs=null;
			}else if(childs.size()>1){
				String label = QueryWorkflow.generateToken();
				Parallel       par = new Parallel("par"+label);
				EndParallel endpar = new EndParallel("end_par"+label);
				QueryWorkflow tempQW = new QueryWorkflow();
				tempQW.addArc(new Arc(new In("in"),par));
				tempQW.addArc(new Arc(endpar, new Out("out")));

				for (Iterator<QueryWorkflow> iterator = childs.iterator(); iterator.hasNext();) {
					QueryWorkflow qwChild = (QueryWorkflow) iterator.next();
					firstActivity = qwChild.getSuceeding(qwChild.getIn()).get(0);
					lastActivity  = qwChild.getPreceding(qwChild.getOut()).get(0);
					tempQW.addArc(new Arc(par, firstActivity));
					tempQW.addArc(new Arc(lastActivity, endpar));
					for (@SuppressWarnings("unchecked")
					Iterator<Arc> iteratorArcs = qwChild.getArcs().values().iterator(); iteratorArcs.hasNext();) {
						Arc arc = iteratorArcs.next();
						if(arc.getLeft() instanceof In || arc.getRight() instanceof Out)
							continue;
						tempQW.addArc(arc);
					}
				}
				qw=QueryWorkflow.merge(tempQW, qw);
			}
		}
		return qw;
	}


	private static MultiValueMap getActivities(QEPNode currentNode, MultiValueMap nodesToActivities){
		if( currentNode != null) {
			int nChildren = currentNode.getNChildren();
			nodeToActivity(currentNode, nodesToActivities);

			for( int j = 0; j < nChildren; j++ ) {
				QEPNode child = currentNode.getChild(j);				
				getActivities(child, nodesToActivities);
			}
			return nodesToActivities;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void nodeToActivity(QEPNode node, MultiValueMap nodesToActivities){
		if(node.getOperator()      instanceof SelectionQEPOp){           
			SelectionQEPOp selection = (SelectionQEPOp) node.getOperator();
			String serviceName = selection.getAttribute().split("_")[0];
			String methodName = selection.getAttribute().split("_")[1];
			String paramName = selection.getAttribute();
			String prefix = "";
			if(paramName.matches("^.*_.*_o_.*$")) prefix = "f";//Log.out.println("\t\tfiltering");
			else if(paramName.matches("^.*_.*_i_.*$")) prefix = "r";//Log.out.println("\t\tretrieval");

			String id=prefix+"_"+serviceName+"_"+methodName;

			Filter filter = new Filter(	id, 
					selection.getCompOp(), 
					serviceName, 
					methodName, 
					paramName, 
					"null", 
					"null",
					(String) selection.getValue());
			if(prefix.equals("f")){
				nodesToActivities.put(node, filter);
			}else{ //Retrieval
				String lookingForId = id;
				String newId        = id;
				boolean found = false;

				for(Iterator<QEPNode> iterator = nodesToActivities.keySet().iterator(); iterator.hasNext() && !found;) {
					QEPNode n = (QEPNode) iterator.next();
					for (Iterator<Activity> iterator2 = nodesToActivities.getCollection(n).iterator(); iterator2.hasNext() && !found;) {
						Activity act = (Activity) iterator2.next();
						if(act instanceof BatchRetrieval && act.getId().equals(lookingForId)){
							nodesToActivities.remove(n, act);
							BatchRetrieval retrieval = new BatchRetrieval(newId,  newId,
									filter.getLeftServiceName(), 
									filter.getLeftMethodName(), 
									filter.getLeftParamName(), 
									filter.getRightParamName());
							nodesToActivities.put(n, retrieval);
							found=true;
							break;
						}
					}
				}
				if(!found){
					nodesToActivities.put(node, filter);
				}
			}
		}
		else if(node.getOperator() instanceof BindSelectionQEPOp){
			//No activity is created for this operator
			return;
		}
		else if(node.getOperator() instanceof OnDemandDataServiceQEPOp){
			OnDemandDataServiceQEPOp onDemandDataService = (OnDemandDataServiceQEPOp) node.getOperator();
			String methodName  = onDemandDataService.getQOpName().split(":")[1];

			String lookingForId = "r_"+methodName;
			String newId        = "r_"+methodName;
			boolean found = false;

			for(Iterator<QEPNode> iterator = nodesToActivities.keySet().iterator(); iterator.hasNext() && !found;) {
				QEPNode n = (QEPNode) iterator.next();
				for (Iterator<Activity> iterator2 = nodesToActivities.getCollection(n).iterator(); iterator2.hasNext() && !found;) {
					Activity act = (Activity) iterator2.next();
					Filter filter;
					if(act instanceof Filter && act.getId().equals(lookingForId)){
						nodesToActivities.remove(n, act);
						filter = (Filter) act;
						BatchRetrieval retrieval = new BatchRetrieval(newId,  newId,
								filter.getLeftServiceName(), 
								filter.getLeftMethodName(), 
								filter.getLeftParamName(), 
								filter.getRightParamName());
						nodesToActivities.put(node, retrieval);
						found=true;
						break;
					}
				}
			}
			if(!found){
				lookingForId = "b_"+methodName;
				newId        = "r_"+methodName;
				for(Iterator<QEPNode> iterator = nodesToActivities.keySet().iterator(); iterator.hasNext() && !found;) {
					QEPNode n = (QEPNode) iterator.next();
					for (Iterator<Activity> iterator2 = nodesToActivities.getCollection(n).iterator(); iterator2.hasNext() && !found;) {
						Activity act = (Activity) iterator2.next();
						if(act instanceof BindJoin && act.getId().startsWith(lookingForId)){
							//If there is a BindJoin activity, no activity is added to the nodesToActivities multi-map. BindJoin represents the retrieval activity.
							found=true;
							break;
						}
					}
				}			
			}
			if(!found){
				BatchRetrieval retrieval = new BatchRetrieval(newId,  newId,
						"", 
						"", 
						"", 
						"");
				nodesToActivities.put(node, retrieval);
			}

			return;
		}
		else if(node.getOperator() instanceof JoinQEPOp){
			JoinQEPOp joinQEPOp = (JoinQEPOp) node.getOperator();

			String leftServiceName = joinQEPOp.getLeftJoinAttribute().split("_")[0];
			String leftMethodName  = joinQEPOp.getLeftJoinAttribute().split("_")[1];
			String leftParamName   = joinQEPOp.getLeftJoinAttribute().split("_")[2]+"_"+joinQEPOp.getLeftJoinAttribute().split("_")[3];

			String rightServiceName = joinQEPOp.getRightJoinAttribute().split("_")[0];
			String rightMethodName  = joinQEPOp.getRightJoinAttribute().split("_")[1];
			String rightParamName   = joinQEPOp.getRightJoinAttribute().split("_")[2]+"_"+joinQEPOp.getRightJoinAttribute().split("_")[3];

			String newId        = "c_"+leftServiceName+"_"+leftMethodName+"_"+rightServiceName+"_"+rightMethodName;

			Join join = new Join(newId, newId, 
					"=", 
					leftServiceName, 
					leftMethodName, 
					leftParamName, 
					rightServiceName, 
					rightMethodName, 
					rightParamName);
			nodesToActivities.put(node, join);

		}else if(node.getOperator() instanceof BindJoinQEPOp){
			BindJoinQEPOp bindJoinQEPOp = (BindJoinQEPOp) node.getOperator();
			Log.out.println("       .getOperation(): "+bindJoinQEPOp.getOperation());
			Log.out.println("         .getQOpName(): "+bindJoinQEPOp.getQOpName());
			Log.out.println("     .getAttMappings(): "+bindJoinQEPOp.getAttMappings());
			Map <String,String> map_atts = bindJoinQEPOp.getAttMappings();
			String left_term=null;
			String right_term=null;
			for (String key : map_atts.keySet()) {
				left_term=key;
				right_term=map_atts.get(key);
				break;
			}
			Log.out.println(left_term+"="+right_term);
			String leftServiceName = left_term.split("_")[0];
			String leftMethodName  = left_term.split("_")[1];
			String leftParamName   = left_term.split("_")[2]+"_"+left_term.split("_")[3];

			String rightServiceName = right_term.split("_")[0];
			String rightMethodName  = right_term.split("_")[1];
			String rightParamName   = right_term.split("_")[2]+"_"+right_term.split("_")[3];

			String newId        = "b_"+leftServiceName+"_"+leftMethodName+"_"+rightServiceName+"_"+rightMethodName;
			//			Log.out.println("newId:"+newId);


			BindJoin bindJoin = new BindJoin(newId, newId, 
					"=", 
					leftServiceName, 
					leftMethodName, 
					leftParamName, 
					rightServiceName, 
					rightMethodName, 
					rightParamName);


			//String lookingForId = newId.replaceFirst("b_", "r_");
			String lookingForId = "r_"+leftServiceName+"_"+leftMethodName;
			boolean found = false;

			for(Iterator<QEPNode> iterator = nodesToActivities.keySet().iterator(); iterator.hasNext() && !found;) {
				QEPNode n = (QEPNode) iterator.next();
				for (Iterator<Activity> iterator2 = nodesToActivities.getCollection(n).iterator(); iterator2.hasNext() && !found;) {
					Activity act = (Activity) iterator2.next();
					if(act instanceof BatchRetrieval && lookingForId.startsWith(act.getId())){
						nodesToActivities.remove(n, act);
						nodesToActivities.put(n, bindJoin);
						found=true;
						break;
					}
				}
			}
			if(!found){
				nodesToActivities.put(node, bindJoin);
			}
			return ;

		}
		else if(node.getOperator() instanceof ProjectionQEPOp){

			ProjectionQEPOp projection = (ProjectionQEPOp) node.getOperator();
			String [] attributeNames = projection.getAttributeNames();
			String serviceName = attributeNames[3].split("_")[0];
			String methodName = attributeNames[3].split("_")[1];
			String previousAlias = serviceName+"_"+methodName;
			ArrayList<ArrayList<String>> projectionList = new ArrayList<ArrayList<String>>();
			//			int activityNumber = 0;
			Projection projectionActivity=null;
			for (int i = 0; i < attributeNames.length; i++) {
				if(attributeNames[i].equals("timestamp") || attributeNames[i].equals("tuple_sign") || attributeNames[i].equals("tuple_id"))
					continue;

				String alias= attributeNames[i].split("_")[0]+"_"+attributeNames[i].split("_")[1];
				if(!alias.equals(previousAlias) ){
					projectionActivity = new Projection("p_"+previousAlias, (ArrayList<?>) projectionList.clone());
					nodesToActivities.put(node, projectionActivity);
					projectionList.clear();							
					previousAlias=alias;
				}
				projectionList.add(new ArrayList<String>(Arrays.asList(serviceName, methodName, attributeNames[i])));

			}
			projectionActivity = new Projection("p_"+previousAlias, (ArrayList<?>) projectionList.clone());
			nodesToActivities.put(node, projectionActivity);
			projectionList.clear();
		}
		else if(node.getOperator() instanceof OutputQEPOp){
			//No se crea ninguna atividad
			return;
		}
		else{
			//No existe mapeo para este tipo  de nodo
			Log.out.println("??? "+node.getOperator().getClass());
			return ;
		}
	}
	//	private MultiValueMap getActivities(QEPNode currentNode, HashMap<String,Activity> activities){
	//		if( currentNode != null) {
	//			MultiValueMap nodesToActivities = new MultiValueMap();
	//			int nChildren = currentNode.getNChildren();
	//			ArrayList<Activity> resultingActivities = nodeToActivity(currentNode, activities);
	//
	//			if(resultingActivities==null)
	//				nodesToActivities.put(currentNode, null);
	//			else
	//				nodesToActivities.putAll(currentNode, resultingActivities);
	//
	//			for( int j = 0; j < nChildren; j++ ) {
	//				QEPNode child = currentNode.getChild(j);				
	//				MultiValueMap nta =getActivities(child, activities);
	//
	//				for (@SuppressWarnings("unchecked")Iterator<QEPNode> iterator = nta.keySet().iterator(); iterator.hasNext();) {
	//					QEPNode node = iterator.next();
	//					nta.getCollection(node);
	//					nodesToActivities.putAll(node, nta.getCollection(node));
	//				}
	//			}
	//			return nodesToActivities;
	//		}
	//		return null;
	//	}
	//	ArrayList<Activity> nodeToActivity(QEPNode node, HashMap<String,Activity> activities){
	//		ArrayList<Activity> resultingActivities = new ArrayList<Activity>();
	//		if(node.getOperator()      instanceof SelectionQEPOp){           
	//			SelectionQEPOp selection = (SelectionQEPOp) node.getOperator();
	//			String serviceName = selection.getAttribute().split("_")[0];
	//			String methodName = selection.getAttribute().split("_")[1];
	//			String paramName = selection.getAttribute();
	//			String prefix = "";
	//			if(paramName.matches("^.*_.*_o_.*$")) prefix = "f";//Log.out.println("\t\tfiltering");
	//			else if(paramName.matches("^.*_.*_i_.*$")) prefix = "r";//Log.out.println("\t\tretrieval");
	//
	//			String id=prefix+"_"+serviceName+"_"+methodName;
	//
	//			
	//			Filter filter = new Filter(	id, 
	//					selection.getCompOp(), 
	//					serviceName, 
	//					methodName, 
	//					paramName, 
	//					"null", 
	//					"null",
	//					(String) selection.getValue());
	//			activities.put(id, filter);
	//			resultingActivities.add(filter);
	//		}
	//		else if(node.getOperator() instanceof BindSelectionQEPOp){
	//			//No se crea ninguna actividad
	//			return null;
	//		}
	//		else if(node.getOperator() instanceof OnDemandDataServiceQEPOp){
	//			OnDemandDataServiceQEPOp onDemandDataService = (OnDemandDataServiceQEPOp) node.getOperator();
	//			String methodName  = onDemandDataService.getQOpName().split(":")[1];
	//
	//			String lookingForId = "r_"+methodName;
	//			String newId        = "r_"+methodName;
	//
	//			Filter filter = (Filter)activities.remove(lookingForId);
	//			
	//			BatchRetrieval retrieval = new BatchRetrieval(newId,  newId,
	//					filter.getLeftServiceName(), 
	//					filter.getLeftMethodName(), 
	//					filter.getLeftParamName(), 
	//					filter.getRightParamName());
	//
	//
	//			activities.put(newId, retrieval);
	//			resultingActivities.add(retrieval);
	//		}
	//		else if(node.getOperator() instanceof JoinQEPOp){
	//			JoinQEPOp joinQEPOp = (JoinQEPOp) node.getOperator();
	//
	//			String leftServiceName = joinQEPOp.getLeftJoinAttribute().split("_")[0];
	//			String leftMethodName  = joinQEPOp.getLeftJoinAttribute().split("_")[1];
	//			String leftParamName   = joinQEPOp.getLeftJoinAttribute().split("_")[2]+"_"+joinQEPOp.getLeftJoinAttribute().split("_")[3];
	//
	//			String rightServiceName = joinQEPOp.getRightJoinAttribute().split("_")[0];
	//			String rightMethodName  = joinQEPOp.getRightJoinAttribute().split("_")[1];
	//			String rightParamName   = joinQEPOp.getRightJoinAttribute().split("_")[2]+"_"+joinQEPOp.getRightJoinAttribute().split("_")[3];
	//
	//			String newId        = "c_"+leftServiceName+"_"+leftMethodName+"_"+rightServiceName+"_"+rightMethodName;
	//
	//			Join join = new Join(newId, newId, 
	//					"=", 
	//					leftServiceName, 
	//					leftMethodName, 
	//					leftParamName, 
	//					rightServiceName, 
	//					rightMethodName, 
	//					rightParamName);
	//
	//			activities.put(newId, join);
	//			resultingActivities.add(join);
	//		}
	//		else if(node.getOperator() instanceof ProjectionQEPOp){
	//
	//			ProjectionQEPOp projection = (ProjectionQEPOp) node.getOperator();
	//			String [] attributeNames = projection.getAttributeNames();
	//			String serviceName = attributeNames[3].split("_")[0];
	//			String methodName = attributeNames[3].split("_")[1];
	//			String previousAlias = serviceName+"_"+methodName;
	//			ArrayList<ArrayList<String>> projectionList = new ArrayList<ArrayList<String>>();
	//			int activityNumber = 0;
	//			Projection projectionActivity=null;
	//			for (int i = 0; i < attributeNames.length; i++) {
	//				if(attributeNames[i].equals("timestamp") || attributeNames[i].equals("tuple_sign") || attributeNames[i].equals("tuple_id"))
	//					continue;
	//
	//				String alias= attributeNames[i].split("_")[0]+"_"+attributeNames[i].split("_")[1];
	//				if(!alias.equals(previousAlias) ){
	//					projectionActivity = new Projection("p_"+previousAlias, (ArrayList<?>) projectionList.clone());
	//					activities.put(("p_"+previousAlias), projectionActivity);
	//					projectionList.clear();							
	//					previousAlias=alias;
	//					resultingActivities.add(projectionActivity);
	//				}
	//				projectionList.add(new ArrayList<String>(Arrays.asList(serviceName, methodName, attributeNames[i])));
	//
	//			}
	//			projectionActivity = new Projection("p_"+previousAlias, (ArrayList<?>) projectionList.clone());
	//			activities.put(("p_"+previousAlias), projectionActivity);
	//			projectionList.clear();
	//			resultingActivities.add(projectionActivity);
	//		}
	//		else if(node.getOperator() instanceof OutputQEPOp){
	//			//No se crea ninguna atividad
	//			return null;
	//		}
	//		else{
	//			//No existe mapeo para este tipo  de nodo
	//			Log.out.println("??? "+node.getOperator().getClass());
	//			return null;
	//		}
	//		return resultingActivities;
	//	}

	@SuppressWarnings("unused")
	private static String QEPtoDAG(QEPNode node){
		String idFather=null;
		int    nChildren=0;
		String idChild=null;
		String idLast =null;
		if( node != null) {
			idFather = node.getOperator().toString().toLowerCase().replaceAll(" ", "_");			
			nChildren = node.getNChildren();
			idLast=idFather;
			for( int j = 0; j < nChildren; j++ ) {
				QEPNode child = node.getChild(j);
				idChild = child.getOperator().toString().toLowerCase().replaceAll(" ", "_");
				if(nChildren>1){
					Log.out.format("arc(par, %s)", idLast);
					Log.out.format("arc(%s, end_par)",idChild);
				}
				else
					Log.out.format("arc(%s, %s)", idChild, idFather);
			}
			if(nChildren>1){
				Log.out.format("arc(end_par, %s)",idFather);
				idLast="par";
			}
		}
		return idLast;
	}	
	public static QueryWorkflow QEPToDAG(QEPNode node){
		QueryWorkflow qw = new QueryWorkflow();
		QEPtoDAG(node, qw);
		return qw;
	}
	private static Activity QEPtoDAG(QEPNode node, QueryWorkflow qw){
		Activity father=null;
		int    nChildren=0;
		Activity child=null;
		Activity last =null;
		if( node != null) {
			//			String fatherId = node.getOperator().toString().toLowerCase().replaceAll(" ", "_");			
			nChildren = node.getNChildren();
			last=father;
			for( int j = 0; j < nChildren; j++ ) {
				QEPNode childNode = node.getChild(j);
				//				String childId = childNode.getOperator().toString().toLowerCase().replaceAll(" ", "_");
				Log.out.println(childNode.getOperator().getClass());

				if(nChildren>1){
					Log.out.format("arc(par, %s)", last);
					Log.out.format("arc(%s, end_par)",child);
				}
				else
					Log.out.format("arc(%s, %s)", child, father);
			}
			if(nChildren>1){
				Log.out.format("arc(end_par, %s)",father);
				//				String lastId="par";
			}
		}
		return last;
	}	
}




