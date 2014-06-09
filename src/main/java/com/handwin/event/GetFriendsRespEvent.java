package com.handwin.event;

import com.handwin.entity.User;

import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-16 下午7:35
 */
public class GetFriendsRespEvent extends Event {
    private int code;
    private List<User> friends;

    public GetFriendsRespEvent(int code, List<User> friends) {
        this.code = code;
        this.type = Events.GET_FRIENDS;
        this.friends = friends;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
