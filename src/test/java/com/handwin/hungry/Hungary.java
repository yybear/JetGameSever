package com.handwin.hungry;

import java.util.HashSet;
import java.util.Set;

public class Hungary {
	private Set<Match> matchs=new HashSet<Match>();//匹配集合
	private Set<Match> path=new HashSet<Match>();
	public int findMatch(){
		for(Cow cow:DateSource.getCows()){//遍历二分图的子图cow
			if(hasAugmentPath(cow)){
				for(Match match:path){
					if(matchs.contains(match)){
						matchs.remove(match);
					}else{
						matchs.add(match);
					}
				}
			}
			path.clear();//清空增广路径信息,便于下一次遍历
		}
		return matchs.size();
	}
	public void printMatchInfo(){
		System.out.println("匹配信息："+matchs);
	}
	/**
	 * 获取cow的增广路径
	 */
	private boolean hasAugmentPath(Cow cow){
		for(Kraal kraal:cow.getKraals()){
			Match match=new Match(cow.getName(),kraal.getName());
			if(matchs.contains(match)){//在匹配集合里
				if(cow.getKraals().size()==1){
					return false;
				}else{
					continue;
				}
			}else{//不再匹配集合里
				path.add(match);
				if(!kraal.isUsed()){//kraal没有被使用
					kraal.setUsed(true);
					return true;
				}else{//kraal已经被使用了
					for(Cow c:kraal.getCows()){
						Match m=new Match(c.getName(),kraal.getName());
						if(path.contains(m)){//match在path里
							if(kraal.getCows().size()==1){
								return false;
							}else{
								continue;
							}
						}else if(matchs.contains(m)){//match在匹配集合中
							path.add(m);//将match加入path
							if(hasAugmentPath(c)){//c有增广路径
								return true;
							}else{//c没有增广路径
								path.remove(m);
								continue;
							}
						}else{//match不在匹配集合中
							continue;
						}
					}
					//遍历下一个kraal,采用DFS遍历
					path.remove(match);
					continue;
				}
			}
		}
		return false;
	}
}
