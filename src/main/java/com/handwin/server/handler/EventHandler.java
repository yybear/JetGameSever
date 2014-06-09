package com.handwin.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.handwin.event.Events;
import com.handwin.event.JoinRespEvent;
import com.handwin.server.ClientApi;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-6 上午9:10
 */
public abstract class EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

    public EventHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    private ClientApi clientApi;

    public abstract void onEvent(JsonNode node, Channel channel) throws Exception;

    public void closeChannelWithLoginFailure(Channel channel, String desc) {
        LOG.info("close channel {} for reason:{}",desc);

        ChannelFuture future = channel.writeAndFlush(new JoinRespEvent(Events.ACTION_FAILED, null, desc));
        future.addListener(ChannelFutureListener.CLOSE);
    }

    public String getMe(String sessionId) {
        return clientApi.getUserMd5(sessionId);  //根据玩家session.id获取玩家name md5
    }

    /**
     * 验证sessionId
     * @param sessionId
     * @param appId
     */
    public boolean auth(String sessionId, int appId, Channel channel) {
        String md5 = getMe(sessionId);  // TODO: 这里直接从redis中获取，其实应该走提供的接口走
        if(StringUtils.isBlank(md5)) {
            // session id已经失效
            LOG.info("session {} is expired", sessionId);
            closeChannelWithLoginFailure(channel, "sessionId:"+ sessionId +"是无效的");
            return false;
        }
        return true;
    }
}
