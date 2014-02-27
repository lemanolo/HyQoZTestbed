package org.lig.hadas.aesop.syntheticQueryGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Query {
	private int numberOfDSs;
	private int numberOfBindJoins;
	private int numberOfJoins;
	private int numberOfFilterings;
	private int numberOfNonBlockingProjections;
	private int numberOfBlockingProjections;

	private String outputFileName;
	private String configString;

	private String sco_string;
	private String hqsl_string;

	private static String plDir          ="/Users/aguacatin/Research/HADAS/PhD/Prolog/qw_generation";
	private static String configLayout   =plDir+"/config_generation.pl.layout";
	private static String condigFileName =plDir+"/config_generation.pl";
	private static String generationScript = plDir+"/generate.sh";

	private static String NUMBER_OF_DS_KEY                   = "NumberOfDS";
	private static String NUMBER_OF_BINDINGS_KEY             = "NumberOfBindings";
	private static String NUMBER_OF_JOINS_KEY                = "NumberOfJoins";
	private static String NUMBER_OF_FILTERINGS               = "NumberOfFilterings";
	private static String NUMBER_OF_BLOCKING_PROJECTIONS     = "NumberOfBlockingProjetions";
	private static String NUMBER_OF_NON_BLOCKING_PROJECTIONS = "NumberOfNonBlockingProjections";
	private static String OUTPUT_FILE_NAME_KEY               = "OutputFileName";

	public Query(int numberOfDSs,        
			int numberOfJoins, 
			int numberOfBindJoins,
			int numberOfFilterings, 
			int numberOfBlockingProjections,
			int numberOfNonBlockingProjections,
			String sco,
			String hsql) {
		
		
		this.numberOfDSs                    = numberOfDSs;
		this.numberOfBindJoins              = numberOfBindJoins;
		this.numberOfJoins                  = numberOfJoins;
		this.numberOfFilterings             = numberOfFilterings;
		this.numberOfBlockingProjections    = numberOfBlockingProjections;
		this.numberOfNonBlockingProjections = numberOfNonBlockingProjections;
		this.sco_string = sco;
		this.hqsl_string = hsql;
	}
	public Query(int numberOfDSs,        
			int numberOfJoins, 
			int numberOfBindJoins,
			int numberOfFilterings, 
			int numberOfBlockingProjections,
			int numberOfNonBlockingProjections) throws PrologGeneratorException {
		
		
		this.numberOfDSs                    = numberOfDSs;
		this.numberOfBindJoins              = numberOfBindJoins;
		this.numberOfJoins                  = numberOfJoins;
		this.numberOfFilterings             = numberOfFilterings;
		this.numberOfBlockingProjections    = numberOfBlockingProjections;
		this.numberOfNonBlockingProjections = numberOfNonBlockingProjections;

		BufferedReader br = null;
		BufferedWriter bw = null;
		this.outputFileName=plDir+"/hq_"+
				String.valueOf(this.numberOfDSs)                    +"_"+
				String.valueOf(this.numberOfBindJoins)              +"_"+
				String.valueOf(this.numberOfJoins)                  +"_"+
				String.valueOf(this.numberOfFilterings)             +"_"+
				String.valueOf(this.numberOfBlockingProjections)    +"_"+
				String.valueOf(this.numberOfNonBlockingProjections) +".txt";
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(configLayout));
			this.configString="";
			while ((sCurrentLine = br.readLine()) != null)
				this.configString+=sCurrentLine+"\n";
			this.configString=this.configString.
					replaceAll(NUMBER_OF_DS_KEY,String.valueOf(this.numberOfDSs)).
					replaceAll(NUMBER_OF_BINDINGS_KEY,             String.valueOf(this.numberOfBindJoins)).
					replaceAll(NUMBER_OF_JOINS_KEY,                String.valueOf(this.numberOfJoins)).
					replaceAll(NUMBER_OF_FILTERINGS,               String.valueOf(this.numberOfFilterings)).
					replaceAll(NUMBER_OF_BLOCKING_PROJECTIONS,     String.valueOf(this.numberOfBlockingProjections)).
					replaceAll(NUMBER_OF_NON_BLOCKING_PROJECTIONS, String.valueOf(this.numberOfNonBlockingProjections)).
					replaceAll(OUTPUT_FILE_NAME_KEY,   		       this.outputFileName);
			//System.out.println("CONFIG: \n"+this.configString);
			bw = new BufferedWriter(new FileWriter(condigFileName));
			bw.write(this.configString);
			bw.close();

			Process p;
			try {
				p = Runtime.getRuntime().exec(generationScript);
				p.waitFor();
				BufferedReader inputReader = 
						new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = inputReader.readLine();
				while (line != null) {
					if(line.startsWith("SCO")){
						this.sco_string=line.replaceAll("::", ".").replaceAll(" *SCO *= *", "");
						//						System.out.println(" sco_string:\t"+this.sco_string);
					}else if(line.startsWith("HSQL")){
						this.hqsl_string=line.replaceAll(" *HSQL *= *", "")+";";
						//						System.out.println("hsql_string:\t"+this.hqsl_string);
					}
					else if(line.startsWith("TRY")) System.err.println(line);
					else{
						throw new PrologGeneratorException("\n[Unexpected output from Prolog]: "+line+"\nQuery: "+this.toDebbugingString());
					}
					line = inputReader.readLine();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	}
	public int getNumberOfDSs() {
		return numberOfDSs;
	}
	public int getNumberOfBindJoins() {
		return numberOfBindJoins;
	}
	public int getNumberOfJoins() {
		return numberOfJoins;
	}
	public int getNumberOfFilterings() {
		return numberOfFilterings;
	}
	public int getNumberOfNonBlockingProjections() {
		return numberOfNonBlockingProjections;
	}
	public int getNumberOfBlockingProjections() {
		return numberOfBlockingProjections;
	}
	public String getSco() {
		return sco_string;
	}
	public String getHqsl() {
		return hqsl_string;
	}
	public String toString(){
		return this.hqsl_string;
	}
	public String toDebbugingString(){
		return String.format("Creation instruction: %s" +
				"\n                 SCO: %s" +
				"\n                HSQL: %s", 
				this.getCreationInstruction(),            
				this.sco_string,
				this.hqsl_string);
	}
	public String getCreationInstruction(){
		return String.format("create_hq(%d, %d, %d, %d, %d, %d).", 
				numberOfDSs,            
				numberOfBindJoins,
				numberOfJoins,             
				numberOfFilterings,            
				numberOfBlockingProjections,
				numberOfNonBlockingProjections);
	}
	public String querySignature() {
		return String.format("%d_%d_%d_%d_%d_%d", 
				numberOfDSs,            
				numberOfBindJoins,
				numberOfJoins,             
				numberOfFilterings,            
				numberOfBlockingProjections,
				numberOfNonBlockingProjections);
		
	}
}
