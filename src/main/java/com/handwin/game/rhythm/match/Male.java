package com.handwin.game.rhythm.match;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 上午11:37
 */
public class Male {
    private String name;
    private boolean used;

    private Map<String, Female> females;

    public Male(String name) {
        this.name = name;
        used = false;
        females = Maps.newHashMap();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Map<String, Female> getFemales() {
        return females;
    }

    public void setFemales(Map<String, Female> females) {
        this.females = females;
    }
}
