package org.lig.hadas.aesop.syntheticQueryGen;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class SynHQGenWrapper {
	private DecimalFormat format = new DecimalFormat( "#0.00%" );

	private ArrayList<QueryGenerationListener> listeners;
	public SynHQGenWrapper() {
		this.listeners = new ArrayList<QueryGenerationListener>();
	}

	public static void main(String[] args) {
		Options options = new Options();

		options.addOption("n", true, "min number of DSs");
		options.addOption("N", true, "max number of DSs");


		CommandLineParser parser = new PosixParser();
		CommandLine cmd=null;
		try {
			cmd = parser.parse( options, args);
		} catch (org.apache.commons.cli.ParseException exp) {
			exp.printStackTrace();
			System.err.println(exp.getMessage());
			return;
		}
		@SuppressWarnings("unused")
		HelpFormatter formatter = new HelpFormatter();

		int minNumberOfDSs = Integer.valueOf(cmd.getOptionValue("n"));
		int maxNumberOfDSs = Integer.valueOf(cmd.getOptionValue("N"));

		SynHQGenWrapper generator = new SynHQGenWrapper();

		//		for (int DSs = minNumberOfDSs; DSs <= maxNumberOfDSs; DSs++) {
		//			ArrayList<Query> al= generator.generateQuerySignatures(DSs);
		//		}

		generator.generateQuerySignatures(minNumberOfDSs, maxNumberOfDSs);

	}
	public HashMap<Integer, ArrayList<Query>> generateQuerySignatures(int n, int N) {
		HashMap<Integer, ArrayList<Query>> queries = new HashMap<Integer, ArrayList<Query>>();
		for (Integer DSs = n; DSs <= N; DSs++) {
			queries.put(DSs, generateSyntheticHQsFor(DSs));
		}
		return queries;
	}
	
	public ArrayList<Query> generateSyntheticHQsFor(int numberOfDSs) {
		ArrayList<Query> result = new ArrayList<Query>();


		int numberOfBindJoins;
		int numberOfJoins;
		int numberOfFilterings;
		int numberOfNonBlockingProjections;
		int numberOfBlockingProjections;

		int count = 0;


		for(numberOfBindJoins=0; 
				numberOfBindJoins <numberOfDSs; 
				numberOfBindJoins++){
			numberOfJoins = numberOfDSs-numberOfBindJoins -1;

			for (numberOfFilterings = 0;
					//BEGIN filterings
					//numberOfFilterings <= 0; //filterings only add complexity to the generation but they do not affect the proper aspects of the qw generation (e.g. deadlock relations)
					numberOfFilterings <= numberOfDSs; 
					//END filterings
					numberOfFilterings++) {
				for (numberOfBlockingProjections = 0; 
						numberOfBlockingProjections <= numberOfDSs;
						numberOfBlockingProjections++) {
					for ( numberOfNonBlockingProjections = 0; 
							numberOfNonBlockingProjections <= numberOfDSs - numberOfBlockingProjections; 
							numberOfNonBlockingProjections++) {
						try {

							boolean cyclic = true;
							int tries = 0;
							Query q =null;
							int threshold = 1;
							while(cyclic && tries < threshold){
								tries++;
								//								try {
								q=new Query(numberOfDSs, 
										numberOfBindJoins, 
										numberOfJoins, 
										numberOfFilterings, 
										numberOfBlockingProjections, 
										numberOfNonBlockingProjections);

								//									QEPNode root = builder.constructQEP(q.getHqsl());
								//									QueryWorkflow qw = builder.getQW(root);
								//									String qwFunctor=qw.toFunctor().replaceAll("\n", "").replaceAll("[ |\t]+", " ");

								cyclic=false;
								result.add(q);
								this.notifyListeners(q);
								//									System.out.println("     ok "+q.getCreationInstruction());
								count++;

								double progress = ((double)count)/((double)this.totalOfQueries(numberOfDSs));
								if(count>0) System.out.print("\r");
								System.out.print(String.format(" [%8s] ... %3s DSs ... ", format.format(progress),String.valueOf(numberOfDSs)));


								//								} catch (CyclicHypergraphException e) {
								//									System.out.println("["+tries+"] cyclic "+q.getCreationInstruction());
								//									System.out.println("["+tries+"] cyclic "+q.getHqsl());
								//									cyclic=true;
								//									Toolkit.getDefaultToolkit().beep();
								//									
								//									if(tries==threshold) this.notifyListeners(q);
								//									
								//									q=null;
								//								}
							}


						} catch (PrologGeneratorException e) {
							e.printStackTrace();
							return result;
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						//						System.out.println(String.format("[%d]\t %s", i++, q.toDebbugingString().replaceAll("\n", "\n\t")));
					}

				}
			}
		}

		return result;

	}

	public void addListener(QueryGenerationListener listener){
		this.listeners.add(listener);
	}

	private void notifyListeners(Query query){
		for (Iterator<QueryGenerationListener> iterator = this.listeners.iterator(); iterator.hasNext();) {
			QueryGenerationListener listener = (QueryGenerationListener) iterator.next();
			listener.generated(query);
		}
	}
	public int totalOfQueries(int numberOfDSs){
		//BEGIN filterings
		return (int)( numberOfDSs* 
				(numberOfDSs+1)* 
				((numberOfDSs+1)*(numberOfDSs+2)/2)
				);
		//		return (int)( numberOfDSs* 
		//				//(numberOfDSs+1)* //filtering 
		//				((numberOfDSs+1)*(numberOfDSs+2)/2)
		//				);
		//END filterings
	}
}


