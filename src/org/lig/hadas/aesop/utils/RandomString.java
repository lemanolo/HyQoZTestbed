package org.lig.hadas.aesop.utils;

import java.util.HashSet;
import java.util.Random;

public class RandomString
{

	private static final char[] symbols = new char[52];
	private static final HashSet<String> usedStrings= new HashSet<String>();
	static {

		for (int idx = 0; idx < 26; ++idx)
			symbols[idx] = (char) ('a' + idx);
		for (int idx = 26; idx < 52; ++idx)
			symbols[idx] = (char) ('A' + idx - 26);
	}

	private final Random random = new Random();

	private final char[] buf;

	public RandomString(int length)
	{
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public synchronized String nextString()
	{
		String string = null;
		do{
			for (int idx = 0; idx < buf.length; idx++){
				char c = symbols[random.nextInt(symbols.length)];
				if(idx==0)
					c=Character.toUpperCase(c);
				else
					c=Character.toLowerCase(c);

				buf[idx] = c;
			}
			string = new String(buf);
		}while(usedStrings.contains(string));
		usedStrings.add(string);
		return string;
	}

}