package org.lig.hadas.aesop.qwGeneration;

import org.apache.commons.exec.LogOutputStream;
import org.lig.hadas.aesop.syntheticQueryGen.PrologGeneratorException;


public class PrologProgramOutputCollector extends LogOutputStream {

	private PrologProgramOutputListener listener;
	public void setListener(PrologProgramOutputListener listener){
		this.listener = listener;
	}

	@Override
	protected void processLine(String line, int level) {
		try {
			this.listener.newLine(line);
		} catch (PrologGeneratorException e) {
			e.printStackTrace();
		}
	}
}

