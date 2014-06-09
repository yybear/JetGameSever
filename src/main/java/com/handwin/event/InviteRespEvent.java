package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-19 上午9:44
 */
public class InviteRespEvent extends Event {
    private int code;
    private String player;
    public InviteRespEvent(int code, String player) {
        this.code = code;
        this.player = player;
        this.type = Events.INVITE_PLAYER;
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
