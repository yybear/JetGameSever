package com.handwin.event;

/**
 * User: roger
 * Date: 13-12-13 下午2:32
 */
public class StatusEvent extends Event {
    private String player;

    public StatusEvent() {

    }
    public StatusEvent(StatusCode code, String player) {
        this.type = code.type();
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
