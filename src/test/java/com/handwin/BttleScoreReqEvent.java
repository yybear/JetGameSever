package com.handwin;

import com.handwin.event.Event;
import com.handwin.event.Events;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-25 下午5:47
 */
public class BttleScoreReqEvent extends Event {
    private Integer playerScore;
    private Integer noteScore;

    public BttleScoreReqEvent(Integer playerScore, Integer noteScore) {
        this.playerScore = playerScore;
        this.noteScore = noteScore;
        this.type = Events.BATTLE_SCORE;
    }

    public Integer getPlayerScore() {
        return playerScore;
    }

    public void setPlayerScore(Integer playerScore) {
        this.playerScore = playerScore;
    }

    public Integer getNoteScore() {
        return noteScore;
    }

    public void setNoteScore(Integer noteScore) {
        this.noteScore = noteScore;
    }
}
