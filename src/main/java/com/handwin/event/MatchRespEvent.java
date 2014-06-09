package com.handwin.event;

import com.handwin.entity.User;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-20 上午9:50
 */
public class MatchRespEvent extends Event {
    private int code;

    private User player;

    //private String gameSessionId;

    public MatchRespEvent(int code, User player) {
        this.type = Events.JOIN_WAIT_QUEUE;
        this.code = code;
        this.player = player;
        //this.gameSessionId = gameSessionId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    /*public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }*/
}
