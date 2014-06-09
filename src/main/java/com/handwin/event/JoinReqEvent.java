package com.handwin.event;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * User: roger
 * Date: 13-12-13 下午2:16
 */
public class JoinReqEvent extends Event {
    @JsonProperty("app_id")
    private int appId;
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("game_session_id")
    private String gameSessionId;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }




    public static void main(String[] args) throws Exception {




    }
}
