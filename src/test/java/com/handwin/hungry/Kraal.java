package com.handwin.hungry;

import java.util.ArrayList;
import java.util.List;

public class Kraal {
	private String name;
	private List<Cow> cows=new ArrayList<Cow>();
	private boolean used=false;
	public Kraal(String name){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Cow> getCows() {
		return cows;
	}
	public void setCows(List<Cow> cows) {
		this.cows = cows;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
}
