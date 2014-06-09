package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-20 下午4:09
 */
public class AddFriendsRespEvent extends Event {
    private int code;

    public AddFriendsRespEvent(int code) {
        this.code = code;
        this.type = Events.ADD_FRIENDS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
