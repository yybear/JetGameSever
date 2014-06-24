package com.handwin.hungry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DateSource {
	static Map<String,Cow> cows=new HashMap<String,Cow>();
	static Map<String,Kraal> kraals=new HashMap<String,Kraal>();
	static{
		Cow cowA=new Cow("A");
		Cow cowB=new Cow("B");
		Cow cowC=new Cow("C");
		Kraal kraal1=new Kraal("1");
		Kraal kraal2=new Kraal("2");
		Kraal kraal3=new Kraal("3");
		cowA.getKraals().add(kraal1);
        cowA.getKraals().add(kraal2);
		cowB.getKraals().add(kraal2);
        cowB.getKraals().add(kraal3);
		cowC.getKraals().add(kraal1);

		kraal1.getCows().add(cowA);
		kraal1.getCows().add(cowB);
		kraal2.getCows().add(cowB);
        kraal2.getCows().add(cowC);
		kraal3.getCows().add(cowA);
		cows.put("A", cowA);
		cows.put("B", cowB);
		cows.put("C", cowC);
		kraals.put("1", kraal1);
		kraals.put("2", kraal2);
		kraals.put("3", kraal3);
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
