package com.handwin.game.rhythm.match;

import com.handwin.game.Player;

import java.util.List;

/**
 * 匹配的元素，包含主体对象和可能匹配的对象
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-23 上午10:15
 */
public class PlayerMatchItem {
    private Player playerA;   // 主体玩家

    private List<Player> playerBs;   // 潜在匹配玩家

    public Player getPlayerA() {
        return playerA;
    }

    public void setPlayerA(Player playerA) {
        this.playerA = playerA;
    }

    public List<Player> getPlayerBs() {
        return playerBs;
    }

    public void setPlayerBs(List<Player> playerBs) {
        this.playerBs = playerBs;
    }
}
