package com.handwin.hungry;

import java.util.ArrayList;
import java.util.List;

public class Cow {
	private String name;
	private List<Kraal> kraals=new ArrayList<Kraal>();
	public Cow(String name){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Kraal> getKraals() {
		return kraals;
	}
	public void setKraals(List<Kraal> kraals) {
		this.kraals = kraals;
	}
}
