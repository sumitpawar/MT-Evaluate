/**
 * 
 */
package com.jhu.cs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sumit
 *
 */
public class BLEU {

	private DataObject[] data = new DataObject[0]; // will be determined at runtime
	private Integer[] dev_ans = new Integer[0];
	private int[] dataans;
	private final String datafile = "./data/hyp1-hyp2-ref";
	private final String ansfile = "./data/dev.answers";
	
	private void readData() {
		try {
			List<DataObject> ldo = new ArrayList<DataObject>();
			BufferedReader br1 = new BufferedReader(new FileReader(datafile));
			BufferedReader br2 = new BufferedReader(new FileReader(ansfile));
			List<Integer> ans = new ArrayList<Integer>();
			String line = "";
			String a = "";
//			int b = 0;
			while ((line = br1.readLine()) != null/* && b <= 1*/){
//				b++;
				if(null != a) {
					a = br2.readLine();
					if(a != null) {
						ans.add(Integer.parseInt(a));
					}
				}
				String[] lines = line.split("\\s+\\|+\\s+");
				DataObject d_o = new DataObject();
				d_o.setH1(lines[0]);
				d_o.setH2(lines[1]);
				d_o.setRef(lines[2]);
				//d_o.setAns(Integer.parseInt(ans));
				ldo.add(d_o);
			}
			data = ldo.toArray(data);
			//System.out.println("total data : " + data.length);
			dev_ans = ans.toArray(dev_ans);
			//System.out.println(data[0].toString());
			//System.out.println("dev ans len: " + dev_ans.length + ", dev'0': " + dev_ans[0]);
			br1.close();
			br2.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private double getGramMatch(int gram, String h, String[] ref) {
		double match = 0.0;
		int len = ref.length - (gram - 1);
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = 0; i < gram ; i++) {
				rfr += ref[i+j];
			}
			if(h.contains(rfr)) {
				match++;
			}
		}
		match = match / ((double) len);
		return match;
	}
	
	@SuppressWarnings("unused")
	private double getGramMatch2(int gram, String h[], String ref) {
		double match = 0.0;
		int len = h.length - (gram - 1);
		for(int j = 0; j < len; j++) {
			String h1 = "";
			for(int i = gram; i > 0 ; i++) {
				h1 += h[i+j];
			}
			if(ref.contains(h1)) {
				match++;
			}
		}
		match = match / ((double) len);
		return match;
	}

	private void evaluate() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			double reflen = d_o.getRef().split("\\s+").length;
			double h1len = d_o.getH1().split("\\s+").length;
			double h2len = d_o.getH2().split("\\s+").length;
			// Compute BP
			double bp1 = (h1len < reflen)?Math.exp(1 - (reflen/h1len)):1.0;
			double bp2 = (h2len < reflen)?Math.exp(1 - (reflen/h2len)):1.0;
			//double bp1 = (h1len < reflen)?reflen - h1len:1.0 +h1len-reflen;
			//double bp2 = (h2len < reflen)?reflen - h2len:1.0 +h2len - reflen;
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			
			double t1 = 0.0, t2 = 0.0;
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			for(int j = 1; j < len - 1; j++) {
				double val1 = getGramMatch(j, h1, refarr);
				double val2 = getGramMatch(j, h2, refarr);
				t1 += (j+50) * ((val1 != 0.0)?Math.log( val1) / (len-j):0.0);
				t2 += (j+50) * ((val2 != 0.0)?Math.log( val2) / (len-j):0.0);
			}
			
			/*String h1ar[] = h1.split("//s+");
			double h1l = h1ar.length;
			for(int j = 1; j < h1l; j++) {
				double val1 = getGramMatch2(j, h1ar, ref);
				t1 += (val1 != 0.0)?Math.log( val1) / h1l:0.0;
			}
			String h2ar[] = h2.split("//s+");
			double h2l = h1ar.length;
			for(int j = 1; j < h2l; j++) {
				double val2 = getGramMatch2(j, h2ar, ref);
				t2 += (val2 != 0.0)?Math.log( val2) / h2l:0.0;
			}*/
			
			double bleu1 = bp1 * Math.exp(t1);
			double bleu2 = bp2 * Math.exp(t2);
			
			//if(Math.abs(bleu1 - bleu2) < 0.00000001) {
				dataans[i] = 0;
			/*}else */if(bleu1 > bleu2) {
				dataans[i] = 1;
			}else if(bleu1 < bleu2) {
				dataans[i] = -1;
			}
		}
	}
	
	private double computeAccuracy() {
		double match = 0.0;
		double len = dev_ans.length;
		for(int i = 0; i < (int)len ; i++) {
			if(dev_ans[i] == dataans[i]) {
				match++;
			}
		}
		return (match/len);
	}
	
	private void printAnsToFile(String filepath) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(filepath), "UTF-8");
			for(int i = 0 ; i < dataans.length; i++) {
				writer.println(dataans[i]);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void BLEU_Evaluation() {
		long start = System.currentTimeMillis();
		readData();
		System.out.println("Time taken to read data files: " + (System.currentTimeMillis() - start) + "ms");
		evaluate();
		System.out.println("Time taken to evaluate: " + (System.currentTimeMillis() - start) + "ms");
		double accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/bleu1");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BLEU().BLEU_Evaluation();
	}

}
