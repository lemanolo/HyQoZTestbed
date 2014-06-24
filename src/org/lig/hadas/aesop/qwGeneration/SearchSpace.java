package org.lig.hadas.aesop.qwGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.lig.hadas.aesop.syntheticQueryGen.PrologGeneratorException;

public class SearchSpace extends HashMap<QueryWorkflowContext, String> implements PrologProgramOutputListener{
	/**
	 *
	 */
	private static final long serialVersionUID = 439051707221883399L;

	private static String plGeneratorDir          = "/Users/aguacatin/Research/HADAS/PhD/Prolog/hyqoz_qwGenerator";
	private static String configGeneratorLayout   = plGeneratorDir+"/config.pl.layout";
	private static String generationScript        = plGeneratorDir+"/run_from_java.sh";

	private static String plWeighterDir          = "/Users/aguacatin/Research/HADAS/PhD/Prolog/hyqoz_qwWeighter";
	private static String configWeighterLayout   = plWeighterDir+"/config.pl.layout";
	private static String weightingScript        = plWeighterDir+"/run_from_java.sh";

	private static String DTFS_KEY                           = "#DTFS#";
	private static String DTYPES_KEY                         = "#DTYPES#";
	private static String FLOW_KEY                           = "#FLOW#";
	private static String FLOW_DEFAULT                       = "cf";

	private static String ACTIVITIES_KEY					 = "#ACTIVITIES#";
	private static String QW_KEY					 		 = "#QW#";
	private static String COST_FORMULATION					 = "#COST_FORMULATION#";
	
	private static String COST_ESTIMATION_APPROACH_KEY       = "#COST_ESTIMATION_APPROACH#";
	@SuppressWarnings("unused")
	private static String COST_ESTIMATION_APPROACH_DEFAULT    = "partialcf";

	private static String OUTPUTFILENAME_KEY                 = "#OUTPUTFILENAME#";
	private static String OUTPUTFILENAME_DEFAULT             = "none";

	private static String SAFE_GENERATION_KEY                = "#SAFE_GENERATION#";
	private static String SAFE_GENERATION_DEFAULT            = "unsafe";

	private static String NO_REDUNDANT_RELATIONS_KEY         = "#NO_REDUNDANT_RELATIONS#";
	private static String NO_REDUNDANT_RELATIONS_DEFAULT     = "false";

	private static String NEW_RELATIONS_COMPUTATION_KEY      = "#NEW_RELATIONS_COMPUTATION#";
	private static String NEW_RELATIONS_COMPUTATION_DEFAULT  = "global";

	private static String NEXT_RELATION_SELECTION_KEY        = "#NEXT_RELATION_SELECTION#";
	private static String NEXT_RELATION_SELECTION_DEFAULT    = "member";

	private static String MEMOIZATION_KEY                    = "#MEMOIZATION#";
	private static String MEMOIZATION_DEFAULT                = "true";

	private static String WEIGHTING_KEY                      = "#WEIGHTING#";
	private static String WEIGHTING_DEFAULT                  = "cost";

	//+INPUT
	private String type_names_string          = null;
	private String dtfs_string                = null;
	private String flow                       = null;
	//-INPUT

	private String activities_string                = null;

	//+CONFIG
	private String outputfilename             = null;
	private String safe_generation            = null;
	private String no_redundant_relations     = null;
	private String new_relations_computation  = null;
	private String next_relation_selection    = null;
	private String memoization                = null;
	private String weighting                  = null;
	//-CONFIG




	public SearchSpace(String type_names_string, String dtfs_string, String flow) throws PrologGeneratorException {
		this.type_names_string = type_names_string;
		this.dtfs_string       = dtfs_string;
		this.flow              = flow;
		if(this.flow == null)
			this.flow = SearchSpace.FLOW_DEFAULT;
		this.generateQWS();
	}

	public SearchSpace(String type_names_string, String activities_string) {
		this.type_names_string = type_names_string;
		this.activities_string = activities_string;
		this.flow=null;
	}


	private void generateQWS() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {		
			String sCurrentLine;

			br = new BufferedReader(new FileReader(configGeneratorLayout));
			String configString="";
			while ((sCurrentLine = br.readLine()) != null)
				configString+=sCurrentLine+"\n";
			configString=configString
					.replaceAll(SearchSpace.DTFS_KEY,this.dtfs_string.replaceAll("\\.", "::"))
					.replaceAll(SearchSpace.DTYPES_KEY,this.type_names_string.replaceAll("\\.", "::"))
					.replaceAll(SearchSpace.FLOW_KEY,this.flow);

			configString=configString
					.replaceAll(SearchSpace.SAFE_GENERATION_KEY,           this.safe_generation==null?SearchSpace.SAFE_GENERATION_DEFAULT:this.safe_generation)
					.replaceAll(SearchSpace.OUTPUTFILENAME_KEY,            this.outputfilename==null?SearchSpace.OUTPUTFILENAME_DEFAULT:this.outputfilename)
					.replaceAll(SearchSpace.NO_REDUNDANT_RELATIONS_KEY,    this.no_redundant_relations==null?SearchSpace.NO_REDUNDANT_RELATIONS_DEFAULT:this.no_redundant_relations)
					.replaceAll(SearchSpace.NEW_RELATIONS_COMPUTATION_KEY, this.new_relations_computation==null?SearchSpace.NEW_RELATIONS_COMPUTATION_DEFAULT:this.new_relations_computation)
					.replaceAll(SearchSpace.NEXT_RELATION_SELECTION_KEY,   this.next_relation_selection==null?SearchSpace.NEXT_RELATION_SELECTION_DEFAULT:this.next_relation_selection)
					.replaceAll(SearchSpace.MEMOIZATION_KEY,               this.memoization==null?SearchSpace.MEMOIZATION_DEFAULT:this.memoization)
					.replaceAll(SearchSpace.WEIGHTING_KEY,                 this.weighting==null?SearchSpace.WEIGHTING_DEFAULT:this.weighting);
			//System.out.println("config: "+configString);


			//+Creating config file				
			File configDir = new File(plGeneratorDir+"/CONFIG");
			if(!configDir.exists())configDir.mkdirs();

			File configFile = File.createTempFile("config", ".pl", configDir);
			//System.out.println("configFile: "+configFile.getAbsolutePath());

			bw = new BufferedWriter(new FileWriter(configFile));
			//-Creating config file

			bw.write(configString);
			bw.close();

			CommandLine cmdLine = new CommandLine(generationScript);
			cmdLine.addArgument(configFile.getAbsolutePath());

			PrologProgramOutputCollector stdout = new PrologProgramOutputCollector();
			stdout.setListener(this);

			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecutor exec = new DefaultExecutor();
			exec.setStreamHandler(psh);
			try {
				exec.execute(cmdLine);
			} catch (ExecuteException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
				if (bw != null)bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("\n\n\t TOTAL QWS: "+this.size());
	}

	public String getType_names_string() {
		return this.type_names_string;
	}

	public String getDtfs_string() {
		return this.dtfs_string;
	}

	public String getFlow() {
		return flow;
	}

	@Override
	public void newLine(String line) throws PrologGeneratorException {
		QueryWorkflowContext qwContext = null;
		if(line.startsWith("QW")){
			String qw_string=null;
			String clean_qw_string=null;

			qw_string=line.replaceAll("::", ".").replaceAll(" *QW *= *", "");
			clean_qw_string = qw_string.replaceAll("end_par_\\w+", "end_par").replaceAll("par_\\w+", "par");

			qwContext = new QueryWorkflowContext(clean_qw_string);
			if(!this.containsKey(qwContext)){
				this.put(qwContext, qw_string);
				System.out.println("---\t------NEW: "+clean_qw_string);
				System.out.println("   \t  MD5 HEX: "+qwContext.getId());
				System.out.println("   \tSignature: "+qwContext.getSignature());
			}else{

			}
		}
		else if(line.startsWith("DTYPES")){
			if(type_names_string==null)
				type_names_string=line.replaceAll("::", ".").replaceAll(" *DTYPES *= *", "");
		} else if(line.startsWith("ACTIVITIES")){
			if(activities_string==null)
				activities_string=line.replaceAll("::", ".").replaceAll(" *ACTIVITIES *= *", "");
		}else if(line.startsWith("TRY")) System.err.println(line);
		else{
			throw new PrologGeneratorException("\n[Unexpected output from Prolog]: "+line+"\nDTFS: "+this.dtfs_string);
		}		
	}

	public QueryWorkflowContext put(String qw_string){
		qw_string=qw_string.replaceAll("::", ".").replaceAll(" *QW *= *", "");
		String clean_qw_string = qw_string.replaceAll("end_par_\\w+", "end_par").replaceAll("par_\\w+", "par");
		QueryWorkflowContext qwContext = new QueryWorkflowContext(clean_qw_string);
		this.put(qwContext,qw_string);
		return qwContext;
	}

	public static String weightQW(String type_names_string,
			String activities_string,
			QWCostFormulations costFormulation,
			QWCostEstimationApproach costEstimationApproach,
			final String qw_string ){
		BufferedReader br = null;
		BufferedWriter bw = null;
		String cost_string =null;
		
		if(costEstimationApproach==null)
			costEstimationApproach = QWCostEstimationApproach.UNREQUIRED;
		
		try {		
			String sCurrentLine;

			br = new BufferedReader(new FileReader(SearchSpace.configWeighterLayout));
			String configString="";
			while ((sCurrentLine = br.readLine()) != null)
				configString+=sCurrentLine+"\n";
			configString=configString
					.replaceAll(SearchSpace.DTYPES_KEY,type_names_string.replaceAll("\\.", "::"))
					.replaceAll(SearchSpace.ACTIVITIES_KEY,activities_string.replaceAll("\\.", "::"))
					.replaceAll(SearchSpace.COST_FORMULATION,costFormulation.getFormulation())
					.replaceAll(SearchSpace.QW_KEY,qw_string.replaceAll("\\.", "::"))
					.replaceAll(SearchSpace.COST_ESTIMATION_APPROACH_KEY,  costEstimationApproach.toString());

//			System.out.println("config: \n"+configString);


			//+Creating config file				
			File configDir = new File(SearchSpace.plWeighterDir+"/CONFIG");
			if(!configDir.exists())configDir.mkdirs();

			File configFile = File.createTempFile("config", ".pl", configDir);
//			System.out.println("configFile: "+configFile.getAbsolutePath());

			bw = new BufferedWriter(new FileWriter(configFile));
			//-Creating config file

			bw.write(configString);
			bw.close();

			CommandLine cmdLine = new CommandLine(SearchSpace.weightingScript);
			cmdLine.addArgument(configFile.getAbsolutePath());

			PrologProgramOutputCollector stdout = new PrologProgramOutputCollector();
			PrologProgramOutputListener prologListener = new PrologProgramOutputListener() {
				ArrayList<String> bufferedOutput = new ArrayList<String>();
				@Override
				public void newLine(String line) throws PrologGeneratorException {
					if(line.startsWith("COST")){
						String cost_string=line.replaceAll("::", ".").replaceAll(" *COST *= *", "");
						this.bufferedOutput.add(cost_string);
					}else if(line.startsWith("TRY")) System.err.println(line);
					else{
						throw new PrologGeneratorException("\n[Unexpected output from Prolog]: "+line+"\nQW: "+qw_string);
					}	

				}
				@Override
				public ArrayList<String> getBufferedOutput() {
					return this.bufferedOutput;
				}
			};

			stdout.setListener(prologListener);

			PumpStreamHandler psh = new PumpStreamHandler(stdout);
			DefaultExecutor exec = new DefaultExecutor();
			exec.setStreamHandler(psh);

			try {
				exec.execute(cmdLine);
				ArrayList<String> output = prologListener.getBufferedOutput();
				while(output.size()==0);
				cost_string=output.get(0);
//				for (String string : output) {
//					System.out.println("[output] "
//							+ qw_string.replaceAll("end_par_\\w+", "end_par").replaceAll("par_\\w+", "par")
//							+" "+string);
//				}
			} catch (ExecuteException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
				if (bw != null)bw.close();
			} catch (IOException ex) { ex.printStackTrace(); }
		}
		return cost_string;
	}

	public String getActivities_string() {
		return activities_string;
	}

	@Override
	public ArrayList<String> getBufferedOutput() {
		return null;
	}

}
