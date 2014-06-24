package org.lig.hadas.aesop.utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Log {
	private final static PrintStream out = System.out;
	private final static DateFormat dateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss:SSS");
	private static Calendar cal = Calendar.getInstance();
	private       static boolean verbose = true;

	public static void setVerbose(boolean v){
		verbose=v;
	}
	public static void outputToFile(File outputFile){
		try {
			System.setOut(new PrintStream(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void outputToStdout(){
		System.setOut(out);
	}

	public static void d(String message){
		if(verbose){
			cal = Calendar.getInstance();
			String time=dateFormat.format(cal.getTimeInMillis());
			String [] lines = message.split("\n");
			for (String line : lines) {
				System.out.println(String.format("[%s]\t%s", time,line));

			}
		}
	}
}
