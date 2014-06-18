package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.type.Counter;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-10 下午3:45
 * 游戏在线人数
 */
@Entity(table = "game_online_counter")
public class GameOnlineCount {
    @Id
    private Integer id;

    @Column
    private Counter counter;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }
}