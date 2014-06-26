package com.handwin.game.rhythm.match;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-23 上午9:58
 */
public class Hungarian {
    private Set<PlayerMatchItem> matchs = Sets.newHashSet();//匹配集合

    private Set<PlayerMatchItem> path = Sets.newHashSet();

    public Set<PlayerMatchItem> getMatchs() {
        return matchs;
    }

    public int findMatch(Map<String, Female> femaleInterestMap){
        if(femaleInterestMap.size() > 0) {
            for(String femalekey : femaleInterestMap.keySet()) {    //遍历二分图的子图
                if(hasAugmentPath(femaleInterestMap.get(femalekey))) {
                    for(PlayerMatchItem match : path){
                        if(matchs.contains(match)){
                            matchs.remove(match);
                        }else{
                            matchs.add(match);
                        }
                    }
                }
                path.clear();//清空增广路径信息,便于下一次遍历
            }
        }
        return matchs.size();
    }

    /**
     * 获取female的增广路径
     */
    private boolean hasAugmentPath(Female female){
        Map<String, Male> maleMap = female.getMales();
        for(String malekey : maleMap.keySet()) {
            Male male = maleMap.get(malekey);
            PlayerMatchItem match = new PlayerMatchItem(female.getName(), male.getName());
            if(matchs.contains(match)) {  //在匹配集合里
                if(female.getMales().size() == 1) {
                    return false;
                } else{
                    continue;
                }
            } else { //不再匹配集合里
                path.add(match);
                if(!male.isUsed()) { // 玩家没有被使用
                    male.setUsed(true);
                    return true;
                } else { // 玩家已经被使用了
                    Map<String , Female> map = male.getFemales();
                    for(String femaleKey : map.keySet()) {
                        Female fe = map.get(femaleKey);
                        PlayerMatchItem m = new PlayerMatchItem(fe.getName(), male.getName());
                        if(path.contains(m)) {
                            if(male.getFemales().size() == 1) {
                                return false;
                            } else {
                                continue;
                            }
                        } else if(matchs.contains(m)) {
                            path.add(m);
                            if(hasAugmentPath(fe)) {//fe有增广路径
                                return true;
                            } else{ //fe没有增广路径
                                path.remove(m);
                                continue;
                            }
                        } else{//match不在匹配集合中
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

    public void printMatchInfo(){
        System.out.println("匹配信息：" + matchs);
    }

    public static void main(String[] args) {
        Hungarian h = new Hungarian();
        Map<String, Female> femaleInterestMap = Maps.newHashMap();
        Map<String, Male> maleInterestMap = Maps.newHashMap();
        Female f1 = new Female("f1");
        Female f2 = new Female("f2");
        Female f3 = new Female("f3");

        Male m1 = new Male("m1");
        Male m2 = new Male("m2");
        Male m3 = new Male("m3");

        f1.getMales().put("m1", m1);
        f1.getMales().put("m2", m2);

        f2.getMales().put("m3", m3);
        f2.getMales().put("m2", m2);

        f3.getMales().put("m1", m1);

        m1.getFemales().put("f1", f1);
        m1.getFemales().put("f3", f3);

        m2.getFemales().put("f1", f1);
        m2.getFemales().put("f2", f2);

        m3.getFemales().put("f2", f2);

        femaleInterestMap.put("f1", f1);
        femaleInterestMap.put("f2", f2);
        femaleInterestMap.put("f3", f3);

        maleInterestMap.put("m1", m1);
        maleInterestMap.put("m2", m2);
        maleInterestMap.put("m3", m3);
        h.findMatch(femaleInterestMap);

        h.printMatchInfo();
    }
}
