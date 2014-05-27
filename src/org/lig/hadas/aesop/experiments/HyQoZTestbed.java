package org.lig.hadas.aesop.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.lig.hadas.aesop.queryWorkflowCost.PrologQWCostException;
import org.lig.hadas.aesop.queryWorkflowCost.QWCost;
import org.lig.hadas.aesop.qwderivation.model.QueryWorkflow;
import org.lig.hadas.aesop.syntheticQueryGen.Query;
import org.lig.hadas.aesop.syntheticQueryGen.QueryGenerationListener;
import org.lig.hadas.aesop.syntheticQueryGen.SynHQGenWrapper;
import org.lig.hadas.hybridqp.QEPBuilder;
import org.lig.hadas.hybridqp.QEPNode;
import org.lig.hadas.hybridqp.Exceptions.CyclicHypergraphException;
import org.lig.hadas.hybridqp.Log.Log;


public class HyQoZTestbed{


	private static Options options = new Options();

	static{
		@SuppressWarnings("static-access")
		Option genshqs = OptionBuilder.withArgName("n=value")
				.withValueSeparator()
				.hasArgs(2)
				.withDescription("Generation of synthetic hqs")
				.create("genshqs");

				@SuppressWarnings("static-access")
				Option Noption   = OptionBuilder
						.withDescription(  "Upper bound" )
						.hasArg()
						.create( "N" );
		
				@SuppressWarnings("static-access")
				Option outputdir   = OptionBuilder
						.withDescription(  "Output directory" )
						.hasArg()
						.create( "outputdir" );

		@SuppressWarnings("static-access")
		Option derive = OptionBuilder
				.withDescription("Derivation of the dt-functions")
				.create("derive");

				@SuppressWarnings("static-access")
				Option hqsignature   = OptionBuilder
						.withDescription(  "Hybrid query signature" )
						.hasOptionalArg()
						.create( "hqsignature" );

				@SuppressWarnings("static-access")
				Option sco   = OptionBuilder
						.withDescription(  "SCO expression" )
						.hasOptionalArg()
						.create( "sco" );
	
				@SuppressWarnings("static-access")
				Option inputfile   = OptionBuilder
						.withDescription(  "Input file" )
						.hasArg()
						.create( "inputfile" );
				
				@SuppressWarnings("static-access")
				Option outputfile   = OptionBuilder
						.withDescription(  "Output file" )
						.hasArg()
						.create( "outputfile" );

		@SuppressWarnings("static-access")		
		Option generate = OptionBuilder
				.withDescription("Generation of alternative query workflows")
				.create("generate");
			
				@SuppressWarnings("static-access")
				Option typenames   = OptionBuilder
						.withDescription(  "Type names in the list format \"[type_name(Alias, S::M),...]\" " )
						.hasArg()
						.create( "typenames" );
				
				@SuppressWarnings("static-access")
				Option dtfs   = OptionBuilder
						.withDescription(  "dt-functions in the list format \"[dtf(A,E,P,ID),...]\" " )
						.hasArg()
						.create( "dtfs" );
				
				@SuppressWarnings("static-access")
				Option controlflow   = OptionBuilder
						.withDescription(  "Defines the generetion by control-flow" )
						.create( "controlflow" );
				
				@SuppressWarnings("static-access")
				Option dataflow   = OptionBuilder
						.withDescription(  "Defines the generetion by data-flow" )
						.create( "dataflow" );

		@SuppressWarnings("static-access")		
		Option weight = OptionBuilder
				.withDescription("Weighting of quwey workflows")
				.create("weight");
		
				@SuppressWarnings("static-access")
				Option qw   = OptionBuilder
						.withDescription(  "Query workflow in functor sytax \"qw/6\" " )
						.hasArg()
						.create( "qw" );
				
				@SuppressWarnings("static-access")
				Option sla   = OptionBuilder
						.withDescription(  "SLA in functor syntax \"sla/3\" " )
						.hasArg()
						.create( "sla" );		
	
		@SuppressWarnings("static-access")		
		Option select = OptionBuilder
				.withDescription("Selection of the solution space of query workflows")
				.create("select");
		
				@SuppressWarnings("static-access")
				Option k   = OptionBuilder
						.withDescription(  "'k' for the top-k selection " )
						.hasArg()
						.create( "k" );
						
		OptionGroup optgroup = new OptionGroup();
		optgroup.addOption(genshqs)
				.addOption(derive)
				.addOption(generate)
				.addOption(weight)
				.addOption(select);;

		options.addOptionGroup(optgroup);

			//-genshqs options
			options.addOption(Noption)
				.addOption(outputdir);

			//-derive options
			options.addOption(hqsignature)
				.addOption(sco)
				.addOption(inputfile)
				.addOption(outputfile);

			//-generate options			
			options.addOption(typenames)
				.addOption(typenames)
				.addOption(dtfs)
				//.addOption(inputfile)
				//.addOption(output)
				.addOption(controlflow)
				.addOption(dataflow);
			
			//-weight options
			options.addOption(qw)
				.addOption(sla);
				//.addOption(inputfile)
				//.addOption(output)

			//-select options
			options.addOption(k);
				//.addOption(sla)
				//.addOption(inputfile)
				//.addOption(output)
	}


	public static String  QUERY_WORKFLOWS_DIR   = "QueryWorkflows";

	//HYPATIA qw generation
	public static String serviceInstancesFileName   = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_providers.txt";
	public static String serviceInterfacesFileName  = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_interfaces.txt";

	//Parameters
	private static String CHOSEN_OPTION = null;
	private static int n =0, N=0;
	private static String SYNTHETIC_QUERIES_DIR = null;
	
	private static String HQ_SIGNATURE = null;
	private static String SCO          = null;
	private static String INPUT_FILE   = null;
	
	private static String OUTPUT_FILE  = null;

	private static String TYPE_NAMES   = null;
	private static String DTFS         = null;

	private static boolean DATAFLOW    = false;
	private static boolean CONTROLFLOW = false;

	private static String QW           = null;
	private static String SLA          = null;

	private static int K               = 0;
	
	
	public static void main(String[] args) {

		validateCommandLine(args);
		System.exit(0);
		HyQoZTestbed experiments = new HyQoZTestbed();
		//		int n=Integer.valueOf(args[0]);
		//		int N=Integer.valueOf(args[1]);

		boolean forceQueryCreation = true;
		Log.off();
		HashMap<Integer, File>  syntheticQueryFiles = experiments.generateSyntheticHQs(n, N, forceQueryCreation);
		Log.on();

		boolean forceQWCreation = true;
		HashMap<Integer, File>  hypatiaQueryWorkflowsFiles = experiments.generateHypatiaQWs(n, N, syntheticQueryFiles, forceQWCreation);

		boolean forceCostComputation= true;
		@SuppressWarnings("unused")
		HashMap<Integer, File> hypatiaQueryWorkglowsWithCostsFiles = experiments.computeQWCost(n,N,hypatiaQueryWorkflowsFiles,forceCostComputation);
	}

	public HashMap<Integer, File>  generateSyntheticHQs(int n, int N, boolean forceQueryCreation) {



		HashMap<Integer, Boolean>        createFiles     = new HashMap<Integer, Boolean>();
		HashMap<Integer, String>         fileNames       = new HashMap<Integer, String>();
		HashMap<Integer, File>           files           = new HashMap<Integer, File>();
		final HashMap<Integer, BufferedWriter> bufferedWriters = new HashMap<Integer, BufferedWriter>();


		for(int i = n; i <= N; i++){
			createFiles.put(i, new Boolean(false));
			String fileName = String.format("%s/%s_DSs.txt", HyQoZTestbed.SYNTHETIC_QUERIES_DIR,String.valueOf(i)); 
			File file = new File(fileName);
			if((file.exists() && forceQueryCreation) || !file.exists()){
				fileNames.put(i, fileName);
				createFiles.remove(i);
				createFiles.put(i, new Boolean(true));
				try {
					file = new File(file.getPath()+".incomplete");
					file.createNewFile();
					files.put(i,file);
					bufferedWriters.put(i, new BufferedWriter(new FileWriter(file)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
				files.put(i,file);
		}

		final SynHQGenWrapper  querySignatureGenerator = new SynHQGenWrapper();
		QueryGenerationListener queryGenerationListener = 
				new QueryGenerationListener() {

			private BufferedWriter bw;

			@Override
			public synchronized void generated(Query q) {
				synchronized (bufferedWriters) {

					bw = bufferedWriters.get(q.getNumberOfDSs());
					try {
						bw.write(q.querySignature()+"\n");
						bw.write(q.getSco()+"\n");
						bw.write(q.getHqsl()+"\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

		querySignatureGenerator.addListener(queryGenerationListener);

		System.out.format("\nProcessing synthetic hybrid query generation of %d to %d data services . . .\n", n, N);
		ArrayList<Query> queryList = null;

		for (int i = n; i <= N; i++) {
			System.out.println(String.format("\n ... %d DSs ... ", i));

			if(createFiles.get(i)){
				queryList = querySignatureGenerator.generateSyntheticHQsFor(i);
				try {
					if(queryList.size()==querySignatureGenerator.totalOfQueries(i)){
						bufferedWriters.get(i).close();
						File incompleteFile = files.get(i);
						File file = new File(fileNames.get(i));
						files.remove(i);
						files.put(i, file);
						if(incompleteFile.renameTo(file))
							System.out.println(String.format("\nThe [%d] queries for %d DSs are COMPLETE and SAVED into the file %s", queryList.size(),i, file.getPath()));
						else{
							file.delete();
							System.out.println(String.format("\nThe [%d] queries for %d DSs are COMLETE and SAVED in the temporary file %s (because of a permissions problem)", queryList.size(), i, incompleteFile.getPath()));
						}
					}
					else{
						System.out.println(String.format("\nThe [%d] queries for %d DSs are INCOMPLETE and SAVED into the file %s", queryList.size(), i, files.get(i).getPath()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


		System.out.println("\n. . .Finishing generation\n");

		return files;
	}

	public HashMap<Integer, File>  generateHypatiaQWs(int n, int N, HashMap<Integer, File> syntheticQueryFiles, boolean forceQueryCreation) {

		HashMap<Integer, Boolean>        createFiles     = new HashMap<Integer, Boolean>();
		HashMap<Integer, String>         fileNames       = new HashMap<Integer, String>();
		HashMap<Integer, File>           files           = new HashMap<Integer, File>();
		HashMap<Integer, File>           cyclicQWsfiles  = new HashMap<Integer, File>();
		HashMap<Integer, Integer> 		 totalQWsResume        = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> 		 cyclicHQsResume       = new HashMap<Integer, Integer>();
		final HashMap<Integer, BufferedWriter> bufferedWriters = new HashMap<Integer, BufferedWriter>();
		final HashMap<Integer, BufferedWriter> bufferedWritersCyclic = new HashMap<Integer, BufferedWriter>();


		for (int i = n; i <= N; i++) {

			createFiles.put(i, new Boolean(false));
			String fileName = String.format("%s/%s_QWs.txt", HyQoZTestbed.QUERY_WORKFLOWS_DIR,String.valueOf(i)); 
			File file = new File(fileName);
			if((file.exists() && forceQueryCreation) || !file.exists()){
				fileNames.put(i, fileName);
				createFiles.remove(i);
				createFiles.put(i, new Boolean(true));
				try {
					file = new File(file.getPath()+".incomplete");
					file.createNewFile();
					files.put(i,file);
					bufferedWriters.put(i, new BufferedWriter(new FileWriter(file)));
					File cyclicFile = new File(file.getPath().replace("incomplete", "cyclic"));
					cyclicFile.createNewFile();
					cyclicQWsfiles.put(i,cyclicFile);
					bufferedWritersCyclic.put(i, new BufferedWriter(new FileWriter(cyclicFile)));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
				files.put(i,file);

		}
		BufferedReader br;
		BufferedWriter bw;
		Log.off();
		System.out.format("\nObtaining Hypatia QWs for %d to %d data services . . .\n", n, N);

		for (int i = n; i <= N; i++) {
			br=null;
			bw=null;
			System.out.println(String.format("\n ... %d DSs ... ", i));
			if(createFiles.get(i)){
				try {
					br = new BufferedReader( new FileReader(syntheticQueryFiles.get(i)));
					bw = bufferedWriters.get(i);
					String line = null;
					int j = 0;
					int numberOfDSs=0;      
					int numberOfBindJoins=0; 
					int numberOfJoins=0; 
					int numberOfFilterings=0;             
					int numberOfBlockingProjections=0; 
					int numberOfNonBlockingProjections=0; 
					String sco="";
					String hsql="";

					QEPBuilder builder = new QEPBuilder(HyQoZTestbed.serviceInstancesFileName, HyQoZTestbed.serviceInterfacesFileName);

					boolean noCyclic = true;
					int totalQWs=0;
					int cyclicHQs=0;

					while((line=br.readLine()) != null){
						switch (++j % 3) {
						case 1://Signature

							String signs[] = line.split("_");
							numberOfDSs                    = Integer.valueOf(signs[0]);      
							numberOfBindJoins              = Integer.valueOf(signs[1]); 
							numberOfJoins                  = Integer.valueOf(signs[2]); 
							numberOfFilterings             = Integer.valueOf(signs[3]);             
							numberOfBlockingProjections    = Integer.valueOf(signs[4]); 
							numberOfNonBlockingProjections = Integer.valueOf(signs[5]); 
							break;
						case 2: //SCO
							if(line.startsWith("pi"))
								sco = line;
							break;
						case 0: //HSQL
							if(line.startsWith("SELECT")){
								hsql = line;
								Query q = new Query(numberOfDSs, 
										numberOfJoins, 
										numberOfBindJoins,
										numberOfFilterings, 
										numberOfBlockingProjections, 
										numberOfNonBlockingProjections, 
										sco, 
										hsql);
								Log.out.println(q.getCreationInstruction());
								Log.out.println(q.toDebbugingString());
								QEPNode root;
								try {

									root = builder.constructQEP(q.getHqsl());
									QueryWorkflow qw = HypatiaToHyQoZMapper.getQW(root);
									//									QueryWorkflow qw = builder.getQW(root);
									String qwFunctor=qw.toFunctor().replaceAll("\n", "").replaceAll("[ |\t]+", " "); 
									Log.out.println("\t\t\t\t\t\tQW: "+qwFunctor);
									Log.out.println();
									bw.append(q.getCreationInstruction()+"\n");
									bw.append(qwFunctor+"\n");
									totalQWs++;

								} catch (CyclicHypergraphException e) {
									Log.err.println(e.getMessage()+q.getCreationInstruction());
									Log.err.println();
									bufferedWritersCyclic.get(i).append(q.getCreationInstruction()+"\n");
									noCyclic=false;
									cyclicHQs++;
								}
							}
							break;
						}
					}


					bufferedWritersCyclic.get(i).close();

					if(noCyclic) cyclicQWsfiles.get(i).delete();
					br.close();
					bw.close();

					totalQWsResume.put(i, totalQWs);
					cyclicHQsResume.put(i, cyclicHQs);
					File incompleteFile = files.get(i);
					File file = new File(fileNames.get(i));
					files.remove(i);
					files.put(i, file);
					int totalTries = totalQWsResume.get(i)+cyclicHQsResume.get(i);
					if(incompleteFile.renameTo(file)){
						System.out.print(String.format("\nThe HYPATIA QWs for %d DSs are COMPLETE and SAVED to %s", i, file.getPath()));
						DecimalFormat totalFormater = new DecimalFormat("#######");
						DecimalFormat percentageFormater = new DecimalFormat("##.##%");
						System.out.format("\t%7s [%6s]\t%7s [%6s]\n", 
								totalFormater.format(totalQWsResume.get(i)),
								percentageFormater.format((double)totalQWsResume.get(i)/totalTries),
								totalFormater.format(cyclicHQsResume.get(i)),
								percentageFormater.format((double)cyclicHQsResume.get(i)/totalTries));


					}
					else{
						file.delete();
						System.out.println(String.format("\nThe HYPATIA QWs for %d DSs are COMLETE and SAVED to %s (because of a permissions problem)", i, incompleteFile.getPath()));
					}



				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}//if(createFiles.get(i))

		}//for (int i = n; i <= N; i++) {
		Log.on();

		System.out.println("\n. . .Finishing obtention of QWs\n");

		return files;
	}

	public HashMap<Integer, File>  computeQWCost(int n, int N, HashMap<Integer, File> hypatiaQueryWorkflows, boolean forceCostComputation) {

		HashMap<Integer, Boolean>        createFiles     = new HashMap<Integer, Boolean>();
		HashMap<Integer, String>         fileNames       = new HashMap<Integer, String>();
		HashMap<Integer, File>           files           = new HashMap<Integer, File>();
		final HashMap<Integer, BufferedWriter> bufferedWriters = new HashMap<Integer, BufferedWriter>();


		for (int i = n; i <= N; i++) {


			String fileName = String.format("%s/%s_QWs_costs.txt", HyQoZTestbed.QUERY_WORKFLOWS_DIR,String.valueOf(i)); 
			File file = new File(fileName);

			if((file.exists() && forceCostComputation) || !file.exists()){
				fileNames.put(i, fileName);
				createFiles.remove(i);
				createFiles.put(i, new Boolean(true));
				try {
					file = new File(file.getPath()+".incomplete");
					file.createNewFile();
					files.put(i,file);
					bufferedWriters.put(i, new BufferedWriter(new FileWriter(file)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				files.put(i,file);
				createFiles.put(i, new Boolean(false));
			}

		}
		BufferedReader br;
		BufferedWriter bw;
		Log.off();
		System.out.format("\nComputing Hypatia QWs costs for %d to %d data services . . .\n", n, N);

		for (int i = n; i <= N; i++) {
			br=null;
			bw=null;
			System.out.println(String.format("\n ... %d DSs ... ", i));
			if(createFiles.get(i)){
				try {
					br = new BufferedReader( new FileReader(hypatiaQueryWorkflows.get(i)));
					bw = bufferedWriters.get(i);
					String line = null;
					int lineNumber = 0;
					while((line=br.readLine()) != null){
						lineNumber++;
						switch (lineNumber%2) {
						case 0: //qw
							System.out.println(line);
							bw.append(line+"\n");
							QWCost qwCost = new QWCost(line);
							try {
								@SuppressWarnings("unused")
								HashMap<String, Double> costMap = qwCost.getCost();
								bw.append(qwCost.toFunctor()+"\n");
								System.out.println(qwCost.toFunctor());
							} catch (PrologQWCostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;

						case 1: //create_hq
							bw.append(line+"\n");
							System.out.println(line);
							break;
						}
					}
					bw.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					bufferedWriters.get(i).close();
					File incompleteFile = files.get(i);
					File file = new File(fileNames.get(i));
					files.remove(i);
					files.put(i, file);
					if(incompleteFile.renameTo(file))
						System.out.println(String.format("\nThe QWs costs for %d  DSs  are COMPLETE and SAVED into the file %s", i, file.getPath()));
					else{
						file.delete();
						System.out.println(String.format("\nThe QWs costs for %d  DSs  are COMPLETE and SAVED into the file %s (because of a permissions problem)",  i, incompleteFile.getPath()));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}//if(createFiles.get(i))

		}//for (int i = n; i <= N; i++) {
		Log.on();


		System.out.println("\n. . .Finishing computation of QWs' costs\n");

		return files;
	}

	private static void validateCommandLine(String [] args){
		CommandLineParser parser = new PosixParser(); 
		HelpFormatter formatter  = new HelpFormatter();
		CommandLine line=null;
		try {
			// parse the command line arguments
			line = parser.parse( options, args );
		}
		catch( ParseException e ) {
			// oops, something went wrong
			formatter.printHelp( "hyqoztb", options );
			e.printStackTrace();
			System.exit(1);
		}
	//+Synthetic HQ generation
		if(line.hasOption("genshqs")){
			CHOSEN_OPTION="genshqs";
			try {
				Properties properties = line.getOptionProperties("genshqs");
				n = Integer.valueOf(properties.getProperty("n"));
				if(line.hasOption("N"))
					N = Integer.valueOf(line.getOptionValue("N"));
				else
					N=n;
			} catch (NumberFormatException e) {
				formatter.printHelp( "hyqoztb", options );
				e.printStackTrace();
				System.exit(1);
			}
			if(line.hasOption("outputdir")){
				SYNTHETIC_QUERIES_DIR = line.getOptionValue("outputdir");
				File f = new File(SYNTHETIC_QUERIES_DIR);
				if (! f.exists()){
					System.err.println("The directory '"+ SYNTHETIC_QUERIES_DIR + "' does not exist");
					System.exit(1);
				}
				if(!f.isDirectory()){
					System.err.println("The path '"+ SYNTHETIC_QUERIES_DIR + "' does not correspond to a directory");
					System.exit(1);
				}
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option 'outputdir' required");
				System.exit(1);
			}
			System.out.println("[CML] -"+CHOSEN_OPTION);
			System.out.println("[CML] n="+n);
			System.out.println("[CML] N="+N);
			System.out.println("[CML] -outputdir '"+SYNTHETIC_QUERIES_DIR+"'");
			
			return;
		}
	//-Synthetic HQ generation
	
	//+Derivation of dt-functions
		if(line.hasOption("derive")){
			CHOSEN_OPTION="derive";
			if(line.hasOption("hqsignature") ^ line.hasOption("sco")){
				if(line.hasOption("hqsignature")){
					HQ_SIGNATURE = null;
					if((HQ_SIGNATURE=line.getOptionValue("hqsignature"))!=null ^ line.hasOption("inputfile") ){
						if(HQ_SIGNATURE != null){
							System.out.println("[CML] -"+CHOSEN_OPTION);
							System.out.println("[CML] -hqsignature '"+HQ_SIGNATURE+"'");
						}
						if(line.hasOption("inputfile")){
							INPUT_FILE = line.getOptionValue("inputfile");
							File f = new File(INPUT_FILE);
							if (! f.exists()){
								System.err.println("The inputfile path '"+ INPUT_FILE + "' does not exist");
								System.exit(1);
							}
							if(f.isDirectory()){
								System.err.println("The inputfile path '"+ INPUT_FILE + "' does not correspond to a file");
								System.exit(1);
							}
							System.out.println("[CML] -"+CHOSEN_OPTION);
							System.out.println("[CML] -hqsignature -inputfile '"+INPUT_FILE+"'");
						}
					}
					else{
						formatter.printHelp( "hyqoztb", options );
						System.err.println("Option '-hqsignature <hqsignature>' xor '-hqsignature -inputfile <filename>' required for '-derive'");
						System.exit(1);
					}
	
				}
				if(line.hasOption("sco")){
					SCO = null;
					if((SCO=line.getOptionValue("sco"))!=null ^ line.hasOption("inputfile") ){
						if(SCO != null){
							System.out.println("[CML] -"+CHOSEN_OPTION);
							System.out.println("[CML] -sco '"+SCO+"'");
						}
						if(line.hasOption("inputfile")){
							INPUT_FILE = line.getOptionValue("inputfile");
							File f = new File(INPUT_FILE);
							if (! f.exists()){
								System.err.println("The inputfile path '"+ INPUT_FILE + "' does not exist");
								System.exit(1);
							}
							if(f.isDirectory()){
								System.err.println("The inputfile path '"+ INPUT_FILE + "' does not correspond to a file");
								System.exit(1);
							}
							System.out.println("[CML] -"+CHOSEN_OPTION);
							System.out.println("[CML] -sco -inputfile '"+INPUT_FILE+"'");
						}
					}
					else{
						formatter.printHelp( "hyqoztb", options );
						System.err.println("Option '-sco <sco>' xor '-sco -inputfile <filename>' required for '-derive'");
						System.exit(1);
					}
				}
				if(line.hasOption("outputfile")){
					OUTPUT_FILE = line.getOptionValue("outputfile");
					System.out.println("[CML] -outputfile "+OUTPUT_FILE);
				}
				else{
					formatter.printHelp( "hyqoztb", options );
					System.err.println("Option '-outputfile <filename>' required for '-derive'");
					System.exit(1);
				}
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-hqsignature <hqsignature>' xor '-sco <sco>' required for '-derive'");
				System.exit(1);
			}
				
		}
	//-Derivation of dt-functions
		
	//+Generation of query workflows
		if(line.hasOption("generate")){
			CHOSEN_OPTION="generate";
			System.out.println("[CML] -"+CHOSEN_OPTION);
			if( (line.hasOption("typenames") && line.hasOption("dtfs"))
				^ 
				line.hasOption("inputfile")){
				if(line.hasOption("typenames") && line.hasOption("dtfs")){
					TYPE_NAMES  = line.getOptionValue("typenames");
					DTFS = line.getOptionValue("dtfs");
					System.out.println("[CML] -typenames "+TYPE_NAMES);
					System.out.println("[CML] -dtfs "+DTFS);
				}
				if(line.hasOption("inputfile")){
					INPUT_FILE = line.getOptionValue("inputfile");
					File f = new File(INPUT_FILE);
					if (! f.exists()){
						System.err.println("The inputfile path '"+ INPUT_FILE + "' does not exist");
						System.exit(1);
					}
					if(f.isDirectory()){
						System.err.println("The inputfile path '"+ INPUT_FILE + "' does not correspond to a file");
						System.exit(1);
					}
					System.out.println("[CML] -inputfile "+INPUT_FILE);
				}
				
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-typenames <typenames> -dtfs <dtfs>' xor '-inputfile <filename>' required for '-generate'");
				System.exit(1);
			}
			
			if(line.hasOption("controlflow") ^ line.hasOption("dataflow")){
				CONTROLFLOW = Boolean.valueOf(line.hasOption("controlflow"));
				DATAFLOW = !CONTROLFLOW;
				if(CONTROLFLOW) System.out.println("[CML] -controflow");
				if(DATAFLOW)    System.out.println("[CML] -dataflow");
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-controflow' xor '-dataflow' required for '-generate'");
				System.exit(1);
			}
			
			if(line.hasOption("outputfile")){
				OUTPUT_FILE = line.getOptionValue("outputfile");
				System.out.println("[CML] -outputfile "+OUTPUT_FILE);
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-outputfile <filename>' required for '-generate'");
				System.exit(1);
			}
				
		}
	//-Generation of query workflows	
		
	//+Weighting of query workflows
		if(line.hasOption("weight")){
			CHOSEN_OPTION="weight";
			System.out.println("[CML] -"+CHOSEN_OPTION);
			if( (line.hasOption("qw") || line.hasOption("sla"))
				^ 
				line.hasOption("inputfile")){
				if(line.hasOption("qw") && line.hasOption("sla") && !line.hasOption("inputfile")){
					QW  = line.getOptionValue("qw");
					SLA = line.getOptionValue("sla");
					System.out.println("[CML] -qw "+QW);
					System.out.println("[CML] -sla "+SLA);
				}
				else if(line.hasOption("inputfile") && !(line.hasOption("qw") && line.hasOption("sla"))){
					INPUT_FILE = line.getOptionValue("inputfile");
					File f = new File(INPUT_FILE);
					if (! f.exists()){
						System.err.println("The inputfile path '"+ INPUT_FILE + "' does not exist");
						System.exit(1);
					}
					if(f.isDirectory()){
						System.err.println("The inputfile path '"+ INPUT_FILE + "' does not correspond to a file");
						System.exit(1);
					}
					System.out.println("[CML] -inputfile "+INPUT_FILE);
				}
				else{
					formatter.printHelp( "hyqoztb", options );
					System.err.println("Option '-qw <qw> -sla <sla>' xor '-inputfile <filename>' required for '-weight'");
					System.exit(1);
				}
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-qw <qw> -sla <sla>' xor '-inputfile <filename>' required for '-weight'");
				System.exit(1);
			}
	
			if(line.hasOption("outputfile")){
				OUTPUT_FILE = line.getOptionValue("outputfile");
				System.out.println("[CML] -outputfile "+OUTPUT_FILE);
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-outputfile <filename>' required for '-weight'");
				System.exit(1);
			}	
		}
	//-Weighting of query workflows	
	
		
	//+Selection of the solution space
		if(line.hasOption("select")){
			CHOSEN_OPTION="select";
			System.out.println("[CML] -"+CHOSEN_OPTION);
			if( line.hasOption("inputfile") && line.hasOption("sla") && line.hasOption("k")){
				INPUT_FILE = line.getOptionValue("inputfile");
				File f = new File(INPUT_FILE);
				if (! f.exists()){
					System.err.println("The inputfile path '"+ INPUT_FILE + "' does not exist");
					System.exit(1);
				}
				if(f.isDirectory()){
					System.err.println("The inputfile path '"+ INPUT_FILE + "' does not correspond to a file");
					System.exit(1);
				}
				System.out.println("[CML] -inputfile "+INPUT_FILE);
				
				SLA = line.getOptionValue("sla");
				System.out.println("[CML] -sla "+SLA);
				
				try {
					K = Integer.valueOf(line.getOptionValue("k"));
					System.out.println("[CML] -k "+K);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					formatter.printHelp( "hyqoztb", options );
					System.err.println("Option '-k <k>' for option '-select' requires an integer value");
					System.exit(1);
				}
			}
			
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-inputfile <filename> -sla <sla> -k <k>' required for '-select'");
				System.exit(1);
			}
	
			
			if(line.hasOption("outputfile")){
				OUTPUT_FILE = line.getOptionValue("outputfile");
				System.out.println("[CML] -outputfile "+OUTPUT_FILE);
			}
			else{
				formatter.printHelp( "hyqoztb", options );
				System.err.println("Option '-outputfile <filename>' required for '-select'");
				System.exit(1);
			}	
		}
	//-Selection of the solution space
	}

}