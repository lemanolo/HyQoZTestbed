package org.lig.hadas.aesop.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecuter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Process p;
		try {
			p = Runtime.getRuntime().exec("/Users/aguacatin/Research/HADAS/PhD/Prolog/qw_generation/generate.sh");
			p.waitFor();
			BufferedReader inputReader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = inputReader.readLine();
			while (line != null) {
				if(line.startsWith("SCO")){
					line=line.replaceAll("::", ".");
					System.out.println(line);
				}else if(line.startsWith("HSQL")){
					 System.out.println(line);
				}
				else System.err.println(line);
				line = inputReader.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
