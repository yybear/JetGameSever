package com.handwin.game;

import com.google.common.collect.Lists;
import com.handwin.event.Events;
import com.handwin.event.MatchRespEvent;
import com.handwin.util.Constants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-20 上午9:18
 */
public class RandomMatchTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RandomMatchTask.class);
    protected LinkedBlockingQueue<Player> maleWaitQueue = new LinkedBlockingQueue<Player>(10000);
    protected LinkedBlockingQueue<Player> femaleWaitQueue = new LinkedBlockingQueue<Player>(10000);

    @Autowired
    protected PlayerManager playerManager;

    @Autowired
    protected GameSessionManager gameSessionManager;

    @Value("${max.wait.time}")
    protected long maxWaitTime;

    public void joinQueue(JsonNode node, Channel channel) {
        // 根据性别放入不同的队列
        String me = channel.attr(ChannelAttrKey.PLAYERNAME_ATTR_KEY).get();
        Player player = playerManager.get(me);
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

    @Override
    public void run() {
        match(maleWaitQueue, femaleWaitQueue);
    }

    protected void match(LinkedBlockingQueue<Player> maleWaitQueue, LinkedBlockingQueue<Player> femaleWaitQueue) {
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
            Player male = maleWaitQueue.poll();
            Player female = femaleWaitQueue.poll();
            GameSession gameSession = gameSessionManager.createSession(male, female);
            matchOk(male, female, gameSession);
            match(maleWaitQueue, femaleWaitQueue);
        }
    }

    protected void cannotMatch(LinkedBlockingQueue<Player> queue, int size) {
        List<Player> tempList = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
            tempList.add(queue.poll());
        }

        for (int i = 0; i < size; i++) {
            cannotMatch(tempList.get(i), queue);
        }
    }

    protected void cannotMatch(Player player, LinkedBlockingQueue<Player> queue) {
        Long inTime = (Long)player.getAttribute(Constants.JOIN_QUEUE_TIME);

        if((System.currentTimeMillis() - inTime) >= maxWaitTime) {
            // 等的时间太长了，直接回复匹配错误
            player.channel().writeAndFlush(new MatchRespEvent(Events.ACTION_FAILED, null));
        } else {
            queue.offer(player); // 重新入队列
        }
    }

    protected void matchOk(final Player player1, final Player player2, final GameSession gameSession) {
        ChannelFuture player1Future = player1.channel().writeAndFlush(new MatchRespEvent(Events.ACTION_SUCCESS, player2.getUser()));
        ChannelFuture player2Future = player2.channel().writeAndFlush(new MatchRespEvent(Events.ACTION_SUCCESS, player1.getUser()));
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

    public static void main(String[] args) {
        /*RandomMatchTask maxMatchTask = new RandomMatchTask();

        User user1 = new User();
        user1.setSex(0);
        user1.setId("user1");
        Player player1 = new Player(user1);

        User user2 = new User();
        user2.setSex(0);
        user2.setId("user2");
        Player player2 = new Player(user2);
        maxMatchTask.femaleWaitQueue.offer(player1);
        maxMatchTask.femaleWaitQueue.offer(player2);

        User male1 = new User();
        male1.setSex(1);
        male1.setId("male1");
        Player mplayer1 = new Player(male1);
        maxMatchTask.maleWaitQueue.offer(mplayer1);

        do {
            maxMatchTask.run();
        } while (true);*/

        List<String> members = Lists.asList("dd", "dd", new String[]{});
        System.out.println(members.get(0) + "," + members.get(1));

    }
}
