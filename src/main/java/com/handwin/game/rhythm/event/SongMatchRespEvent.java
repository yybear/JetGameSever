package com.handwin.game.rhythm.event;

import com.handwin.entity.User;
import com.handwin.event.MatchRespEvent;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-16 上午10:45
 */
public class SongMatchRespEvent extends MatchRespEvent {
    private Integer[] data;

    public SongMatchRespEvent(int code, User player, Integer[] data) {
        super(code, player);
        this.data = data;
    }

    public Integer[] getData() {
        return data;
    }

    public void setData(Integer[] data) {
        this.data = data;
    }
}
