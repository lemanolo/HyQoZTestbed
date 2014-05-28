package org.lig.hadas.aesop.dtfsDerivation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.lig.hadas.aesop.syntheticQueryGen.PrologGeneratorException;

public class DTFunctionsCollection extends HashSet<DTFunction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5156844043608151614L;
	
	private static String plDir          ="/Users/aguacatin/Research/HADAS/PhD/Prolog/hyqoz_dtfsDerivator";
	private static String configLayout   =plDir+"/config.pl.layout";
	
	private static String generationScript = plDir+"/run_from_java.sh";
	
	private static String SCO_KEY                   = "#SCO#";
	private static String OUTPUTFILENAME_KEY        = "#OUTPUTFILENAME#";
	
	
	private String sco_string   = null;
	private String types_string = null;
	private String dtfs_string  = null;
	
	public DTFunctionsCollection(String sco_string) throws PrologGeneratorException {
		this.sco_string=sco_string;
		this.deriveDTFS();
	}
	
	private void deriveDTFS() throws PrologGeneratorException{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(configLayout));
			String configString="";
			while ((sCurrentLine = br.readLine()) != null)
				configString+=sCurrentLine+"\n";
			configString=configString.
					replaceAll(SCO_KEY,this.sco_string.replaceAll("\\.", "::")).
					replaceAll(OUTPUTFILENAME_KEY,"none");
			
//			System.out.println("CONFIG: \n"+configString);
			
//+Creating config file				
			File configDir = new File(plDir+"/CONFIG");
			if(!configDir.exists())configDir.mkdirs();
			
			File configFile = File.createTempFile("config", ".pl", configDir);
//			System.out.print("configFile: "+configFile.getAbsolutePath());
			
			//bw = new BufferedWriter(new FileWriter(condigFileName));
			bw = new BufferedWriter(new FileWriter(configFile));
//-Creating config file
			
			bw.write(configString);
			bw.close();

			Process p;
			try {
				p = Runtime.getRuntime().exec(generationScript+ " "+configFile.getAbsolutePath());
				p.waitFor();
				BufferedReader inputReader = 
						new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = inputReader.readLine();
				this.types_string=null;
				this.dtfs_string=null;
				while (line != null) {
					if(line.startsWith("TYPES")){
						this.types_string=line.replaceAll("::", ".").replaceAll(" *TYPES *= *", "");
						System.out.println(" types_string:\t"+this.types_string);
					}else if(line.startsWith("DTFS")){
						this.dtfs_string=line.replaceAll(" *DTFS *= *", "");
						System.out.println("dtfs_string:\t"+this.dtfs_string);
					}
					else if(line.startsWith("TRY")) System.err.println(line);
					else{
						throw new PrologGeneratorException("\n[Unexpected output from Prolog]: "+line+"\nSCO: "+this.sco_string);
					}
					line = inputReader.readLine();
				}
				if(this.types_string == null || this.dtfs_string == null){
					throw new PrologGeneratorException("\n[Unexpected output from Prolog]: "+line+"\nSCO: "+this.sco_string);
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
	
	
	public String getSco_string() {return this.sco_string;}

	public String getTypes_string() {return this.types_string;}
	
	public String getDtfs_string() {return this.dtfs_string;}
	
}
