package com.handwin.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: roger
 * Date: 13-12-16 上午11:46
 */
public class LockCompleteEvent extends Event {
    private String player;
    @JsonProperty(value = "lock_code")
    private String code;
    @JsonProperty(value = "is_timeout")
    private boolean timeout = false;

    public LockCompleteEvent(String player,String code,boolean timeout) {

        this.type = Events.RES_LOCK_COMPLETE;
        this.player = player;
        this.code = code;
        if(timeout) {
            this.timeout = timeout;
        }

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

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }
}
