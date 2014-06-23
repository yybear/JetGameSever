package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-21 下午12:59
 */
public class SendPushMsgRespEvent extends  Event{
    private String player;
    private String msg;

    public SendPushMsgRespEvent(String player, String msg) {
        this.player = player;
        this.msg = msg;
        this.type = Events.FORWARD_PUSH_MSG;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
