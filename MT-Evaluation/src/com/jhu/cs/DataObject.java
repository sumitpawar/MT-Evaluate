/**
 * 
 */
package com.jhu.cs;

import java.util.HashSet;
import java.util.Set;

/**
 * @author sumit
 *
 */
public class DataObject {
	private String h1;
	private String h2;
	private String ref;
	private Set<String> refset = new HashSet<String>();
	/**
	 * @return the h1
	 */
	public String getH1() {
		return h1;
	}
	/**
	 * @param h1 the h1 to set
	 */
	public void setH1(String h1) {
		this.h1 = h1;
	}
	/**
	 * @return the h2
	 */
	public String getH2() {
		return h2;
	}
	/**
	 * @param h2 the h2 to set
	 */
	public void setH2(String h2) {
		this.h2 = h2;
	}
	/**
	 * @return the ref
	 */
	public String getRef() {
		return ref;
	}
	/**
	 * @param ref the ref to set
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	/**
	 * @return the refset
	 */
	public Set<String> getRefset() {
		return refset;
	}
	/**
	 * @param refset the refset to set
	 */
	public void setRefset(Set<String> refset) {
		this.refset = refset;
	}
	@Override
	public String toString() {
		String str = "";
		str = "H1: " + this.getH1() + ", H2: " + this.getH2() + 
				", Ref: " + this.getRef();
		return str;
	}
	
}
