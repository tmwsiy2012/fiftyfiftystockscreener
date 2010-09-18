package com.eddiedunn.screen;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.eddiedunn.util.UC;

public class WriteNasDaqSymbols {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FileWriter outFile = new FileWriter("C:\\Users\\hpcorei3\\Documents\\screenerdata\\nasdaq.csv");
			PrintWriter out = new PrintWriter(outFile);

			String[] alpha = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
			for (int i = 0; i < alpha.length; i++) {
				UC.writeOneLetterOfNasdaqSymbols(out,alpha[i]);
				//UC.writeNasdaqTicker(alpha[i]);
			}	
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		//  <TD VALIGN=TOP ALIGN=LEFT WIDTH=64><P ALIGN=LEFT><FONT COLOR="#000000" SIZE="-1" FACE="MS Sans Serif">
	}


}
