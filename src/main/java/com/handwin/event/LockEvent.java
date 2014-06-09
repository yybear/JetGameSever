package com.handwin.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: roger
 * Date: 13-12-16 上午11:05
 */
public class LockEvent extends Event {
    private String player;

    @JsonProperty(value = "lock_code")
    private String code;

    private int timeout;

    public LockEvent() {
        this.type = Events.RES_LOCK;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static void main(String[] args) {

    }
}
