package org.lig.hadas.aesop.queryWorkflowCost;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class QWCost {


	private static String plDir          = "/Users/aguacatin/Research/HADAS/PhD/Prolog/qw_generation";
	private static String configLayout   = plDir+"/config_qw_cost.pl.layout";
	private static String condigFileName = plDir+"/config_qw_cost.pl";
	private static String qwCostScript   = plDir+"/run_qw_cost.sh";

	private static String ACTIVITIES ="Activities";
	private static String PARALLEL   ="Parallel";
	private static String VERTICES   ="Vertices";
	private static String EDGES      ="Edges";
	private static String IN		 ="IN";
	private static String OUT		 ="OUT";
	private static String ID		 ="ID";


	private static String [] KEYS = {"time","price","energy"};


	private String qwFunctor = null;
	private String activitiesString = "";
	private String parallelString 	= "";
	private String verticesString 	= "";
	private String edgesString 		= "";
	private String inString 		= "";
	private String outString 		= "";
	private String idString 		= "";

	private HashMap<String, Double> costMap;

	public QWCost(String qwFunctor) {
		this.qwFunctor = qwFunctor;
		String analizing = "qw";
		
		String qwFunctorClean = this.qwFunctor.replaceFirst("^ *qw *\\(", "").replaceFirst("\\) *. *$", "");
		
		for (int i = 0; i < qwFunctorClean.length(); i++) {
			if(analizing.equals("qw") && (i==0||qwFunctorClean.charAt(i)==',')){
				if(activitiesString=="") analizing = "activities";
				else if(parallelString=="")  analizing = "parallel";
				else if(verticesString=="")  analizing = "vertices";
				else if(edgesString=="")     analizing = "edges";
				else if(inString=="")        analizing = "in";
				else if(outString=="")       analizing = "out";
				else if(idString=="")        analizing = "id";
				if(qwFunctorClean.charAt(i)==',')
					continue;
			}
			
			if(analizing.equals("activities")){
				if(qwFunctorClean.charAt(i)==']') analizing = "qw";
				activitiesString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("parallel")){
				if(qwFunctorClean.charAt(i)==']') analizing = "qw";
				parallelString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("vertices")){
				if(qwFunctorClean.charAt(i)==']') analizing = "qw";
				verticesString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("edges")){
				if(qwFunctorClean.charAt(i)==']') analizing = "qw";
				edgesString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("in")){
				if(qwFunctorClean.charAt(i+1)==',') analizing = "qw";
				inString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("out")){
				if(qwFunctorClean.charAt(i+1)==',') analizing = "qw";
				outString += qwFunctorClean.charAt(i);
			}else if(analizing.equals("id")){
//				if(qwFunctorClean.charAt(i+1)==',') analizing = "qw";
				idString += qwFunctorClean.charAt(i);
			}

		}
	}
	public Set<String> getCostKeys(){
		if(this.costMap!=null)
			return this.costMap.keySet();
		return null;
	}
	public Collection<Double> getCostValues(){
		if(this.costMap!=null)
			return this.costMap.values();
		return null;
	}
	public HashMap<String, Double> getCost() throws PrologQWCostException {

		if(this.costMap!=null)
			return this.costMap;

	
		return this.computeCost();
	}
	
	public String toFunctor(){

		String functor = "cost(";
		for (Iterator<String> iterator = this.costMap.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			double cost = costMap.get(key);
			DecimalFormat df = new DecimalFormat("0.00##");
			functor+=df.format(cost).replace(',', '.');
			if(iterator.hasNext())
				functor+=", ";
		}
		functor+=").";
		return functor;
	}

	private HashMap<String, Double> computeCost() throws PrologQWCostException{
		BufferedReader br = null;
		BufferedWriter bw = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(configLayout));
			String configString="";
			while ((sCurrentLine = br.readLine()) != null)
				configString+=sCurrentLine+"\n";

			configString=configString.
					replaceAll(ACTIVITIES, activitiesString).
					replaceAll(PARALLEL,   parallelString).
					replaceAll(VERTICES,     verticesString).
					replaceAll(EDGES,      edgesString).
					replaceAll(IN,         inString).
					replaceAll(OUT,        outString).
					replaceAll(ID,         idString);

			//System.out.println("CONFIG: \n"+this.configString);
			bw = new BufferedWriter(new FileWriter(condigFileName));
			bw.write(configString);
			bw.close();

			Process p;
			try {
				p = Runtime.getRuntime().exec(qwCostScript);
				p.waitFor();
				BufferedReader inputReader = 
						new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = inputReader.readLine();
				String cost_string= "";
				while (line != null) {
					if(line.startsWith("COST")){
						cost_string=line.replaceFirst("^.*cost *\\(", "").replaceFirst("\\) *$", "");
						String [] costs_array = cost_string.split(" *, *");
						for (int i = 0; i < costs_array.length; i++) {
							if(this.costMap==null) this.costMap = new HashMap<String, Double>();

							this.costMap.put(KEYS[i], Double.valueOf(costs_array[i]));
						}
					}
					else{
						throw new PrologQWCostException("\n[Unexpected output from Prolog]: "+line+"\nQW: "+this.qwFunctor);
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
		return this.costMap;
	}
}
