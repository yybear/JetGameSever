package com.handwin.util;

public class Match {
	private String cowName;
	private String kraalName;
	public Match(String cowName,String kraalName){
		this.cowName=cowName;
		this.kraalName=kraalName;
	}
	public String getCowName() {
		return cowName;
	}
	public void setCowName(String cowName) {
		this.cowName = cowName;
	}
	public String getKraalName() {
		return kraalName;
	}
	public void setKraalName(String kraalName) {
		this.kraalName = kraalName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cowName == null) ? 0 : cowName.hashCode());
		result = prime * result
				+ ((kraalName == null) ? 0 : kraalName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Match other = (Match) obj;
		if (cowName == null) {
			if (other.cowName != null)
				return false;
		} else if (!cowName.equals(other.cowName))
			return false;
		if (kraalName == null) {
			if (other.kraalName != null)
				return false;
		} else if (!kraalName.equals(other.kraalName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return cowName + "->" + kraalName;
	}
}
