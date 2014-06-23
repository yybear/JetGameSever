package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-19 下午2:17
 */
public class GamePlayerKey {
    @Column
    @Order(1)
    private String uid;

    @Column(name="game_id")
    @Order(2)
    private Integer gameId;

    public GamePlayerKey() {
    }

    public GamePlayerKey(String uid, Integer gameId) {
        this.uid = uid;
        this.gameId = gameId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
}
