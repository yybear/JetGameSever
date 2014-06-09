package com.handwin.game;

import com.google.common.collect.Maps;
import com.handwin.entity.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * User: roger
 * Date: 13-12-13 上午11:30
 */
public class Player {
    private static Logger LOG = LoggerFactory.getLogger(Player.class);

    private Channel channel;

    private User user;

    private int appId;

    protected Map<String, Object> attributes = Maps.newHashMap();

    public Player(User user) {
        this.user = user;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public void setTcpSender(Channel channel) {
        this.channel = channel;

    }

    public Channel channel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key,Object value) {
        attributes.put(key, value);
    }

    public Map<String,Object> getAttributes() {
        return attributes;
    }


    public ChannelFuture writeAndFlush(Object object) {
        return channel.writeAndFlush(object).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!future.isSuccess())  {
                    LOG.error("send data to {} error", user.getId());
                }
            }
        });
    }

    private class State {
        //1-掉线 2-提前退出 3-游戏结束
        public int state;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("name=").append(user.getId()).append(",attributes=").append(attributes).append(",channel=").append(channel);
        return builder.toString();
    }

    public void clearAttribute() {
        attributes.clear();
    }
}


