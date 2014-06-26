package com.handwin.game.rhythm.event;

import com.handwin.event.Event;
import com.handwin.event.Events;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-25 下午4:36
 */
public class BattleScoreRespEvent extends Event {
    private Integer battleScore;
    private int code;


    public BattleScoreRespEvent(Integer battleScore, int code) {
        this.battleScore = battleScore;
        this.code = code;
        this.type = Events.BATTLE_SCORE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Integer getBattleScore() {
        return battleScore;
    }

    public void setBattleScore(Integer battleScore) {
        this.battleScore = battleScore;
    }
}
