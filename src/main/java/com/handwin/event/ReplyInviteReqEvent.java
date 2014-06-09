package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-19 上午9:33
 */
public class ReplyInviteReqEvent extends Event {
    private Integer code;

    private String player;

    public ReplyInviteReqEvent(String player) {
        this.type = Events.REPLY_INVITE;
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
