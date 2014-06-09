package com.handwin.event;

import java.util.List;

/**
 * User: roger
 * Date: 13-12-13 下午2:20
 */
public class JoinRespEvent extends Event {
    private int code;
    private List<String> players;
    private String desc;
    public JoinRespEvent(int code, List<String> players, String desc) {
        this.type = Events.LOG_IN;
        this.code = code;
        this.players = players;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
