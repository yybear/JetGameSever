package com.handwin.game.rhythm.match;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.handwin.event.Events;
import com.handwin.game.ChannelAttrKey;
import com.handwin.game.GameSession;
import com.handwin.game.Player;
import com.handwin.game.RandomMatchTask;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-23 下午7:47
 */
public class SongMaxMatchTask2 extends RandomMatchTask {

    private static final Logger log = LoggerFactory.getLogger(SongMaxMatchTask2.class);

    private Map<String, Female> femaleInterestMap = Maps.newHashMap();
    private Map<String, Male> maleInterestMap = Maps.newHashMap();
    private Hungarian hungarian = new Hungarian();

    private void add(Player femalePlayer, Player malePlayer) {

        String femaleId = femalePlayer.getUser().getId();
        String maleId = malePlayer.getUser().getId();
        Female female;
        if(femaleInterestMap.containsKey(femaleId)) {
            female = femaleInterestMap.get(femaleId);
        } else {
            female = new Female(femaleId);
        }

        Male male;
        if(maleInterestMap.containsKey(maleId)) {
            male = maleInterestMap.get(maleId);
        } else {
            male = new Male(maleId);
        }

        // 保存女感兴趣的男
        female.getMales().put(maleId, male);

        // 保存男感兴趣的女
        male.getFemales().put(femaleId, female);

        femaleInterestMap.put(femaleId, female);
        maleInterestMap.put(maleId, male);
    }

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
                            matched = fesongNo[h];
                            break;
                        }
                    }

                    if(matched != null) {
                        if(log.isDebugEnabled())
                            log.debug("add male {} to {}'s interested map", male.getUser().getId(), female.getUser().getId());
                        malePool.remove(j);
                        add(female, male);  // 有配对可能，放入playerMatchItemMap
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

            int size = femaleInterestMap.size();
            if(size == 0)
                return;
            // 使用匈牙利算法配对
            if(log.isDebugEnabled()) {
                log.debug("femaleInterestMap size is {}", size);
                for(String key : femaleInterestMap.keySet()) {
                    log.debug("femaleInterestMap item key is {}", key);
                    Map<String, Male> value = femaleInterestMap.get(key).getMales();
                    for(String key2 : value.keySet()) {
                        log.debug("males key is {}", key2);
                    }
                }
            }
            hungarian.findMatch(femaleInterestMap);
            Set<PlayerMatchItem> matchItemSet = hungarian.getMatchs();
            log.debug("match size is {}", matchItemSet.size());
            for(PlayerMatchItem matchItem : matchItemSet) {
                Player female = playerManager.get(matchItem.getFemale());
                Player male = playerManager.get(matchItem.getMale());
                GameSession gameSession = gameSessionManager.createSession(male, female);

                matchOk(female, male, gameSession);
            }

            hungarian.clear(); femaleInterestMap.clear(); maleInterestMap.clear(); //清理
        }
    }

    private Integer[] getSameSongNo(int[] songNo1, int[] songNo2) {
        List<Integer> list = Lists.newArrayList();
        for (int i = 0; i<songNo1.length; i++) {
            for(int j =0; j < songNo2.length; j++) {
                if(songNo1[i] == songNo2[j]) {
                    list.add(songNo1[i]);
                    break;
                }
            }
        }

        return list.toArray(new Integer[0]);
    }

    protected void matchOk(final Player player1, final Player player2, final GameSession gameSession) {
        int[] songNo1 = (int[])player1.getAttribute(Constants.SONG_NO_ATTR_KEY);
        int[] songNo2 = (int[])player2.getAttribute(Constants.SONG_NO_ATTR_KEY);
        Integer[] songs = getSameSongNo(songNo1, songNo2);

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

    public static void main(String[] args) {
        SongMaxMatchTask2 task = new SongMaxMatchTask2();

        task.getSameSongNo(new int[] {1,2,3}, new int[] {1,2,3});

    }
}
