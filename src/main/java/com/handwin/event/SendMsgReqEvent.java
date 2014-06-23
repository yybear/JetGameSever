package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-21 下午1:12
 */
public class SendMsgReqEvent extends Event {
    private String player;
    private String msg;

    public SendMsgReqEvent(String msg, String player) {
        this.msg = msg;
        this.player = player;
        this.type = Events.SEND_PUSH_MSG;
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
