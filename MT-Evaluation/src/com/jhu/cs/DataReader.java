/**
 * 
 */
package com.jhu.cs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sumit
 *
 */
public class DataReader {

	private final String datafile = "./data/hyp1-hyp2-ref";
	private final String ansfile = "./data/dev.answers";
	

	private DataObject[] data = new DataObject[0]; // will be determined at runtime
	private Integer[] dev_ans = new Integer[0];
	
	/**
	 * @return the data
	 */
	public DataObject[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(DataObject[] data) {
		this.data = data;
	}

	/**
	 * @return the dev_ans
	 */
	public Integer[] getDev_ans() {
		return dev_ans;
	}

	/**
	 * @param dev_ans the dev_ans to set
	 */
	public void setDev_ans(Integer[] dev_ans) {
		this.dev_ans = dev_ans;
	}

	public void readData() {
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
				//d_o.setAns(Integer.parseInt(ans));
				ldo.add(d_o);
			}
			setData(ldo.toArray(getData()));
			//System.out.println("total data : " + data.length);
			setDev_ans(ans.toArray(getDev_ans()));
			//System.out.println(data[0].toString());
			//System.out.println("dev ans len: " + dev_ans.length /*+ ", dev'0': " + dev_ans[0]*/);
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new DataReader().readData();
	}

}
