package com.handwin.game.rhythm.match;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 上午10:54
 */
public class PlayerInterested {
    private String player;
    private boolean used;

    public PlayerInterested(String player) {
        this.player = player;
        this.used = false;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
