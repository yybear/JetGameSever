package com.handwin.event;

import com.handwin.entity.User;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-16 下午4:19
 */
public class LoginGameRespEvent extends Event {
    private int code;
    private User user;

    public LoginGameRespEvent(int code, User user) {
        this.type = Events.LOGIN_GAME;
        this.code = code;
        this.user = user;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
