package com.handwin.game.empty;

import com.google.common.collect.Maps;
import com.handwin.game.ChannelAttrKey;
import com.handwin.game.Player;
import com.handwin.game.RandomMatchTask;
import com.handwin.util.Constants;
import io.netty.channel.Channel;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.handwin.game.ChannelAttrKey.APPID_ATTR_KEY;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-30 上午10:43
 */
public class EmptyRandomMatchTask extends RandomMatchTask {
    private static final Logger log = LoggerFactory.getLogger(RandomMatchTask.class);
    private Map<Integer, Queue[]> waitQueueMap = Maps.newConcurrentMap();

    @Override
    public void joinQueue(JsonNode node, Channel channel) {
        Integer appId = channel.attr(APPID_ATTR_KEY).get();
        if(!waitQueueMap.containsKey(appId)) {
            synchronized (this) {  // 没有，就初始化
                if(!waitQueueMap.containsKey(appId)) {
                    LinkedBlockingQueue<Player> maleWaitQueue = new LinkedBlockingQueue<Player>();
                    LinkedBlockingQueue<Player> femaleWaitQueue = new LinkedBlockingQueue<Player>();

                    waitQueueMap.put(appId, new Queue[] {maleWaitQueue, femaleWaitQueue});
                }
            }
        }
        Queue[] queues = waitQueueMap.get(appId);
        LinkedBlockingQueue<Player> maleWaitQueue = (LinkedBlockingQueue<Player>)queues[0];
        LinkedBlockingQueue<Player> femaleWaitQueue = (LinkedBlockingQueue<Player>)queues[1];

        String me = channel.attr(ChannelAttrKey.PLAYERNAME_ATTR_KEY).get();
        Player player = playerManager.get(me);
        boolean res = false;
        if(player.getUser().getSex() == Constants.FEMALE) {
            res = femaleWaitQueue.offer(player);
        } else if(player.getUser().getSex() == Constants.MALE) {
            res = maleWaitQueue.offer(player);
        }

        if(res) {
            log.debug("player {}, sex {} join in wait queue", player.getUser().getId(), player.getUser().getSex());
            player.setAttribute("joinQueueTime", System.currentTimeMillis());
        }
    }

    @Override
    public void run() {
        for(Integer id : waitQueueMap.keySet()) {
            Queue[] queues = waitQueueMap.get(id);
            LinkedBlockingQueue<Player> maleWaitQueue = (LinkedBlockingQueue<Player>)queues[0];
            LinkedBlockingQueue<Player> femaleWaitQueue = (LinkedBlockingQueue<Player>)queues[1];
            match(maleWaitQueue, femaleWaitQueue);
        }
    }
}
