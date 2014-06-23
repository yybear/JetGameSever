package com.handwin.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DateSource2 {
	static Map<String,Cow> cows=new HashMap<String,Cow>();
	static Map<String,Kraal> kraals=new HashMap<String,Kraal>();
	static{
		Cow cowA=new Cow("A");
		Cow cowB=new Cow("B");
		Cow cowC=new Cow("C");
		Cow cowD=new Cow("D");
		Kraal kraal1=new Kraal("1");
		Kraal kraal2=new Kraal("2");
		Kraal kraal3=new Kraal("3");
		Kraal kraal4=new Kraal("4");
		cowA.getKraals().add(kraal1);
		cowA.getKraals().add(kraal3);
		cowB.getKraals().add(kraal1);
		cowB.getKraals().add(kraal2);
		cowC.getKraals().add(kraal4);
		cowD.getKraals().add(kraal2);
		cowD.getKraals().add(kraal3);
		kraal1.getCows().add(cowA);
		kraal1.getCows().add(cowB);
		kraal2.getCows().add(cowB);
		kraal2.getCows().add(cowD);
		kraal3.getCows().add(cowA);
		kraal3.getCows().add(cowD);
		kraal4.getCows().add(cowC);
		cows.put("A", cowA);
		cows.put("B", cowB);
		cows.put("C", cowC);
		cows.put("D", cowD);
		kraals.put("1", kraal1);
		kraals.put("2", kraal2);
		kraals.put("3", kraal3);
		kraals.put("4", kraal4);
	}
	public static Cow getCowByName(String name){
		return cows.get(name);
	}
	public static Kraal getKraalByName(String name){
		return kraals.get(name);
	}
	public static Collection<Cow> getCows(){
		return cows.values();
	}
	public static Collection<Kraal> getKraals(){
		return kraals.values();
	}
}
