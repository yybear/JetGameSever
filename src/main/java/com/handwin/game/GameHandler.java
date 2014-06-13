package com.handwin.game;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Un Thread Safe，不能有自己的instance variable
 * User: roger
 * Date: 13-12-16 下午4:10
 */
public interface GameHandler {

    /**
     * 有新玩家进入游戏会话
     * @param playerName 玩家NameMD5
     */
    public void onPlayerConnected(String playerName, GameSession session);

    /**
     *有玩家退出游戏
     * @param playerName
     * @param exitCode 1:游戏正常结束 2:提前退出游戏 3:掉线
     */
    public void onPlayerDisconnected(String playerName, int exitCode, GameSession session);

    /**
     * 资源锁条件满足触发event
     * @param resourceLock
     */
    public void onLockComplete(ResourceLock resourceLock, GameSession session);

    /**
     *
     * @param message
     */
    public void onReceiveMessage(String playerName, JsonNode message, GameSession session);

    /**
     * 清除游戏会话状态
     */
    public void clear(GameSession session);


}
