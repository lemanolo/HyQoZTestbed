package org.lig.hadas.aesop.syntheticQueryGen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class QuerySignatureGenerator {

	private ArrayList<QueryGenerationListener> listeners;
	public QuerySignatureGenerator() {
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
		HelpFormatter formatter = new HelpFormatter();

		int minNumberOfDSs = Integer.valueOf(cmd.getOptionValue("n"));
		int maxNumberOfDSs = Integer.valueOf(cmd.getOptionValue("N"));

		QuerySignatureGenerator generator = new QuerySignatureGenerator();

		//		for (int DSs = minNumberOfDSs; DSs <= maxNumberOfDSs; DSs++) {
		//			ArrayList<Query> al= generator.generateQuerySignatures(DSs);
		//		}

		generator.generateQuerySignatures(minNumberOfDSs, maxNumberOfDSs);

	}
	public HashMap<Integer, ArrayList<Query>> generateQuerySignatures(int n, int N) {
		HashMap<Integer, ArrayList<Query>> queries = new HashMap<Integer, ArrayList<Query>>();
		for (Integer DSs = n; DSs <= N; DSs++) {
			queries.put(DSs, generateQuerySignatures(DSs));
		}
		return queries;
	}
	public ArrayList<Query> generateQuerySignatures(int numberOfDSs) {
		ArrayList<Query> result = new ArrayList<Query>();
//		int length = this.totalOfQueries(numberOfDSs);
		//		Query [] result  = new Query [this.totalOfQueries(numberOfDSs)];

		int numberOfBindJoins;
		int numberOfJoins;
		int numberOfFilterings;
		int numberOfNonBlockingProjections;
		int numberOfBlockingProjections;
		//		System.out.println("#ofDSs: "+numberOfDSs +"\t #ofQueries: "+length);
		int i = 1;
		for(numberOfBindJoins=0; 
				numberOfBindJoins <numberOfDSs; 
				numberOfBindJoins++){
			numberOfJoins = numberOfDSs-numberOfBindJoins -1;
			for (numberOfFilterings = 0;  
					numberOfFilterings <= numberOfDSs; 
					numberOfFilterings++) {
				for (numberOfBlockingProjections = 0; 
						numberOfBlockingProjections <= numberOfDSs;
						numberOfBlockingProjections++) {
					for ( numberOfNonBlockingProjections = 0; 
							numberOfNonBlockingProjections <= numberOfDSs - numberOfBlockingProjections; 
							numberOfNonBlockingProjections++) {


						try {
							Query q=new Query(numberOfDSs, 
									numberOfBindJoins, 
									numberOfJoins, 
									numberOfFilterings, 
									numberOfBlockingProjections, 
									numberOfNonBlockingProjections);
							result.add(q);
							this.notifyListeners(q);

						} catch (PrologGeneratorException e) {
							e.printStackTrace();
							return result;
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
		return (int)( numberOfDSs* 
				     (numberOfDSs+1)* 
				     ((numberOfDSs+1)*(numberOfDSs+2)/2)
				    );
	}
}


