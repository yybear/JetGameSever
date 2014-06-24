package com.handwin.game.rhythm.match;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 上午11:35
 */
public class Female {
    private String name;

    private Map<String, Male> males;

    public Female(String name) {
        this.name = name;
        males = Maps.newHashMap();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Male> getMales() {
        return males;
    }

    public void setMales(Map<String, Male> males) {
        this.males = males;
    }
}
