package com.handwin.server.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.handwin.database.DBManager;
import com.handwin.game.app.GameHandler;
import com.handwin.game.app.GameSession;
import com.handwin.game.app.GameSessionManager;
import com.handwin.game.app.Player;
import com.handwin.game.entity.User;
import com.handwin.game.entity.UserScore;
import com.handwin.game.server.PlayerManager;
import com.handwin.game.server.RandomMatchTask;
import com.handwin.game.server.ServerConfiguration;
import com.handwin.jackson.Jackson;
import com.handwin.util.Constants;
import com.handwin.util.HttpRequestUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.httpclient.Header;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.handwin.game.app.ChannelAttrKey.*;
import static com.handwin.game.app.ChannelAttrKey.APPID_ATTR_KEY;
import static com.handwin.game.app.ChannelAttrKey.GAMESESSION_ID_ATTR_KEY;
import static com.handwin.game.app.ChannelAttrKey.PLAYERNAME_ATTR_KEY;
import static com.handwin.game.app.ChannelAttrKey.PLAYERSESSION_ATTR_KEY;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-6 上午9:10
 */
public class EventHandlerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(EventHandlerFactory.class);

    private static final Map<Integer, EventHandler> EVENT_HANDLER_MAP = Maps.newHashMap();

    static {
        EVENT_HANDLER_MAP.put(Events.LOGIN_GAME, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception{
                // TODO: 用户重复登陆?
                final String sessionId = node.get("session_id").asText();
                final int appId = node.get("app_id").asInt();
                Header header = new Header("client-session", sessionId);

                LOG.debug("loginAction session id is {}", sessionId);

                Map<String, Integer> params = Maps.newHashMap();
                params.put("app_id", appId);
                String response = HttpRequestUtils.doGet(ServerConfiguration.INSTANCE.getCoreServer() + "/api/user/auth", params, new Header[]{header});
                if(StringUtils.isBlank(response) || response.indexOf("error_code") > 0) {
                    // 验证失败
                    LOG.info("login core server failed");
                    ChannelFuture future = channel.writeAndFlush(new LoginGameRespEvent(Events.ACTION_FAILED, null));
                    future.addListener(ChannelFutureListener.CLOSE);
                } else {
                    final User user = Jackson.fromJson(response, User.class);
                    int count = DBManager.count("select count(*) c from player_game_info where id=? and game_id=?", user.getId(), appId);
                    if(0 == count) {  // 第一次登陆保存玩家游戏信息
                        DBManager.update("insert into player_game_info (id, game_id, score, num) values (?,?,?,?)",
                                user.getId(), appId, 0, 0);
                    }
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
                                PlayerManager.INSTANCE.addPlayer(player);
                            }
                        }
                    });
                }
            }
        });

        EVENT_HANDLER_MAP.put(Events.LOGOUT_GAME, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                // 关闭channel
                channel.close();
            }
        });

        EVENT_HANDLER_MAP.put(Events.GET_FRIENDS, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                LOG.debug("getFriends");
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                Header header = new Header("client-session", sessionId);
                Map<String, Integer> params = Maps.newHashMap();
                params.put("app_id", Constants.DUDU_APP_ID);
                String response = HttpRequestUtils.doGet(ServerConfiguration.INSTANCE.getCoreServer() + "/api/contacts", params, new Header[]{header});
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
                        int count = DBManager.count("select count(*) from player_game_info where id=? and game_id=?", user.getId(), appId);
                        if(count > 0) {
                            Player player = PlayerManager.INSTANCE.get(user.getId());
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
        });

        EVENT_HANDLER_MAP.put(Events.ADD_FRIENDS, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
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
                String response = HttpRequestUtils.doPost(ServerConfiguration.INSTANCE.getCoreServer() + "/api/contact/addByUid", params, new Header[]{header});
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

        EVENT_HANDLER_MAP.put(Events.INVITE_PLAYER, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                LOG.debug("invite request to players {} ", node.get("player"));
                String[] players = Jackson.fromJson(node.get("player"), new TypeReference<String[]>() {});

                // 给邀请的人发送
                String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                for(String playerName : players) {
                    LOG.debug("send invite request to player {} to play game", playerName);
                    Player player = PlayerManager.INSTANCE.get(playerName);

                    if(player == null) { // 用户已经下线
                        LOG.debug("player {} has already offline, invite failed", playerName);
                        channel.writeAndFlush(new InviteRespEvent(Events.ACTION_FAILED, playerName));
                    } else { // 给受邀者发送消息
                        ReplyInviteReqEvent event = new ReplyInviteReqEvent(me);
                        player.channel().writeAndFlush(event);
                    }
                }
            }
        });


        EVENT_HANDLER_MAP.put(Events.REPLY_INVITE, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
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
                    GameSessionManager.instance.createSession(PlayerManager.INSTANCE.get(invater),
                            PlayerManager.INSTANCE.get(channel.attr(PLAYERNAME_ATTR_KEY).get()),
                            handlerClazz);

                } else {
                    LOG.debug("{} reject {} invite, code is {}", channel.attr(PLAYERNAME_ATTR_KEY).get(), invater, code);
                    event = new InviteRespEvent(code, player);
                }

                Player invaterPlayer = PlayerManager.INSTANCE.get(invater);
                invaterPlayer.channel().writeAndFlush(event);
            }
        });

        EVENT_HANDLER_MAP.put(Events.SAVE_SEX, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                int sex = node.get("sex").asInt();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                Header header = new Header("client-session", sessionId);
                Map<String, String> params = Maps.newHashMap();
                params.put("sex", sex + "");
                String response = HttpRequestUtils.doPost(ServerConfiguration.INSTANCE.getCoreServer() + "/api/user/upload", params, new Header[]{header});
                LOG.debug("/api/user/upload response is {}", response);

                String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                PlayerManager.INSTANCE.get(me).getUser().setSex(sex);
            }
        });

        EVENT_HANDLER_MAP.put(Events.SAVE_GAME_INFO, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                LOG.debug("saveGameInfo");
                String sessionId = node.get("session_id").asText();
                int appId = node.get("app_id").asInt();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                int score = node.get("score").asInt();
                String me = channel.attr(PLAYERNAME_ATTR_KEY).get();
                DBManager.update("update player_game_info set score=?, num=num+1 where id=? and game_id=?", score, me, appId);
            }
        });

        EVENT_HANDLER_MAP.put(Events.PLAYER_READY, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                String me = getMe(sessionId);

                String gameSessionId = channel.attr(GAMESESSION_ID_ATTR_KEY).get();
                GameSession gameSession = GameSessionManager.instance.lookupSession(gameSessionId);
                if(gameSession == null) {
                    closeChannelWithLoginFailure(channel,"没有配对的游戏会话");
                    return;
                }

                gameSession.addPlayerReadyNum(); // 准备就绪的玩家计数

                List<String> players = gameSession.getMembers();
                for(String playerName : players) {
                    if(playerName.equals(me)) continue;

                    Player player = PlayerManager.INSTANCE.get(playerName);
                    if(null == player) continue;

                    player.channel().writeAndFlush(new UserReadyRespEvent(Events.ACTION_SUCCESS, me));
                }

                if(players.size() == gameSession.getPlayerReadyNum()) {
                    // 都准备好了
                    Event event = new Event();
                    event.setType(Events.GAME_START);
                    for(String playerName : players) {
                        Player player = PlayerManager.INSTANCE.get(playerName);
                        if(null == player) continue;

                        player.channel().writeAndFlush(event);
                    }
                }
            }
        });

        EVENT_HANDLER_MAP.put(Events.SCORE_LIST, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }
                int count = 10;
                if(node.has("count"))
                    count = node.get("count").asInt();

                List queryList = DBManager.list("select id, score from player_game_info where game_id=? order by score desc limit ?", appId, count);
                List<UserScore> res = Lists.newArrayList();
                for(int i = 0; i < queryList.size(); i++) {
                    Map<String, Object> map = (Map<String, Object>)queryList.get(i);

                    String userid = (String)map.get("id");
                    Integer score = (Integer)map.get("score");
                    LOG.debug("player_game_info user id: {}, score: {}", userid, score);

                    Header header = new Header("client-session", sessionId);
                    Map<String, String> params = Maps.newHashMap();
                    params.put("id", userid);
                    String response = HttpRequestUtils.doGet(ServerConfiguration.INSTANCE.getCoreServer() + "/api/user", params, new Header[]{header});

                    if(StringUtils.isBlank(response) || response.indexOf("error_code") > 0) {
                        // 异常
                        LOG.debug("get user error, response is {}", response);
                    } else {
                        User user = null;
                        try {
                            user = Jackson.fromJson(response, User.class);
                            Player player = PlayerManager.INSTANCE.get(user.getId());
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

        EVENT_HANDLER_MAP.put(Events.JOIN_WAIT_QUEUE, new EventHandler() {
            @Override
            public void onEvent(JsonNode node, Channel channel, Class<? extends GameHandler> handlerClazz, RandomMatchTask matchTask) throws Exception {
                String sessionId = channel.attr(PLAYERSESSION_ATTR_KEY).get();
                int appId = channel.attr(APPID_ATTR_KEY).get();
                if (!auth(sessionId, appId, channel)) {
                    return;
                }

                matchTask.joinQueue(node, channel);
            }
        });
    }

    public static EventHandler getEventHandler(int eventType) {
        return EVENT_HANDLER_MAP.get(eventType);
    }
}