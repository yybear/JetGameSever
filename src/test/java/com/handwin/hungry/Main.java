package com.handwin.hungry;

public class Main {
	public static void main(String[] args) {
		Hungary test=new Hungary();
		System.out.println("最大匹配数："+test.findMatch());
		test.printMatchInfo();
	}
}
