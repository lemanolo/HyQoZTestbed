package org.lig.hadas.aesop.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.lig.hadas.aesop.qwderivation.model.QueryWorkflow;
import org.lig.hadas.aesop.syntheticQueryGen.Query;
import org.lig.hadas.aesop.syntheticQueryGen.QueryGenerationListener;
import org.lig.hadas.aesop.syntheticQueryGen.QuerySignatureGenerator;
import org.lig.hadas.hybridqp.QEPBuilder;
import org.lig.hadas.hybridqp.QEPNode;
import org.lig.hadas.hybridqp.Exceptions.CyclicHypergraphException;
import org.lig.hadas.hybridqp.Log.Log;

import com.google.common.base.Splitter;

public class Experiments{


	//synthetic query generation
	public static String  SYNTHETIC_QUERIES_DIR = "SyntheticQueries";
	public static String  QUERY_WORKFLOWS_DIR   = "QueryWorkflowsDir";

	//HYPATIA qw generation
	public static String serviceInstancesFileName   = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_providers.txt";
	public static String serviceInterfacesFileName  = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_interfaces.txt";

	public static void main(String[] args) {
		Experiments experiments = new Experiments();
		int n=1;
		int N=12;
		boolean forceQueryCreation = false;
		HashMap<Integer, File>  syntheticQueryFiles = experiments.generateSyntheticQueries(n, N, forceQueryCreation);
		BufferedReader br =null;
		boolean forceQWCreation = true;
		HashMap<Integer, File>  hypatiaQueryWorkflowsFiles = experiments.generateHypatiaQWs(n, N, syntheticQueryFiles, forceQWCreation);

	}

	public HashMap<Integer, File>  generateSyntheticQueries(int n, int N, boolean forceQueryCreation) {

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

		final QuerySignatureGenerator  querySignatureGenerator = new QuerySignatureGenerator();
		querySignatureGenerator.addListener(new QueryGenerationListener() {
			private int i = 1;
			private BufferedWriter bw;
			private int last = 0;
			@Override
			public synchronized void generated(Query q) {
				synchronized (bufferedWriters) {
					if(q.getNumberOfDSs()!= last){
						System.out.print(String.format("\n[%2d] ",q.getNumberOfDSs()));
						last=q.getNumberOfDSs();
						i=1;
					} else{
						i++;
						if((i-1) % 80 ==0) System.out.print("\n     ");
					}


					System.out.print(".");
					//							System.out.println(String.format("[%d]\t%s", i++, q.getCreationInstruction()));
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
		});

		System.out.format("\nProcessing synthetic hybrid query generation of %d to %d data services . . .\n", n, N);
		ArrayList<Query> queryList = null;

		for (int i = n; i <= N; i++) {
			if(createFiles.get(i)){
				queryList = querySignatureGenerator.generateQuerySignatures(i);
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
		for (int i = n; i <= N; i++) {
			br=null;
			bw=null;
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
				Log.off();
				QEPBuilder builder = new QEPBuilder(Experiments.serviceInstancesFileName, Experiments.serviceInterfacesFileName);
				Log.on();
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
							System.out.println(q.getCreationInstruction());
							System.out.println(q.toDebbugingString());
							QEPNode root;
							try {
								Log.off();
								root = builder.constructQEP(q.getHqsl());
								QueryWorkflow qw = builder.getQW(root);
								Log.on();
								String qwFunctor=qw.toFunctor().replaceAll("\n", "").replaceAll("[ |\t]+", " "); 
								System.out.println("\t\t\t\t\t\tQW: "+qwFunctor);
								System.out.println();
								bw.append(q.getCreationInstruction()+"\n");
								bw.append(qwFunctor+"\n");
								totalQWs++;
								
							} catch (CyclicHypergraphException e) {
								System.err.println(e.getMessage()+q.getCreationInstruction());
								System.err.println();
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
				if(incompleteFile.renameTo(file))
					System.out.println(String.format("\nThe HYPATIA QWs for %d DSs are COMPLETE and SAVED into the file %s", i, file.getPath()));
				else{
					file.delete();
					System.out.println(String.format("\nThe HYPATIA QWs for %d DSs are COMLETE and SAVED in the temporary file %s (because of a permissions problem)", i, incompleteFile.getPath()));
				}
				


			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("\tDSs\tok\t\tcyclic");
		for (int i = n; i <= N; i++) {
			System.out.format("\t%d\t\t%d\t\t%d\n", i,totalQWsResume.get(i),cyclicHQsResume.get(i));
		}

		return files;
	}

}
