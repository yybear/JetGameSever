package com.handwin.entity;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-4 下午1:36
 */
public class UserScore {
    private int Score;

    private User user;

    public UserScore(int score, User user) {
        Score = score;
        this.user = user;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
