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

public class Experiments{


	//synthetic query generation
	public static String  SYNTHETIC_QUERIES_DIR = "SyntheticHQs";
	public static String  QUERY_WORKFLOWS_DIR   = "QueryWorkflowsDir";

	//HYPATIA qw generation
	public static String serviceInstancesFileName   = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_providers.txt";
	public static String serviceInterfacesFileName  = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_interfaces.txt";

	public static void main(String[] args) {
		Experiments experiments = new Experiments();
		int n=Integer.valueOf(args[0]);
		int N=Integer.valueOf(args[1]);
		
		boolean forceQueryCreation = true;
		Log.off();
		HashMap<Integer, File>  syntheticQueryFiles = experiments.generateSyntheticHQs(n, N, forceQueryCreation);
		Log.on();

		boolean forceQWCreation = true;
		HashMap<Integer, File>  hypatiaQueryWorkflowsFiles = experiments.generateHypatiaQWs(n, N, syntheticQueryFiles, forceQWCreation);

		boolean forceCostComputation= true;
		HashMap<Integer, File> hypatiaQueryWorkglowsWithCostsFiles = experiments.computeQWCost(n,N,hypatiaQueryWorkflowsFiles,forceCostComputation);
	}

	public HashMap<Integer, File>  generateSyntheticHQs(int n, int N, boolean forceQueryCreation) {

		HashMap<Integer, Boolean>        createFiles     = new HashMap<Integer, Boolean>();
		HashMap<Integer, String>         fileNames       = new HashMap<Integer, String>();
		HashMap<Integer, File>           files           = new HashMap<Integer, File>();
		final HashMap<Integer, BufferedWriter> bufferedWriters = new HashMap<Integer, BufferedWriter>();


		for(int i = n; i <= N; i++){
			createFiles.put(i, new Boolean(false));
			String fileName = String.format("%s/%s_DSs.txt", Experiments.SYNTHETIC_QUERIES_DIR,String.valueOf(i)); 
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
			private int count = 0;
			private BufferedWriter bw;


			@Override
			public synchronized void generated(Query q) {
				synchronized (bufferedWriters) {
					//					if(q.getNumberOfDSs()!= last){
					//						System.out.print(String.format("\n[%2d] ",q.getNumberOfDSs()));
					//						last=q.getNumberOfDSs();
					//						count=1;
					//					} else{
					//						count++;
					//						if((count-1) % 80 ==0) System.out.print("\n     ");
					//					}
					count++;


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
			String fileName = String.format("%s/%s_QWs.txt", Experiments.QUERY_WORKFLOWS_DIR,String.valueOf(i)); 
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

					QEPBuilder builder = new QEPBuilder(Experiments.serviceInstancesFileName, Experiments.serviceInterfacesFileName);

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
									QueryWorkflow qw = builder.getQW(root);

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


			String fileName = String.format("%s/%s_QWs_costs.txt", Experiments.QUERY_WORKFLOWS_DIR,String.valueOf(i)); 
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
}
