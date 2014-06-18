package com.handwin.db;

import com.handwin.entity.GameOnlineCount;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午9:19
 */
public class Cassandra {
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
}
