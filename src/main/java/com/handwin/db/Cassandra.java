package com.handwin.db;

import com.handwin.entity.GameOnlineCount;
import com.handwin.entity.GamePlayer;
import com.handwin.entity.GamePlayerKey;
import com.handwin.entity.song.GameSongLevel;
import com.handwin.entity.song.GameSongScore;
import com.handwin.util.ConfigUtils;
import com.handwin.util.Constants;
import com.handwin.util.TimeUtils;
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

    public GamePlayer initPlayer(String uid, Integer gameId) {
        log.debug("init player uid {}, gameId {}", uid, gameId);
        GamePlayer player = getPlayer(uid, gameId);
        if(player == null) {
            log.debug("player is null, insert");
            player = new GamePlayer(new GamePlayerKey(uid, gameId), Constants.INT_LEVEL, 0, 0, Constants.INT_TITLE, 0);
            player.setThreeStarNum(0);
            player.setLoseNum(0);
            player.setTieNum(0);
            player.setPower(ConfigUtils.getInt("game.power"));
            player.setPowerConsumeTime(0l);
            player.setPowerRecoverTime(0L);
            manager.persist(player);
        }

        return player;
    }

    public GamePlayer getPlayer(String uid, Integer gameId) {
        return manager.find(GamePlayer.class, new GamePlayerKey(uid, gameId));
    }

    public GameSongLevel getGameSongLevel(Integer level) {
        return manager.find(GameSongLevel.class, level);
    }

    public GameSongScore getSongScore(String uid) {
        return manager.find(GameSongScore.class, new GameSongScore.GameSongScoreKey(uid, TimeUtils.getTodayStart()));
    }
}
