package com.handwin.db;

import com.handwin.entity.GameOnlineCount;
import com.handwin.entity.GamePlayer;
import com.handwin.entity.GamePlayerKey;
import com.handwin.util.Constants;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午9:19
 */
public class Cassandra {
    private static final Logger log = LoggerFactory.getLogger(Cassandra.class);
    @Autowired
    private PersistenceManager manager;

    public void updateGameOnlineNum(Integer gameId, boolean incr) {
        GameOnlineCount onlineCount = manager.getProxy(GameOnlineCount.class, gameId);
        if(null == onlineCount) {
            onlineCount = new GameOnlineCount();
            onlineCount.setId(gameId);
            onlineCount.getCounter().incr();
            manager.persist(onlineCount);
        } else {
            if(incr)
                onlineCount.getCounter().incr();
            else
                onlineCount.getCounter().decr();

            manager.update(onlineCount);
        }
    }

    public void initPlayer(String uid, Integer gameId) {
        log.debug("init player uid {}, gameId {}", uid, gameId);
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameId));
        if(player == null) {
            log.debug("player is null, insert");
            player = new GamePlayer(new GamePlayerKey(uid, gameId), Constants.INT_LEVEL, 0, 0, Constants.INT_TITLE, 0);
            player.setThreeStarNum(0);
            player.setLoseNum(0);
            player.setTieNum(0);
            manager.persist(player);
        }
    }
}
