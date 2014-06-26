package com.handwin.entity.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Order;

import java.util.Date;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-25 下午4:25
 */
@Entity(table = "game_song_score")
public class GameSongScore {
    @EmbeddedId
    private GameSongScoreKey key;

    @Column
    private Integer score;

    public GameSongScore() {
    }

    public GameSongScore(GameSongScoreKey key, Integer score) {
        this.key = key;
        this.score = score;
    }

    public GameSongScoreKey getKey() {
        return key;
    }

    public void setKey(GameSongScoreKey key) {
        this.key = key;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public static class GameSongScoreKey {
        @Column
        @Order(1)
        private String uid;

        @Column
        @Order(2)
        private Date date;

        public GameSongScoreKey() {
        }

        public GameSongScoreKey(String uid, Date date) {
            this.uid = uid;
            this.date = date;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Override
    public String toString() {
        return this.key.getUid() + "," + this.key.getDate() + "," + this.score;
    }
}
