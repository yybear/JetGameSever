package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 下午2:11
 */
public class InviteReqEvent extends Event {
    private String[] player;

    public InviteReqEvent(String[] player) {
        this.player = player;
        this.type = Events.INVITE_PLAYER;
    }

    public String[] getPlayer() {
        return player;
    }

    public void setPlayer(String[] player) {
        this.player = player;
    }
}
