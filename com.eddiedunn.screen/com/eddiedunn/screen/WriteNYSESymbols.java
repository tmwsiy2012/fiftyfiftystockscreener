package com.eddiedunn.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class WriteNYSESymbols {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader reader = null;
		String delimiter = "\t";
		ArrayList<String> ar = new ArrayList<String>();		
		try {
			File infile = new File("C:\\Users\\hpcorei3\\Documents\\screenerdata\\NYSE.txt");
			//File file = new File(filename);


				reader = new BufferedReader(new FileReader(infile));
				String text = null;
				
				// skip header row
				reader.readLine();
				
				// repeat until all lines are read
				while ((text = reader.readLine()) != null) {
					String[] tmp = text.split(delimiter);
					//ar.add(tmp[0].trim());
					System.out.println(tmp[0].trim());
				}
					}catch(Exception e)
			{e.printStackTrace();} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			//return (String []) ar.toArray (new String [ar.size ()]);			
		}
	}



