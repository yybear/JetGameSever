package com.handwin.game;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * User: roger
 * Date: 13-12-17 下午2:24
 */
public class ChannelAttrKey {
    public  static final AttributeKey<Integer> GAME_CLOSESTATE_ATTR_KEY = AttributeKey.valueOf("gameCloseState");
    public  static final AttributeKey<String> PLAYERNAME_ATTR_KEY = AttributeKey.valueOf("playerName");
    public  static final AttributeKey<Integer> APPID_ATTR_KEY = AttributeKey.valueOf("appId");
    public  static final AttributeKey<String> PLAYERSESSION_ATTR_KEY = AttributeKey.valueOf("playerSession");
    public  static final AttributeKey<String> GAMESESSION_ID_ATTR_KEY = AttributeKey.valueOf("gameSessionID");

    public static boolean isUserCloseGame(Channel channel) {
        return channel.attr(GAME_CLOSESTATE_ATTR_KEY).get() != null;
    }

}
