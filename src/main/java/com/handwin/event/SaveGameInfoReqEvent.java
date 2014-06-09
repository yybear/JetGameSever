package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 下午5:16
 */
public class SaveGameInfoReqEvent extends Event {

    private int score;

    public SaveGameInfoReqEvent(int score) {
        this.score = score;

        this.type = Events.SAVE_GAME_INFO;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
