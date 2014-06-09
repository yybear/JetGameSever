package com.handwin.game;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.handwin.game.ChannelAttrKey.PLAYERNAME_ATTR_KEY;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-16 下午4:44
 */
@Component
public class PlayerManager {
    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);

    private Map<String, Player> onlineClientMap = new ConcurrentHashMap<String, Player>();

    private final ChannelFutureListener remover = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            Channel channel = future.channel();
            String name = channel.attr(PLAYERNAME_ATTR_KEY).get();
            clear(name);
        }
    };

    public void addPlayer(Player player) {
        String id = player.getUser().getId();
        log.debug("add player {} to PlayerManager onlineClientMap", id);
        onlineClientMap.put(id, player);

        player.channel().closeFuture().addListener(remover);
    }

    public void clear(String name) {
        log.debug("remove player {} from PlayerManager onlineClientMap", name);
        onlineClientMap.remove(name);
    }

    public Player get(String name) {
        return onlineClientMap.get(name);
    }

    public boolean contain(String name) {
        return onlineClientMap.containsKey(name);
    }
}
