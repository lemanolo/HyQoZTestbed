package org.lig.hadas.aesop.qwGeneration;

import java.util.ArrayList;

import org.lig.hadas.aesop.syntheticQueryGen.PrologGeneratorException;

public interface PrologProgramOutputListener {
	public ArrayList<String> getBufferedOutput();
	public void newLine(String line) throws PrologGeneratorException;
}
