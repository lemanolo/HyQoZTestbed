package org.lig.hadas.aesop.tests;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

public class CommandExecuter {

	/**
	 * @param args
	 */
	//	public static void main(String[] args) {
	//
	//		ProcessBuilder pb = new ProcessBuilder("/Users/aguacatin/Research/HADAS/workspace_aesop/HyQoZTestbed/read.sh");
	//		Process p;
	//		try {
	//			p = pb.start();
	//			p.waitFor();  // wait for process to finish then continue.
	//
	//			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
	//			String RESULT = "";
	//			String line = "";
	//			while ((line = bri.readLine()) != null) {
	//				System.out.println("[Java] "+line);
	//				RESULT+=line;
	//			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}  
	//	}

	public static void main(String[] args) {


		CommandLine cmdLine = new CommandLine("/Users/aguacatin/Research/HADAS/workspace_aesop/HyQoZTestbed/read.sh");
		cmdLine.addArgument("ss_sizes.txt");

		LogOutputStream stdout = new LogOutputStream() {
			
			@Override
			protected void processLine(String line, int level) {
				System.out.format("[JAVA][%d] %s \n",level,line);
			}
		};
	    PumpStreamHandler psh = new PumpStreamHandler(stdout);
	    DefaultExecutor exec = new DefaultExecutor();
	    exec.setStreamHandler(psh);
	    try {
			exec.execute(cmdLine);
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
