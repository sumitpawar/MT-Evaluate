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
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;

/**
 * @author sumit
 *
 */
public class METEOR {
	
	private DataObject[] data = new DataObject[0]; // will be determined at runtime
	private Integer[] dev_ans = new Integer[0];
	private int[] dataans;
	private final String datafile = "./data/hyp1-hyp2-ref";
	private final String ansfile = "./data/dev.answers";
	private Map<String, Set<String>> wordmap = new HashMap<String, Set<String>>();
	

	WordNetDatabase database = WordNetDatabase.getFileInstance(); 

	private Set<String> getSynonyms(String word) {
		Set<String> wordset = new HashSet<String>();
		Synset[] synsets = database.getSynsets(word);
		for(Synset synset : synsets) {
			String[] wf = synset.getWordForms();
//				System.out.println(" type : " + synset.getType());
			for(String str: wf) {
//					System.out.println(str);
				wordset.add(str);
			}
			WordSense[] wsar = synset.getDerivationallyRelatedForms(word);
			for(WordSense ws : wsar) {
//					System.out.println("related forms: " + ws.getWordForm());
				wordset.add(ws.getWordForm());
			}
		}
		return wordset;
	}
	
	private void readData() {
		try {
			List<DataObject> ldo = new ArrayList<DataObject>();
			BufferedReader br1 = new BufferedReader(new FileReader(datafile));
			BufferedReader br2 = new BufferedReader(new FileReader(ansfile));
			List<Integer> ans = new ArrayList<Integer>();
			String line = "";
			String a = "";
			while ((line = br1.readLine()) != null){
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
				String[] refarr = lines[2].split("\\s+");
				for(int i = 0 ; i < refarr.length; i++) {
					String word = refarr[i];
					Set<String> refset = new HashSet<String>();
					refset.add(word);
					if(!wordmap.containsKey(word)) {
						Set<String> syns = getSynonyms(word);
						refset.addAll(syns);
						wordmap.put(word, syns);
					} else {
						refset.addAll(wordmap.get(word));
					}
					d_o.getRefset().addAll(refset);
					String w2 = "";
					for(int j =0; j <= i ; j++) {
						Set<String> refset2 = new HashSet<String>();
						w2 += " " +refarr[i];
						w2 = w2.trim();
						if(!wordmap.containsKey(word)) {
							Set<String> syns = getSynonyms(word);
							if(syns.size() > 0) {
								wordmap.put(word, syns);
								refset2.addAll(syns);
								refset2.add(w2);
								refset.addAll(refset2);
							}
						}
					}
				}
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
		//match = match / ((double) len);
		return match;
	}
	
	private double getGramMatchSyn(int gram, String h, String[] ref, Set<String> refset) {
		double match = 0.0;
		int len = ref.length - (gram - 1);
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = 0; i < gram ; i++) {
				rfr += " " + ref[i+j];
				//rfr.trim();
			}
			if(h.contains(rfr)) {
				match++;
			}else {
				String h1 = "";
				String harr[] = h.split("\\s+");
				for(int i = 0; i < gram ; i++) {
					h1 += " " + harr[i+j];
					h1.trim();
				}
				for(String syn : getSynonyms(h1)) {
					if(refset.contains(syn)) {
						match++;
						break;
					}
				}
			}
		}
		//match = match / ((double) len);
		return match;
	}

	private String getMatchSynAndChunk(int gram, String h, String[] ref, Set<String> refset) {
		double match = 0.0;
		double chunk = 1.0;
		String arr[] = new String[ref.length];
		int len = ref.length - (gram - 1);
		String[] har = h.split("\\s+");
		int c = 0;
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = 0; i < gram ; i++) {
				rfr += " " + ref[i+j];
				rfr = rfr.trim();
			}
			if(h.contains(rfr)) {
				match++;
				for(int k = 0; k < har.length; k ++) {
					String h1 = "";
					for(int i = 0; i < gram ; i++) {
						h1 += " " + har[i+k];
						h1 = h1.trim();
					}
					if(rfr.equalsIgnoreCase(h1)) {
						for(int i = 0; i < gram ; i++) {
							arr[c] = (k + i) + "-" + (j + i);
							k++;
						}
						break;
					}
				}
			}else {
				String h1 = "";
				for(int i = 0; i < gram && (i+j) < har.length; i++) {
					h1 += " " + har[i+j];
					h1.trim();
				}
				for(String syn : getSynonyms(h1)) {
					if(refset.contains(syn)) {
						match++;
						for(int i = 0; i < gram ; i++) {
							arr[c] = (j + i) + "-" + (j + i);
							c++;
						}
						break;
					}
				}
			}
		}
//		int fc = c;
//		for(;c<ref.length; c++) {
//			arr[c] = null;
//		}
//		//Arrays.sort(arr);
		for(int i = 1; i < c; i ++) {
			String s1 = arr[i-1];
			String s2 = arr[i];
			String[] sp = s1.split("\\-");
			int c1 = Integer.parseInt(sp[0]);
			int r1 = Integer.parseInt(sp[1]);
			sp = s2.split("\\-");
			int c2 = Integer.parseInt(sp[0]);
			int r2 = Integer.parseInt(sp[1]);
			if((!(r1 == (r2 + 1) && c1 == (c2 + 1))
				|| (r1 == (r2 - 1) && c2 == (c2 - 1)) 
				|| (r1 == (r2 + 1 ) && c2 == (c2 - 1))
				|| (r1 == (r2 - 1) && c1 == (c2 + 1))) ) {
				chunk++;
			}
		}
		//match = match / ((double) len);
		return (match +"-"+ chunk);
	}
	
	
	private String getMatchAndChunk(int gram, String h, String[] ref) {
		double match = 0.0;
		double chunk = 1.0;
		String arr[] = new String[ref.length];
		int len = ref.length - (gram - 1);
		String[] har = h.split("\\s+");
		int c = 0;
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = 0; i < gram ; i++) {
				rfr += " " + ref[i+j];
				rfr = rfr.trim();
			}
			if(h.contains(rfr)) {
				match++;
				for(int k = 0; k < har.length; k ++) {
					if(rfr.equalsIgnoreCase(har[k])) {
						arr[c] = k + "-" + j;
						k++;
						break;
					}
				}
			}
		}
//		int fc = c;
//		for(;c<ref.length; c++) {
//			arr[c] = null;
//		}
//		//Arrays.sort(arr);
		for(int i = 1; i < c; i ++) {
			String s1 = arr[i-1];
			String s2 = arr[i];
			String[] sp = s1.split("\\-");
			int c1 = Integer.parseInt(sp[0]);
			int r1 = Integer.parseInt(sp[1]);
			sp = s2.split("\\-");
			int c2 = Integer.parseInt(sp[0]);
			int r2 = Integer.parseInt(sp[1]);
			if((!(r1 == (r2 + 1) && c1 == (c2 + 1))
				|| (r1 == (r2 - 1) && c2 == (c2 - 1)) 
				|| (r1 == (r2 + 1 ) && c2 == (c2 - 1))
				|| (r1 == (r2 - 1) && c1 == (c2 + 1))) ) {
				chunk++;
			}
		}
		//match = match / ((double) len);
		return (match +"-"+ chunk);
	}
	
	private String getMatchAndChunk2(int gram, String h, String[] ref, Set<String> refset) {
		double match = 0.0;
		double chunk = 1.0;
		String arr[] = new String[ref.length];
		int len = ref.length - (gram - 1);
		String[] har = h.split("\\s+");
		int c = 0;
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = 0; i < gram ; i++) {
				rfr += " " + ref[i+j];
				rfr = rfr.trim();
			}
			if(h.contains(rfr)) {
				match++;
				for(int k = 0; k < har.length; k ++) {
					if(rfr.equalsIgnoreCase(har[k])) {
						arr[c] = k + "-" + j;
						k++;
						break;
					}
				}
			}else {
				String h1 = "";
				for(int i = 0; i < gram && (i+j) < har.length; i++) {
					h1 += " " + har[i+j];
					h1 = h1.trim();
				}
				for(String str : getSynonyms(h1)) {
					if(refset.contains(str)) {
						match++;
						break;
					}
				}
			}
		}
//		int fc = c;
//		for(;c<ref.length; c++) {
//			arr[c] = null;
//		}
//		//Arrays.sort(arr);
		for(int i = 1; i < c; i ++) {
			String s1 = arr[i-1];
			String s2 = arr[i];
			String[] sp = s1.split("\\-");
			int c1 = Integer.parseInt(sp[0]);
			int r1 = Integer.parseInt(sp[1]);
			sp = s2.split("\\-");
			int c2 = Integer.parseInt(sp[0]);
			int r2 = Integer.parseInt(sp[1]);
			if((!(r1 == (r2 + 1) && c1 == (c2 + 1))
				|| (r1 == (r2 - 1) && c2 == (c2 - 1)) 
				|| (r1 == (r2 + 1 ) && c2 == (c2 - 1))
				|| (r1 == (r2 - 1) && c1 == (c2 + 1))) ) {
				chunk++;
			}
		}
		//match = match / ((double) len);
		return (match +"-"+ chunk);
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
	
	private void evaluatePen2() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			double alpha = 0.78;
			double beta = 4.0;
			double gamma = 0.9;
			
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			double h1l = h1.split("\\s+").length;
			double h2l = h2.split("\\s+").length;
			
			double meteor1 = 0.0;
			double meteor2 = 0.0; 
			double p1 = 0.0, p2 = 0.0;
			double r1 = 0.0, r2 = 0.0;
			double pen1 = 0.0, pen2 = 0.0;
			for(int j = 1; j < len - 1; j++) {
				/*double m1 = getGramMatch(j, h1, refarr);
				p1 +=  (j+50) * m1 / (len + j + 6);
				r1 +=  (j+50) * m1 / (h1l + j + 6);
				//meteor1 += p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha));
				
				double m2 = getGramMatch(j, h2, refarr);
				p2 +=  (j+50) * m2 / (len + j + 6);
				r2 +=  (j+50) * m2 / (h2l + j + 6);
				//meteor2 +=  p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha));
*/				String mnc1[] = getMatchAndChunk(1, h1, refarr).split("\\-");
				double m1 = Double.parseDouble(mnc1[0]);
				double c1 = Double.parseDouble(mnc1[1]);
				p1 +=   m1 / (len);
				r1 +=   m1 / (h1l);
				pen1 += Math.pow((gamma * (c1/m1)), beta);
				
				String mnc2[] = getMatchAndChunk(1, h2, refarr).split("\\-");
				double m2 = Double.parseDouble(mnc2[0]);
				double c2 = Double.parseDouble(mnc2[1]);
				p2 +=  m2 / (len);
				r2 +=  m2 / (h2l);
				pen2 += Math.pow((gamma * (c2/m2)), beta);
				
			}
			meteor1 +=  (1 - pen1) * ( p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha)) );
			meteor2 +=  (1 - pen2) * ( p2 * r2/ (( (1 - alpha) * p2) + (r2 * alpha)) );
			
			//if(Math.abs(meteor1 - meteor2) < 0.000001 ) {
				dataans[i] = 0;
			/*} else */if(meteor1 > meteor2) {
				dataans[i] = 1;
			}else if(meteor1 < meteor2) {
				dataans[i] = -1;
			}
		}
	}
	
	private void evaluatePen2Syn() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			double alpha = 0.78;
			double beta = 4.0;
			double gamma = 0.9;
			
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			double h1l = h1.split("\\s+").length;
			double h2l = h2.split("\\s+").length;
			
			double meteor1 = 0.0;
			double meteor2 = 0.0; 
			double p1 = 0.0, p2 = 0.0;
			double r1 = 0.0, r2 = 0.0;
			double pen1 = 0.0, pen2 = 0.0;
			for(int j = 1; j < len - 1; j++) {
				/*double m1 = getGramMatch(j, h1, refarr);
				p1 +=  (j+50) * m1 / (len + j + 6);
				r1 +=  (j+50) * m1 / (h1l + j + 6);
				//meteor1 += p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha));
				
				double m2 = getGramMatch(j, h2, refarr);
				p2 +=  (j+50) * m2 / (len + j + 6);
				r2 +=  (j+50) * m2 / (h2l + j + 6);
				//meteor2 +=  p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha));
*/				String mnc1[] = getMatchAndChunk2(1, h1, refarr, d_o.getRefset()).split("\\-");
				double m1 = Double.parseDouble(mnc1[0]);
				double c1 = Double.parseDouble(mnc1[1]);
				p1 +=   m1 / (len);
				r1 +=   m1 / (h1l);
				pen1 += Math.pow((gamma * (c1/m1)), beta);
				
				String mnc2[] = getMatchAndChunk2(1, h2, refarr, d_o.getRefset()).split("\\-");
				double m2 = Double.parseDouble(mnc2[0]);
				double c2 = Double.parseDouble(mnc2[1]);
				p2 +=  m2 / (len);
				r2 +=  m2 / (h2l);
				pen2 += Math.pow((gamma * (c2/m2)), beta);
				
			}
			meteor1 +=  (1 - pen1) * ( p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha)) );
			meteor2 +=  (1 - pen2) * ( p2 * r2/ (( (1 - alpha) * p2) + (r2 * alpha)) );
			
			//if(Math.abs(meteor1 - meteor2) < 0.000001 ) {
				dataans[i] = 0;
			/*} else */if(meteor1 > meteor2) {
				dataans[i] = 1;
			}else if(meteor1 < meteor2) {
				dataans[i] = -1;
			}
		}
	}
	
	private void evaluatePenalty() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			double alpha = 0.65;
			double beta = 3.0;
			double gamma = 0.9;
			
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			double h1l = h1.split("\\s+").length;
			double h2l = h2.split("\\s+").length;
			String mnc1[] = getMatchAndChunk(1, h1, refarr).split("\\-");
			double m1 = Double.parseDouble(mnc1[0]);
			double c1 = Double.parseDouble(mnc1[1]);
			double p1 =  m1 / len;
			double r1 = m1 / h1l;
			double pen1 = Math.pow((gamma * (c1/m1)), beta);
			double meteor1 =  (1 - pen1) * ( p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha)) );
			
			String mnc2[] = getMatchAndChunk(1, h2, refarr).split("\\-");
			double m2 = Double.parseDouble(mnc2[0]);
			double c2 = Double.parseDouble(mnc2[1]);
			double p2 =  m2 / len;
			double r2 = m2 / h2l;
			double pen2 = Math.pow((gamma * (c2/m2)), beta);
			double meteor2 =  (1 - pen2) * ( p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha)) );
			
			
			//if(Math.abs(bleu1 - bleu2) < 0.00000001) {
				dataans[i] = 0;
			/*}else */if(meteor1 > meteor2) {
				dataans[i] = 1;
			}else if(meteor1 < meteor2) {
				dataans[i] = -1;
			}
		}
	}
	
	private void evaluateSimple2() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			double alpha = 0.65;
			
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			double h1l = h1.split("\\s+").length;
			double h2l = h2.split("\\s+").length;
			
			double meteor1 = 0.0;
			double meteor2 = 0.0; 
			double p1 = 0.0, p2 = 0.0;
			double r1 = 0.0, r2 = 0.0;
			for(int j = 1; j < len - 1; j++) {
				double m1 = getGramMatch(j, h1, refarr);
				p1 +=  (j+50) * m1 / (len + j + 6);
				r1 +=  (j+50) * m1 / (h1l + j + 6);
				//meteor1 += p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha));
				
				double m2 = getGramMatch(j, h2, refarr);
				p2 +=  (j+50) * m2 / (len + j + 6);
				r2 +=  (j+50) * m2 / (h2l + j + 6);
				//meteor2 +=  p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha));
			}
			meteor1 += p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha));
			meteor2 +=  p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha));
			
			dataans[i] = 0;
			if(meteor1 > meteor2) {
				dataans[i] = 1;
			}else if(meteor1 < meteor2) {
				dataans[i] = -1;
			}
		}
	}
	
	private void evaluateSimple() {
		int size = data.length;
		dataans = new int[size];
		for(int i = 0; i < size; i++) {
			DataObject d_o = data[i];
			String h1 = d_o.getH1();
			String h2 = d_o.getH2();
			String ref = d_o.getRef();
			double alpha = 0.65;
			
			String refarr[] = ref.split("\\s+");
			double len = refarr.length;
			double h1l = h1.split("\\s+").length;
			double h2l = h2.split("\\s+").length;
			double m1 = getGramMatch(1, h1, refarr);
			double p1 =  m1 / len;
			double r1 = m1 / h1l;
			double meteor1 =  p1 * r1 / (( (1 - alpha) * p1) + (r1 * alpha));
			
			double m2 = getGramMatch(1, h2, refarr);
			double p2 =  m2 / len;
			double r2 = m2 / h2l;
			double meteor2 =  p2 * r2 / (( (1 - alpha) * p2) + (r2 * alpha));
			
			
			//if(Math.abs(bleu1 - bleu2) < 0.00000001) {
				dataans[i] = 0;
			/*}else */if(meteor1 > meteor2) {
				dataans[i] = 1;
			}else if(meteor1 < meteor2) {
				dataans[i] = -1;
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void evaluateRef() {
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
				t1 += (j+50) * ((val1 != 0.0)?Math.log( val1) / len:0.0);
				t2 += (j+50) * ((val2 != 0.0)?Math.log( val2) / len:0.0);
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
	
	public void runMETEOR() {
		double accuracy = 0.0;
		long start = System.currentTimeMillis();
		readData();
		System.out.println("Time taken to read data files: " + (System.currentTimeMillis() - start) + "ms");
		evaluateSimple();
		System.out.println("Time taken to evaluate simple: " + (System.currentTimeMillis() - start) + "ms");
		accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/meteor.simple1");
		
		// evaluate simple2
		System.out.println();
		evaluateSimple2();
		System.out.println("Time taken to evaluate simple2: " + (System.currentTimeMillis() - start) + "ms");
		accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy simple2: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/meteor.simple2");
		
		// evaluate penalty
		System.out.println();
		evaluatePenalty();
		System.out.println("Time taken to evaluate penalty: " + (System.currentTimeMillis() - start) + "ms");
		accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy penalty: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/meteor.penalty");
		
//		getSynonyms("bank");
		// evaluate penalty
		System.out.println();
		evaluatePen2();
		System.out.println("Time taken to evaluate pen2: " + (System.currentTimeMillis() - start) + "ms");
		accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy pen2: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/meteor.pen2");
		
		// evaluate penalty and synonyms
		System.out.println();
		evaluatePen2Syn();
		System.out.println("Time taken to evaluate pen2: " + (System.currentTimeMillis() - start) + "ms");
		accuracy = computeAccuracy();
		System.out.println("Time taken to compute accuracy pen2: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println("accuracy: " + accuracy);
		printAnsToFile("./data/meteor.pen2syn");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("wordnet.database.dir", "/home/sumit/workspace/MT-Evaluation/data/Wordnet/wn3.1/dict/");
		new METEOR().runMETEOR();
	}

}
