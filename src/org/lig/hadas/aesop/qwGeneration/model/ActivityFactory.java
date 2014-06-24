package org.lig.hadas.aesop.qwGeneration.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.lig.hadas.aesop.qwGeneration.exceptions.*;
import org.lig.hadas.aesop.utils.AtomTokenizer;


public class ActivityFactory {
	private static HashMap<String, Activity> activityStore = new HashMap<String, Activity>();

	public static Activity factory(String activityString) throws ActivityDescriptionException{

		String originalString = activityString;
		activityString=activityString.trim();
		int idx=0;
		AtomTokenizer tokenizer = new AtomTokenizer(activityString);
		Activity activity=null;
		System.out.println("activityString: "+activityString);
		if(tokenizer.hasMoreElements()){

			String type = (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
			String id   = (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
			if(type == null)throw new ActivityTypeRequiredException(String.format("Activity � 'type' is required in position 1 of activity's descriptor: %s", originalString));
			if(id == null)	throw new ActivityIdRequiredException(String.format("Activity � 'id' is required in position 2 of activity's descriptor: %s", originalString));

			//if the activity with 'id' already exist in the activityStore, 
			//it is not necessary to factory the activity.
			if(activityStore.containsKey(id))
				return activityStore.get(id); 

			if(ActivityType.BATCH_RETRIEVAL.getType().equals(type)){
				String serviceName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String unifiedName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				
				if(unifiedName 	== null) throw new BatchActivityUnifiedNameRequiredException(String.format("Batch Retrieval activity � 'unifiedName' is required in position 3 of activity's descriptor: %s", originalString));
				if(serviceName 	== null) throw new BatchActivityServiceNameRequiredException(String.format("Batch Retrieval activity � 'serviceName' is required in position 4 of activity's descriptor: %s", originalString));
				if(methodName 	== null) throw new BatchActivityMethodNameRequiredException(String.format("Batch Retrieval activity � 'methodName' is required in position 5 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Batch Retrieval activity � extra parameter in activity's descriptor: %s", originalString));


				activity = new BatchRetrieval(id, unifiedName, serviceName, methodName, "boundedAtt", "constant");
			}
			else if(ActivityType.STREAM_SUBSCRIPTION.getType().equals(type)){
				String unifiedName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String serviceName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(unifiedName 	== null) throw new StreamActivityServiceNameRequiredException(String.format("Stream Subscription activity � 'unifiedName' is required in position 3 of activity's descriptor: %s", originalString));
				if(serviceName 	== null) throw new StreamActivityServiceNameRequiredException(String.format("Stream Subscription activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(methodName 	== null) throw new StreamActivityMethodNameRequiredException(String.format("Stream Subscription  activity � 'methodName' is required in position 5 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Stream Subscription activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new StreamSubscription(id, unifiedName, serviceName, methodName);
			}else if(ActivityType.PROJECTION.getType().equals(type)){
				ArrayList<?> projectionList		= (ArrayList<?>) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
System.out.println(":::"+projectionList);
				if(projectionList 	 == null) throw new ProjectionActivityProjectionListRequiredException(String.format("Projection activity � 'projectionList' is required in position 3 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Projection activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new Projection(id, projectionList);

			}else if(ActivityType.FILTER.getType().equals(type)){
				String eqOperator			= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String leftServiceName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String rightServiceName = (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(eqOperator			== null) throw new FilterActivityEqualityOperatorRequiredException(String.format("Filter activity � 'eqOperator' is required in position 3 of activity's descriptor: %s", originalString));
				if(leftServiceName	== null) throw new FilterActivityLeftSideParameterRequiredException(String.format("Filter activity � 'leftServiceName' is required in position 4 of activity's descriptor: %s", originalString));
				if(leftMethodName	== null) throw new FilterActivityLeftSideParameterRequiredException(String.format("Filter activity � 'leftMethodName' is required in position 5 of activity's descriptor: %s", originalString));
				if(leftParamName	== null) throw new FilterActivityLeftSideParameterRequiredException(String.format("Filter activity � 'leftParamName' is required in position 6 of activity's descriptor: %s", originalString));

				if(rightServiceName	== null) throw new FilterActivityRightSideParameterRequiredException(String.format("Filter activity � 'rightServiceName' is required in position 7 of activity's descriptor: %s", originalString));
				if(rightMethodName	== null) throw new FilterActivityRightSideParameterRequiredException(String.format("Filter activity � 'rightMethodName' is required in position 8 of activity's descriptor: %s", originalString));
				if(rightParamName	== null) throw new FilterActivityRightSideParameterRequiredException(String.format("Filter activity � 'rightParamName' is required in position 9 of activity's descriptor: %s", originalString));


				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Filter activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new Filter( id,  eqOperator, 
						leftServiceName,  leftMethodName,  leftParamName,
						rightServiceName,  rightMethodName,  rightParamName);
			}else if(ActivityType.FILTER_INVOKE.getType().equals(type)){
				String serviceName 		= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName 		= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				ArrayList<?> parameterList 	= (ArrayList<?>) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String eqOperator		= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String leftServiceName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String rightServiceName = (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(serviceName	== null) throw new FilterInvocationActivityServiceNameRequiredException(String.format("Filter Invocation activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(methodName	== null) throw new FilterInvocationActivityMethodNameRequiredException(String.format("Filter Invocation activity � 'methodName' is required in position 4 of activity's descriptor: %s", originalString));
				if(parameterList	== null) throw new FilterInvocationActivityParameterListRequiredException(String.format("Filter Invocation activity � 'parameterList' is required in position 5 of activity's descriptor: %s", originalString));

				
				if(eqOperator			== null) throw new FilterInvocationActivityEqualityOperatorRequiredException(String.format("Filter Invocation activity � 'eqOperator' is required in position 6 of activity's descriptor: %s", originalString));
				if(leftServiceName	== null) throw new FilterInvocationActivityLeftSideParameterRequiredException(String.format("Filter Invocation activity � 'leftServiceName' is required in position 7 of activity's descriptor: %s", originalString));
				if(leftMethodName	== null) throw new FilterInvocationActivityLeftSideParameterRequiredException(String.format("Filter Invocation activity � 'leftMethodName' is required in position 8 of activity's descriptor: %s", originalString));
				if(leftParamName	== null) throw new FilterInvocationActivityLeftSideParameterRequiredException(String.format("Filter Invocation activity � 'leftParamName' is required in position 9 of activity's descriptor: %s", originalString));

				if(rightServiceName	== null) throw new FilterInvocationActivityRightSideParameterRequiredException(String.format("Filter Invocation activity � 'rightServiceName' is required in position 10 of activity's descriptor: %s", originalString));
				if(rightMethodName	== null) throw new FilterInvocationActivityRightSideParameterRequiredException(String.format("Filter Invocation activity � 'rightMethodName' is required in position 11 of activity's descriptor: %s", originalString));
				if(rightParamName	== null) throw new FilterInvocationActivityRightSideParameterRequiredException(String.format("Filter Invocation activity � 'rightParamName' is required in position 12 of activity's descriptor: %s", originalString));


				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Filter Invocation activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new FilterInvoke( id,  
						serviceName, methodName, parameterList,
						eqOperator, 
						leftServiceName,  leftMethodName,  leftParamName,
						rightServiceName,  rightMethodName,  rightParamName);
			}else if(ActivityType.RENAME_PARAM.getType().equals(type)){

				String serviceName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String parameterName= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String alias		= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(serviceName	== null) throw new RenameParameterActivityParameterRequiredException(String.format("Rename Parameter activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(methodName	== null) throw new RenameParameterActivityParameterRequiredException(String.format("Rename Parameter activity � 'methodName' is required in position 4 of activity's descriptor: %s", originalString));
				if(parameterName== null) throw new RenameParameterActivityParameterRequiredException(String.format("Rename Parameter activity � 'parameterName' is required in position 5 of activity's descriptor: %s", originalString));
				
				if(alias	== null) throw new RenameParameterActivityAliasRequiredException(String.format("Rename Parameter activity � 'alias' is required in position 6 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Rename Parameter activity � extra parameter in activity's descriptor: %s", originalString));


				activity = new RenameParameter(id,serviceName,methodName,parameterName,alias); //TODO
			}else if(ActivityType.RENAME_SERVICE.getType().equals(type)){

				String serviceName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String alias		= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(serviceName	== null) throw new RenameServiceActivityServiceMethidRequiredException(String.format("Rename Service activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(methodName	== null) throw new RenameServiceActivityServiceMethidRequiredException(String.format("Rename Service activity � 'methodName' is required in position 4 of activity's descriptor: %s", originalString));
				
				if(alias	== null) throw new RenameServiceActivityAliasRequiredException(String.format("Rename Service activity � 'alias' is required in position 6 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Rename Service activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new RenameService(id,serviceName,methodName,alias);
			}else if(ActivityType.TIME_WINDOW.getType().equals(type)){
				String unifiedName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String serviceName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String windowSize 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				
				if(unifiedName	== null) throw new TimeWindowActivityServiceNameRequiredException(String.format("Time Window activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(serviceName	== null) throw new TimeWindowActivityServiceNameRequiredException(String.format("Time Window activity � 'serviceName' is required in position 3 of activity's descriptor: %s", originalString));
				if(methodName	== null) throw new TimeWindowActivityMethodNameRequiredException(String.format("Time Window activity � 'methodName' is required in position 4 of activity's descriptor: %s", originalString));
				if(windowSize	== null) throw new TimeWindowActivityWindowSizeRequiredException(String.format("Time Window activity � 'windowSize' is required in position 5 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Time Window activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new TimeWindow( id, unifiedName, serviceName, methodName,  windowSize);

			}else if(ActivityType.TUPLE_WINDOW.getType().equals(type)){
				String unifiedName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String serviceName	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String methodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String windowSize 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				
				if(unifiedName	== null) throw new TupleWindowActivityServiceNameRequiredException(String.format("Tuple Window activity � 'unifiedName' is required in position 3 of activity's descriptor: %s", originalString));
				if(serviceName	== null) throw new TupleWindowActivityServiceNameRequiredException(String.format("Tuple Window activity � 'serviceName' is required in position 4 of activity's descriptor: %s", originalString));
				if(methodName	== null) throw new TupleWindowActivityMethodNameRequiredException(String.format("Tuple Window activity � 'methodName' is required in position 5 of activity's descriptor: %s", originalString));
				if(windowSize	== null) throw new TupleWindowActivityWindowSizeRequiredException(String.format("Tuple Window activity � 'windowSize' is required in position 6 of activity's descriptor: %s", originalString));

				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Tuple Window activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new TupleWindow( id,  unifiedName, serviceName, methodName,  windowSize);
			}else if(ActivityType.JOIN.getType().equals(type)){
				String unifiedName			= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String eqOperator			= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String leftServiceName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String leftParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				String rightServiceName = (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightMethodName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);
				String rightParamName 	= (String) (tokenizer.hasMoreElements()?tokenizer.nextElement():null);

				if(unifiedName		== null) throw new JoinActivityUnifiedNameRequiredException(String.format("Join activity � 'eqOperator' is required in position 3 of activity's descriptor: %s", originalString));
				if(eqOperator		== null) throw new JoinActivityEqualityOperatorRequiredException(String.format("Join activity � 'eqOperator' is required in position 3 of activity's descriptor: %s", originalString));
				if(leftServiceName	== null) throw new JoinActivityLeftSideParameterRequiredException(String.format("Join activity � 'leftServiceName' is required in position 4 of activity's descriptor: %s", originalString));
				if(leftMethodName	== null) throw new JoinActivityLeftSideParameterRequiredException(String.format("Join activity � 'leftMethodName' is required in position 5 of activity's descriptor: %s", originalString));
				if(leftParamName	== null) throw new JoinActivityLeftSideParameterRequiredException(String.format("Join activity � 'leftParamName' is required in position 6 of activity's descriptor: %s", originalString));

				if(rightServiceName	== null) throw new JoinActivityRightSideParameterRequiredException(String.format("Join activity � 'rightServiceName' is required in position 7 of activity's descriptor: %s", originalString));
				if(rightMethodName	== null) throw new JoinActivityRightSideParameterRequiredException(String.format("Join activity � 'rightMethodName' is required in position 8 of activity's descriptor: %s", originalString));
				if(rightParamName	== null) throw new JoinActivityRightSideParameterRequiredException(String.format("Join activity � 'rightParamName' is required in position 9 of activity's descriptor: %s", originalString));


				if(tokenizer.hasMoreElements()) throw new ExtraActivityDescriptorParameterException(String.format("Join activity � extra parameter in activity's descriptor: %s", originalString));

				activity = new Join( id,  unifiedName, eqOperator, 
						leftServiceName,  leftMethodName,  leftParamName,
						rightServiceName,  rightMethodName,  rightParamName);			}
			activityStore.put(id, activity);


		}
		else{ //in case of no activity descriptor  (e.g. par, init, finish)
			String id = activityString.trim();
			idx = activityString.lastIndexOf('_');
			String type = activityString.substring(0,idx);

			if(ActivityType.PAR.getType().equals(type)){
				activity = new Parallel(id);
			}else if(ActivityType.ENDPAR.getType().equals(type)){
				activity = new EndParallel(id);
			}else if(ActivityType.IN.getType().equals(type)){
				activity = new In(id);
			}else if(ActivityType.OUT.getType().equals(type)){
				activity = new Out(id);
			}
			activityStore.put(id, activity);
		}

		return activity;
	}
}
