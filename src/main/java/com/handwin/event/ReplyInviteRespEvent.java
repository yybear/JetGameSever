package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 下午2:20
 */
public class ReplyInviteRespEvent extends Event {
    private String player;
    private int code;

    public ReplyInviteRespEvent(String player, int code) {
        this.player = player;
        this.code = code;
        this.type = Events.REPLY_INVITE;
    }


    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
