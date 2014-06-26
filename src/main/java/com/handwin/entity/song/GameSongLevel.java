package com.handwin.entity.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-25 下午5:32
 */
@Entity(table = "game_song_levels")
public class GameSongLevel {
    @Id
    private Integer level;

    @Column
    private Integer experience;

    @Column(name="single_experience")
    private Integer singleExperience;

    @Column(name="two_experience")
    private Integer twoExperience;

    @Column
    private Integer addition;

    public GameSongLevel() {
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getSingleExperience() {
        return singleExperience;
    }

    public void setSingleExperience(Integer singleExperience) {
        this.singleExperience = singleExperience;
    }

    public Integer getAddition() {
        return addition;
    }

    public void setAddition(Integer addition) {
        this.addition = addition;
    }

    public Integer getTwoExperience() {
        return twoExperience;
    }

    public void setTwoExperience(Integer twoExperience) {
        this.twoExperience = twoExperience;
    }
}
