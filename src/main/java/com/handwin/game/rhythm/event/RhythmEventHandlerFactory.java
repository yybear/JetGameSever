package com.handwin.game.rhythm.event;

import com.handwin.server.handler.EventHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 下午4:39
 */
public class RhythmEventHandlerFactory extends EventHandlerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(RhythmEventHandlerFactory.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        /*EVENT_HANDLER_MAP.put(Events.LOGIN_GAME, new EventHandler(clientApi) {
            @Override
            public void onEvent(JsonNode node, Channel channel) throws Exception{
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
        });*/
    }
}
