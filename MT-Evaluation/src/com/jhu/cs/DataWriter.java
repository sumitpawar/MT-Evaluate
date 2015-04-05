/**
 * 
 */
package com.jhu.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author sumit
 *
 */
public class DataWriter {

	public void printAnsToFile(String filepath, int[] dataans) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File("filepath"), "UTF-8");
			for(int i = 0 ; i < dataans.length; i++) {
				writer.println(dataans[i]);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
