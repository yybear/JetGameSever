package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 下午1:51
 */
public class AddFriendReqEvent extends Event {
    private String[] players;

    public AddFriendReqEvent(String[] players) {
        this.players = players;
        this.type = Events.ADD_FRIENDS;
    }

    public String[] getPlayers() {
        return players;
    }

    public void setPlayers(String[] players) {
        this.players = players;
    }

}
