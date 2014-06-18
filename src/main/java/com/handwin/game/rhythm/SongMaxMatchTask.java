package com.handwin.game.rhythm;

import com.google.common.collect.Lists;
import com.handwin.event.Events;
import com.handwin.game.*;
import com.handwin.game.rhythm.event.SongMatchRespEvent;
import com.handwin.util.Constants;
import com.handwin.util.Jackson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-20 下午2:24
 */
public class SongMaxMatchTask extends RandomMatchTask {

    private static final Logger log = LoggerFactory.getLogger(SongMaxMatchTask.class);

    @Autowired
    protected PlayerManager playerManager;

    @Autowired
    protected GameSessionManager gameSessionManager;

    @Override
    public void run() {
        int maleSize = maleWaitQueue.size();
        int femaleSize = femaleWaitQueue.size();
        if(maleSize == 0 && femaleSize == 0) {
            // 队列里面没有，停止匹配等待下次扫描
            return;
        } else if(maleSize == 0 && femaleSize > 0) {
            cannotMatch(femaleWaitQueue, femaleSize);
        } else if(maleSize > 0 && femaleSize == 0) {
            cannotMatch(maleWaitQueue, maleSize);
        } else {
            // 将要配对的放入池，避免和队列里面的混淆
            List<Player> femalePool = Lists.newArrayList();
            List<Player> malePool = Lists.newArrayList();
            while(true) {
                Player female = femaleWaitQueue.poll();
                if(female == null)
                    break;
                femalePool.add(female);
            }
            while(true) {
                Player male = maleWaitQueue.poll();
                if(male == null)
                    break;
                malePool.add(male);
            }

            for(int i = 0; i < femalePool.size(); i++) {
                Player female = femalePool.get(i);
                int[] fesongNo = (int[])female.getAttribute(Constants.SONG_NO_ATTR_KEY);

                Integer matched = null;
                for(int j = 0; j < malePool.size(); j++) {
                    Player male = malePool.get(j);
                    int[] msongNo = (int[])male.getAttribute(Constants.SONG_NO_ATTR_KEY);
                    for(int h = 0; h < msongNo.length; h++) {
                        if(fesongNo[h] == msongNo[h]) {
                            // TODO: 先简单点只要一个成功就可以了
                            matched = fesongNo[h];
                            break;
                        }
                    }

                    if(matched != null) {
                        log.debug("match {},{}", male.getUser().getId(), female.getUser().getId());
                        GameSession gameSession = gameSessionManager.createSession(male, female);
                        malePool.remove(j);
                        log.debug("malePool size left {}", malePool.size());
                        final Integer[] songs = new Integer[]{matched};
                        matchOk(male, female, gameSession, songs);
                        break;
                    }
                }

                if(null == matched) {
                    log.debug("female {} can't match", female.getUser().getId());
                    cannotMatch(female, femaleWaitQueue);
                }

            }

            // 匹配完成后，剩下的male 也没有匹配成功
            cannotMatch(malePool, maleWaitQueue);
        }
    }

    private void matchOk(final Player player1, final Player player2, final GameSession gameSession, Integer[] songs) {
        ChannelFuture player1Future = player1.channel().writeAndFlush(new SongMatchRespEvent(Events.ACTION_SUCCESS, player2.getUser(), songs));
        ChannelFuture player2Future = player2.channel().writeAndFlush(new SongMatchRespEvent(Events.ACTION_SUCCESS, player1.getUser(), songs));
        player1Future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    Channel channel = future.channel();
                    channel.attr(ChannelAttrKey.GAMESESSION_ID_ATTR_KEY).set(gameSession.getGameSessionID());
                    gameSession.playerJoin(player1);
                }
            }
        });
        player2Future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    Channel channel = future.channel();
                    channel.attr(ChannelAttrKey.GAMESESSION_ID_ATTR_KEY).set(gameSession.getGameSessionID());
                    gameSession.playerJoin(player2);
                }
            }
        });
    }

    private void cannotMatch(List<Player> players, LinkedBlockingQueue<Player> queue) {
        for(Player player : players) {
            log.debug("player {} with sex {} can't match", player.getUser().getId(), player.getUser().getSex());
            cannotMatch(player, queue);
        }
    }

    @Override
    public void joinQueue(JsonNode node, Channel channel) {
        int[] songNo;
        try {
            songNo = Jackson.fromJson(node.get("data"), new TypeReference<int[]>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("can't parse song no.", e);
        }

        // 根据性别放入不同的队列
        String me = channel.attr(ChannelAttrKey.PLAYERNAME_ATTR_KEY).get();
        Player player = playerManager.get(me);
        player.setAttribute(Constants.SONG_NO_ATTR_KEY, songNo);
        boolean res = false;
        if(player.getUser().getSex() == Constants.FEMALE) {
            res = femaleWaitQueue.offer(player);
        } else if(player.getUser().getSex() == Constants.MALE) {
            res = maleWaitQueue.offer(player);
        }

        if(res) {
            log.debug("player {} join in wait queue", player.getUser().getId());
            player.setAttribute("joinQueueTime", System.currentTimeMillis());
        }
    }
}
