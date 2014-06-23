package com.handwin.entity;

import info.archinnov.achilles.annotations.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午4:47
 */
@Entity(table = "game_players")
public class GamePlayer {
    @EmbeddedId
    private GamePlayerKey id;

    @Column
    private Integer level;

    @Column
    private Integer stars;

    @Column
    private Integer experience;

    @Column
    private Integer title;

    /**
     * 对战赢得场次
     */
    @Column(name = "win_num")
    private Integer winNum;

    /**
     * 获得三星的场次
     */
    @Column(name = "three_star_num")
    private Integer threeStarNum;

    /**
     * 对战平局场次
     */
    @Column(name = "tie_num")
    private Integer tieNum;

    /**
     * 对战输掉场次
     */
    @Column(name = "lose_num")
    private Integer loseNum;

    public GamePlayer() {
    }

    public GamePlayer(GamePlayerKey id, Integer level, Integer stars, Integer experience, Integer title, Integer winNum) {
        this.id = id;
        this.level = level;
        this.stars = stars;
        this.experience = experience;
        this.title = title;
        this.winNum = winNum;
    }

    public Integer getTieNum() {
        return tieNum;
    }

    public void setTieNum(Integer tieNum) {
        this.tieNum = tieNum;
    }

    public Integer getLoseNum() {
        return loseNum;
    }

    public void setLoseNum(Integer loseNum) {
        this.loseNum = loseNum;
    }

    public Integer getThreeStarNum() {
        return threeStarNum;
    }

    public void setThreeStarNum(Integer threeStarNum) {
        this.threeStarNum = threeStarNum;
    }

    public Integer getWinNum() {
        return winNum;
    }

    public void setWinNum(Integer winNum) {
        this.winNum = winNum;
    }
    public GamePlayerKey getId() {
        return id;
    }

    public void setId(GamePlayerKey id) {
        this.id = id;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTitle() {
        return title;
    }

    public void setTitle(Integer title) {
        this.title = title;
    }
}
