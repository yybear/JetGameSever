package com.handwin.game;

import com.handwin.event.*;
import com.handwin.server.ClientApi;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * User: roger
 * Date: 13-12-13 上午11:31
 */
public class GameSession {
    private static Logger LOG = LoggerFactory.getLogger(GameSession.class);
    private static HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("LOCK_TIMEOUT"), 1, TimeUnit.SECONDS);

    private GameSessionState state = GameSessionState.CREATED;

    private Timeout lockTimeout;
    private ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<String, Player>();
    private ConcurrentHashMap<String, Boolean> lockState = new ConcurrentHashMap<String, Boolean>();
    private List<String> members = new ArrayList<String>();
    private Map<String,Object> attributes = new HashMap<String,Object>();
    private int gameID;
    private String gameSessionID;

    private ClientApi client;
    private GameSessionManager gameSessionManager;

    private GameHandler gameHandler;

    private AtomicInteger playerReadyNum = new AtomicInteger(0);

    // 对战成绩同步
    private AtomicInteger battleScoreNum = new AtomicInteger(0);
    // 对战成绩
    private AtomicInteger battleScore = new AtomicInteger(0);

    public GameSession(int gameID, String gameSessionID, GameHandler gameHandler, List<String> members,
                       ClientApi client, GameSessionManager gameSessionManager) {
        this.gameID = gameID;
        this.gameSessionID = gameSessionID;
        this.gameHandler = gameHandler;
        this.members = members;
        this.client = client;
        this.gameSessionManager = gameSessionManager;
    }

    public void setGameSessionID(String gameSessionID) {
        this.gameSessionID = gameSessionID;
    }

    public String getGameSessionID() {
        return gameSessionID;
    }

    public int getGameID() {
        return gameID;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public Object attr(String key) {
        return attributes.get(key);
    }

    public GameSession attr(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public void playerJoin(final Player player) {
        LOG.debug("player {} join game", player.getUser().getId());
        final String userId = player.getUser().getId();
        if(gameHandler != null) gameHandler.onPlayerConnected(userId, this);

        players.put(userId, player);
        //网络掉线或者连接关闭，移除
        player.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                offlineOrClose(future.channel(), userId);
            }
        });

        broadcast(userId, new StatusEvent(StatusCode.ONLINE, userId));
        client.setPlaying(userId, this.gameID);
    }

    /**
     * 玩家连接断开处理
     * 主动提前退出游戏、游戏结束、网络掉线
     * @param player
     */
    public void offlineOrClose(Channel channel, String player) {
        if(gameHandler != null) gameHandler.onPlayerDisconnected(player, ChannelAttrKey.isUserCloseGame(channel) ? 1 : 0, this);
        lockState.remove(player);
        players.remove(player);

        StatusCode statusCode = ChannelAttrKey.isUserCloseGame(channel) ? StatusCode.OVER : StatusCode.OFFLINE;
        broadcast(player, new StatusEvent(statusCode, player));

        if(players.size() < 1) {
            LOG.info("close game session {}", gameSessionID);
            gameSessionManager.removeSession(this);
        }
        client.removePlaying(player, this.gameID);
    }
    /**
     * 申请资源锁
     * @param requestPlayer 发起玩家
     * @param lockEvent 争取的资源锁
     */
    public synchronized void lock(final String requestPlayer, final LockEvent lockEvent) {
        lockEvent.setPlayer(requestPlayer);
        cancelLockTimeout(lockEvent.getCode());
        newLockTimeoutIfNecessary(requestPlayer, lockEvent);

        broadcast(requestPlayer, lockEvent); //向其他玩家发送申请资源锁指令

        lockState.put(requestPlayer, true);

        if(lockState.size() >= members.size()) {    //满足条件
            lockState.clear(); //reset the lock status
            cancelLockTimeout(lockEvent.getCode());

            final GameSession session = this;
            broadcast(null, new LockCompleteEvent(requestPlayer, lockEvent.getCode(), false)).addListener(new ChannelGroupFutureListener() {
                @Override
                public void operationComplete(ChannelGroupFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        LOG.warn("broadcast send lock complete event error", future.cause());
                        return;
                    }

                    LOG.info("lock complete");
                    if (gameHandler != null)
                        gameHandler.onLockComplete(new ResourceLock(lockEvent.getCode()), session);

                }
            });
        }

    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * 创建资源锁的超时定时器
     * @param requestPlayer  发起玩家
     * @param lockEvent    资源锁事件
     */

    private void newLockTimeoutIfNecessary(final String requestPlayer,final LockEvent lockEvent) {
        if(lockEvent.getTimeout() > 0) {
            lockTimeout = timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if(!timeout.isCancelled()) {
                        lockState.remove(requestPlayer);
                        broadcast(null, new LockCompleteEvent(requestPlayer, lockEvent.getCode(), true));
                    }
                }
            }, lockEvent.getTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * 取消资源锁的定时器
     * 取消的条件，有新的资源锁指令到达，或者满足了资源锁的条件
     * @param code
     */
    private void cancelLockTimeout(String code) {
        if(lockTimeout != null && !lockTimeout.isCancelled()) {
            LOG.info("cancel lock timeout {}", lockTimeout);
            lockTimeout.cancel();
        }
    }
    /**
     * 广播消息，excludePlayer不广播
     * @param excludePlayer 不接受广播消息的用户
     * @param event 广播消息
     */
    public ChannelGroupFuture broadcast(String excludePlayer, Event event) {

        if(event == null) {
            throw new NullPointerException("event");
        }

        Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(players.size());

        for(String playerName : players.keySet()) {
            if(!playerName.equalsIgnoreCase(excludePlayer)) {
                Player player = players.get(playerName);
                if(player != null) {
                    futures.put(player.channel(), player.writeAndFlush(event));
                }
            }
        }

        return new DefaultGameSessionFuture(futures, GlobalEventExecutor.INSTANCE);


    }

    /**
     *
     * @param player
     */
    public void end(String player) {

    }

    public List<String> onlineMembers() {
        List<String> result = new ArrayList<String>();
        for(Player player : players.values()) {
            result.add(player.getUser().getId());
        }
        return result.size() < 1 ? null : result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("players=").append(players).append(",members=").append(members).append(",lock_state=").append(lockState);
        return sb.toString();
    }

    public Player getPlayer(String playerName) {
        return players.get(playerName);
    }

    public GameHandler handler() {
        return gameHandler;
    }

    public Collection<Player> players() {
        return Collections.unmodifiableCollection(players.values());
    }

    public void clear() {
        lockState.clear();
        gameHandler.clear(this);
        if(lockTimeout != null && !lockTimeout.isCancelled()) lockTimeout.cancel();

    }

    public void addPlayerReadyNum() {
        playerReadyNum.getAndIncrement();
    }

    public int getPlayerReadyNum() {
        return playerReadyNum.get();
    }

    public void addBattleScoreNum(Integer score) {
        battleScoreNum.getAndIncrement();
        battleScore.getAndAdd(score);
    }

    public int getBattleScoreNum() {
        return battleScoreNum.get();
    }

    public void resetBattleScoreNum() {
        battleScoreNum = new AtomicInteger(0);
        battleScore = new AtomicInteger(0);
    }

    public int getBattleScore() {
        return battleScore.get();
    }
}
