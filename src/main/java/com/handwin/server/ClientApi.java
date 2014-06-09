package com.handwin.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handwin.util.Jackson;
import com.handwin.util.RedisKey;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * User: roger
 * Date: 13-12-16 下午4:51
 */
public class ClientApi {
    private static Logger LOG = LoggerFactory.getLogger(ClientApi.class);

    private static final ObjectMapper mapper = Jackson.mapper();

    @Autowired
    private JedisPool jedisPool;

    public List<String> getMembers(final int appId, final String gameSessionID) {
        Jedis jedis = this.jedisPool.getResource();
        try {
            String gameSessionData = jedis.hget("" + RedisKey.RANDOM_MATCH_RESULT + appId, gameSessionID);
            String[] data = StringUtils.split(gameSessionData, ",");
            List<String> members = new ArrayList<String>();
            for (String d : data) {
                members.add(d);
            }
            LOG.trace("member list is = [{}]", members);
            return members;
        } catch (Exception e) {
            LOG.error("error getting game members. {}", e);
            return null;
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }

    public void saveMembers(int appId, String gameSessionID, String[] members) {
        String msg = StringUtils.join(members, ",");
        Jedis jedis = this.jedisPool.getResource();
        try {
            jedis.hset("" + RedisKey.RANDOM_MATCH_RESULT + appId, gameSessionID, msg);
        } catch (Exception e) {
            LOG.error("error setting game members. {}", e);
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }

    public String getUserMd5(String sessionId) {
        Jedis jedis = this.jedisPool.getResource();
        try {
            return jedis.get(RedisKey.USER_SESSION_MD5 + sessionId);
        } catch (Exception e) {
            LOG.error("get user md5 error. sessioinId=" + sessionId, e);
            return null;
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }

    public void setPlaying(String name, int appId) {
        Jedis jedis = this.jedisPool.getResource();
        try {
            jedis.sadd(RedisKey.PLAYER_STATUS + name, String.valueOf(appId));
        } catch (Exception e) {
            LOG.error("set user:" + name + " playing status error.", e);
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }

    public void removeGameSession(int gameID, String gameSessionID) {
        Jedis jedis = this.jedisPool.getResource();
        try {
            jedis.hdel("" + RedisKey.RANDOM_MATCH_RESULT + gameID, gameSessionID);
        } catch (Exception e) {
            LOG.error("remove game session error. appid=" + gameID + " gameSessionId=" + gameSessionID, e);
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }

    public void removePlaying(String player, int appId) {
        Jedis jedis = this.jedisPool.getResource();
        try {
            jedis.srem(RedisKey.PLAYER_STATUS + player, String.valueOf(appId));
        } catch (Exception e) {
            LOG.error("remove user:" + player + " playing status error.", e);
        } finally {
            this.jedisPool.returnResource(jedis);
        }
    }
}
