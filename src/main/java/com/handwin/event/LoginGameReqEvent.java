package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 上午9:22
 */
public class LoginGameReqEvent extends Event {
    private String sessionId;
    private int appId;

    public LoginGameReqEvent(String sessionId, int appId) {
        this.sessionId = sessionId;
        this.appId = appId;
        this.type = Events.LOGIN_GAME;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }
}
