package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-28 下午6:06
 */
public class UserReadyRespEvent extends Event {
    private int code;
    private String player;

    public UserReadyRespEvent(int code, String player) {
        this.type = Events.PLAYER_READY;
        this.code = code;
        this.player = player;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
