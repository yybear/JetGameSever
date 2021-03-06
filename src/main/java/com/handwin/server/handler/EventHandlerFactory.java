package com.handwin.server.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.handwin.db.Cassandra;
import com.handwin.db.Jdbc;
import com.handwin.entity.GamePlayer;
import com.handwin.entity.User;
import com.handwin.entity.UserScore;
import com.handwin.event.*;
import com.handwin.game.*;
import com.handwin.game.rhythm.event.BattleScoreRespEvent;
import com.handwin.server.ClientApi;
import com.handwin.util.Constants;
import com.handwin.util.HttpRequestUtils;
import com.handwin.util.Jackson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.httpclient.Header;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.handwin.game.ChannelAttrKey.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-6 上午9:10
 */
public class EventHandlerFactory implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(EventHandlerFactory.class);

    @Value("${core.server}")
    protected String coreServer;

    @Autowired
    protected PlayerManager playerManager;

    @Autowired
    protected GameSessionManager gameSessionManager;

    @Autowired
    protected Jdbc jdbc;

    @Autowired
    protected RandomMatchTask matchTask;

    @Autowired
    protected Cassandra cassandra;

    @Autowired
    protected ClientApi clientApi;

    @Value("${max.wait.time}")
    protected int maxWaitTime;

    protected Map<Integer, EventHandler> EVENT_HANDLER_MAP = Maps.newHashMap();

    protected EventHandler loginGameHandler() {
        return new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception{

                // TODO: 用户重复登陆?
                final String sessionId = node.get("session_id").asText();
                final int appId = node.get("app_id").asInt();
                final Header header = new Header("client-session", sessionId);
                LOG.debug("loginAction session id is {}", sessionId);

                Map<String, Integer> params = Maps.newHashMap();
                params.put("app_id", appId);
                String response = HttpRequestUtils.doGet(coreServer + "/api/user/auth", params, new Header[]{header});
                if(StringUtils.isBlank(response) || response.indexOf("error_code") > 0) {
                    // 验证失败
                    LOG.info("login core server failed");
                    ChannelFuture future = channel.writeAndFlush(new LoginGameRespEvent(Events.ACTION_FAILED, null));
                    future.addListener(ChannelFutureListener.CLOSE);
                } else {
                    final User user = Jackson.fromJson(response, User.class);
                    // TODO: 这个后面去除，全存放在cassandra里面
                    int count = jdbc.count("select count(*) c from player_game_info where id=? and game_id=?", user.getId(), appId);
                    if(0 == count) {  // 第一次登陆保存玩家游戏信息
                        jdbc.update("insert into player_game_info (id, game_id, score, num) values (?,?,?,?)",
                                user.getId(), appId, 0, 0);
                    }
                    // 进入游戏更新游戏在线人数
                    cassandra.updateGameOnlineNum(appId, true);
                    GamePlayer gamePlayer = cassandra.initPlayer(user.getId(), appId);

                    user.setExperience(gamePlayer.getExperience());
                    user.setLevet(gamePlayer.getLevel());
                    user.setStars(gamePlayer.getStars());
                    user.setThreeStarNum(gamePlayer.getThreeStarNum());

                    ChannelFuture future = channel.writeAndFlush(new LoginGameRespEvent(Events.ACTION_SUCCESS, user));
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if(future.isSuccess()) {

                                Channel channel = future.channel();
                                Player player = new Player(user);
                                channel.attr(PLAYERNAME_ATTR_KEY).set(user.getId());
                                channel.attr(PLAYERSESSION_ATTR_KEY).set(sessionId);
                                channel.attr(APPID_ATTR_KEY).set(appId);
                                player.setTcpSender(channel);
                                player.setAppId(appId);
                                playerManager.addPlayer(player);
                            }
                        }
                    });

                    // channel关闭时更新游戏在线人数
                    channel.closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            cassandra.updateGameOnlineNum(appId, false);
                        }
                    });
                }
            }
        };
    }

    protected EventHandler getFriendsHandler() {
        return new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                LOG.debug("getFriends");
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                Header header = new Header("client-session", sessionId);
                Map<String, Integer> params = Maps.newHashMap();
                params.put("app_id", Constants.DUDU_APP_ID);
                String response = HttpRequestUtils.doGet(coreServer + "/api/contacts", params, new Header[]{header});
                if(StringUtils.isBlank(response) || response.indexOf("person") < 0) {
                    // 没有找到
                    channel.writeAndFlush(new GetFriendsRespEvent(Events.ACTION_FAILED, null));
                } else {
                    LOG.debug("/api/contacts response is {}", response);
                    JsonNode rootNode = Jackson.readTree(response);
                    // 过滤掉没有安装游戏的好友
                    List<User> friends = Jackson.fromJson(rootNode.get("person"), new TypeReference<List<User>>() {});
                    List<User> players = Lists.newArrayList();
                    for(User user : friends) {
                        int count = jdbc.count("select count(*) from player_game_info where id=? and game_id=?", user.getId(), appId);
                        if(count > 0) {
                            Player player = playerManager.get(user.getId());
                            if(null != player) {
                                String gameSessionId = player.channel().attr(GAMESESSION_ID_ATTR_KEY).get();
                                LOG.debug("get friends, friend {}' game_session_id is {}", user.getId(), gameSessionId);
                                if(gameSessionId != null) {
                                    // 在game session中，表明正在游戏
                                    user.setGameStatus(Constants.BUSY);
                                } else {
                                    user.setGameStatus(Constants.ONLINE);
                                }
                            } else {
                                user.setGameStatus(Constants.OFFLINE);
                            }
                            players.add(user);
                        }
                    }

                    channel.writeAndFlush(new GetFriendsRespEvent(Events.ACTION_SUCCESS, players));
                }
            }
        };
    }

    protected EventHandler readyHandler() {
        return new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                String me = getMe(sessionId);

                String gameSessionId = channel.attr(GAMESESSION_ID_ATTR_KEY).get();
                GameSession gameSession = gameSessionManager.lookupSession(gameSessionId);
                if(gameSession == null) {
                    closeChannelWithLoginFailure(channel,"没有配对的游戏会话");
                    return;
                }

                gameSession.addPlayerReadyNum(); // 准备就绪的玩家计数

                List<String> players = gameSession.getMembers();
                for(String playerName : players) {
                    if(playerName.equals(me)) continue;

                    Player player = playerManager.get(playerName);
                    if(null == player) continue;

                    player.channel().writeAndFlush(new UserReadyRespEvent(Events.ACTION_SUCCESS, me));
                }

                if(players.size() == gameSession.getPlayerReadyNum()) {
                    // 都准备好了
                    Event event = new Event();
                    event.setType(Events.GAME_START);
                    for(String playerName : players) {
                        Player player = playerManager.get(playerName);
                        if(null == player) continue;

                        player.channel().writeAndFlush(event);
                    }
                }
            }
        };
    }

    protected EventHandler battleScoreHandler() {
        return new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                Integer score = node.get("player_score").asInt(); // 玩家个人得分
                Integer noteScore = node.get("note_score").asInt(); // 音符总分

                String gameSessionId = channel.attr(GAMESESSION_ID_ATTR_KEY).get();
                GameSession gameSession = gameSessionManager.lookupSession(gameSessionId);
                if(gameSession == null) {
                    closeChannelWithLoginFailure(channel,"没有配对的游戏会话");
                    return;
                }

                gameSession.addBattleScoreNum(score);
                List<String> players = gameSession.getMembers();
                Integer battleScore;
                if(players.size() == gameSession.getBattleScoreNum()) {
                    // 都准备取双人成绩
                    LOG.debug("all player is ready, send battle score");
                    int sumPlayerScore = gameSession.getBattleScore();  // 2个人的成绩和
                    LOG.debug("tow players score is {}", sumPlayerScore);
                    String player1 = players.get(0);
                    String player2 = players.get(1);

                    // 取玩家的等级
                    GamePlayer gamePlayer1 = cassandra.getPlayer(player1, appId);
                    GamePlayer gamePlayer2 = cassandra.getPlayer(player2, appId);

                    // 等级加成
                    Integer player1Addition = cassandra.getGameSongLevel(gamePlayer1.getLevel()).getAddition();
                    Integer player2Addition = cassandra.getGameSongLevel(gamePlayer2.getLevel()).getAddition();

                    Float fNoteScore = new Float(noteScore);
                    Float fPlayer1Addition = new Float(player1Addition)/100;
                    Float fPlayer2Addition = new Float(player2Addition)/100;

                    // 计算双人得分
                    battleScore = Math.round(fNoteScore * (1 + fPlayer1Addition + fPlayer2Addition)) + gameSession.getBattleScore();

                    Player player1User = playerManager.get(player1);
                    Player player2User = playerManager.get(player2);

                    player1User.channel().writeAndFlush(new BattleScoreRespEvent(battleScore, Events.ACTION_SUCCESS));
                    player2User.channel().writeAndFlush(new BattleScoreRespEvent(battleScore, Events.ACTION_SUCCESS));
                }
                // TODO: 超时没有处理
            }
        };
    }

    protected EventHandler joinRamdomQueueHandler() {
        return new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                matchTask.joinQueue(node, channel);
            }
        };
    }

    protected void initEventHandlerMap() {
        EVENT_HANDLER_MAP.put(Events.LOGIN_GAME, loginGameHandler());

        EVENT_HANDLER_MAP.put(Events.LOGOUT_GAME, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                // 关闭channel
                channel.close();
            }
        });

        EVENT_HANDLER_MAP.put(Events.GET_FRIENDS, getFriendsHandler());

        EVENT_HANDLER_MAP.put(Events.BATTLE_SCORE, battleScoreHandler());

        EVENT_HANDLER_MAP.put(Events.ADD_FRIENDS, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                LOG.debug("players node is {}", node.get("players"));
                String[] players = Jackson.fromJson(node.get("players"), new TypeReference<String[]>() {});
                String playersStr = StringUtils.join(players, ",");

                LOG.debug("addFriends players is {}", playersStr);
                Map<String, String> params = Maps.newHashMap();
                params.put("src_app_id", appId + "");
                params.put("dst_app_id", Constants.DUDU_APP_ID + "");
                params.put("uids", playersStr);
                Header header = new Header("client-session", sessionId);
                String response = HttpRequestUtils.doPost(coreServer + "/api/contact/addByUid", params, new Header[]{header});
                LOG.debug("/api/contact/addByUid response is {}", response);
                if(StringUtils.isBlank(response)) {
                    channel.writeAndFlush(new AddFriendsRespEvent(Events.ACTION_FAILED));
                } else {
                    JsonNode rootNode = Jackson.readTree(response);
                    if(rootNode.get("error_code").asInt() == Constants.CORE_SERVER_OK) {
                        channel.writeAndFlush(new AddFriendsRespEvent(Events.ACTION_SUCCESS));
                    } else {
                        channel.writeAndFlush(new AddFriendsRespEvent(Events.ACTION_FAILED));
                    }
                }
            }
        });

        EVENT_HANDLER_MAP.put(Events.INVITE_PLAYER, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, final Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                LOG.debug("invite request to players {} ", node.get("player"));
                String[] players = Jackson.fromJson(node.get("player"), new TypeReference<String[]>() {});

                // 给邀请的人发送
                final String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                final String playerName = players[0];

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long begin = System.currentTimeMillis();
                        LOG.debug("invite player {} on {}, wait!", playerName, begin);
                        do {
                            Player self = playerManager.get(me);
                            Player player = playerManager.get(playerName);
                            if(self == null) {
                                // 自己退出了
                                break;
                            }
                            if(player == null) {
                                // 玩家不在线 而且没有过超时时间
                                LOG.debug("player {} is not online on {}, wait!", playerName, System.currentTimeMillis());
                                if((System.currentTimeMillis() - begin) >= maxWaitTime) {
                                    LOG.debug("time out, send failed msg to inviter!");
                                    channel.writeAndFlush(new InviteRespEvent(Events.ACTION_FAILED, playerName));
                                    break;
                                } else {
                                    LOG.debug("sleep for 2 min!");
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            } else { // 给受邀者发送消息
                                LOG.debug("player {} is online on {}, send invite!", playerName, System.currentTimeMillis());
                                ReplyInviteReqEvent event = new ReplyInviteReqEvent(me);
                                player.channel().writeAndFlush(event);
                                break;
                            }
                        } while (true);
                    }
                }).start();
            }
        });


        EVENT_HANDLER_MAP.put(Events.REPLY_INVITE, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                int code = node.get("code").asInt();
                String invater = node.get("player").asText();    // 邀请人
                InviteRespEvent event;
                String player = channel.attr(PLAYERNAME_ATTR_KEY).get(); // 被邀请人
                if(code == Events.ACTION_SUCCESS) {  // 对方接受
                    LOG.debug("{} accept {} invite to play game", player, invater);
                    // TODO: 目前这样做 只能支持2个人一起玩游戏
                    event = new InviteRespEvent(code, player);

                    // 直接创建game session
                    gameSessionManager.createSession(playerManager.get(invater),
                            playerManager.get(channel.attr(PLAYERNAME_ATTR_KEY).get()));

                } else {
                    LOG.debug("{} reject {} invite, code is {}", channel.attr(PLAYERNAME_ATTR_KEY).get(), invater, code);
                    event = new InviteRespEvent(code, player);
                }

                Player invaterPlayer = playerManager.get(invater);
                invaterPlayer.channel().writeAndFlush(event);
            }
        });

        EVENT_HANDLER_MAP.put(Events.SAVE_SEX, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                int sex = node.get("sex").asInt();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                Header header = new Header("client-session", sessionId);
                Map<String, String> params = Maps.newHashMap();
                params.put("sex", sex + "");
                String response = HttpRequestUtils.doPost(coreServer + "/api/user/upload", params, new Header[]{header});
                LOG.debug("/api/user/upload response is {}", response);

                String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                playerManager.get(me).getUser().setSex(sex);
            }
        });

        EVENT_HANDLER_MAP.put(Events.SAVE_GAME_INFO, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                LOG.debug("saveGameInfo");
                String sessionId = node.get("session_id").asText();
                int appId = node.get("app_id").asInt();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                int score = node.get("score").asInt();
                String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                jdbc.update("update player_game_info set score=?, num=num+1 where id=? and game_id=?", score, me, appId);
            }
        });

        EVENT_HANDLER_MAP.put(Events.PLAYER_READY, readyHandler());

        EVENT_HANDLER_MAP.put(Events.SCORE_LIST, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                int count = 10;
                if(node.has("count"))
                    count = node.get("count").asInt();

                List queryList = jdbc.list("select id, score from player_game_info where game_id=? order by score desc limit ?", appId, count);
                List<UserScore> res = Lists.newArrayList();
                for(int i = 0; i < queryList.size(); i++) {
                    Map<String, Object> map = (Map<String, Object>)queryList.get(i);

                    String userid = (String)map.get("id");
                    Integer score = (Integer)map.get("score");
                    LOG.debug("player_game_info user id: {}, score: {}", userid, score);

                    Header header = new Header("client-session", sessionId);
                    Map<String, String> params = Maps.newHashMap();
                    params.put("id", userid);
                    String response = HttpRequestUtils.doGet(coreServer + "/api/user", params, new Header[]{header});

                    if(StringUtils.isBlank(response) || response.indexOf("error_code") > 0) {
                        // 异常
                        LOG.debug("get user error, response is {}", response);
                    } else {
                        User user = null;
                        try {
                            user = Jackson.fromJson(response, User.class);
                            Player player = playerManager.get(user.getId());
                            if(null != player) {
                                String gameSessionId = player.channel().attr(GAMESESSION_ID_ATTR_KEY).get();
                                if(gameSessionId != null) {
                                    // 在game session中，表明正在游戏
                                    user.setGameStatus(Constants.BUSY);
                                } else {
                                    user.setGameStatus(Constants.ONLINE);
                                }
                            } else {
                                user.setGameStatus(Constants.OFFLINE);
                            }
                            res.add(new UserScore(score, user));
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }

                channel.writeAndFlush(new GetScoreListRespEvent(res));
            }
        });

        EVENT_HANDLER_MAP.put(Events.JOIN_WAIT_QUEUE, joinRamdomQueueHandler());

        EVENT_HANDLER_MAP.put(Events.SEND_PUSH_MSG, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                String player = node.get("player").asText();
                String msg = node.get("msg").asText();
                LOG.debug("get push msg {} {}", player, msg);

                Player recv = playerManager.get(player);
                if(recv != null) {
                    recv.channel().writeAndFlush(new SendPushMsgRespEvent(player, msg));
                }
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initEventHandlerMap();
    }

    public EventHandler getEventHandler(int eventType) {
        return EVENT_HANDLER_MAP.get(eventType);
    }
}
