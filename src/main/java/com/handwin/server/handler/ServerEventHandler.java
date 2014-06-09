package com.handwin.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handwin.event.*;
import com.handwin.game.GameSession;
import com.handwin.game.GameSessionManager;
import com.handwin.util.Jackson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.handwin.game.ChannelAttrKey.*;

/**
 * User: roger
 * Date: 13-12-13 上午11:24
 */
public class ServerEventHandler extends SimpleChannelInboundHandler<Packet> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerEventHandler.class);
    private static final ObjectMapper mapper = Jackson.mapper();

    private GameSessionManager gameSessionManager;

    private EventHandlerFactory eventHandlerFactory;

    public void setGameSessionManager(GameSessionManager gameSessionManager) {
        this.gameSessionManager = gameSessionManager;
    }

    public void setEventHandlerFactory(EventHandlerFactory eventHandlerFactory) {
        this.eventHandlerFactory = eventHandlerFactory;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent)evt;
            if(e.state() == IdleState.READER_IDLE) {
                LOG.info("idle timeout for channel {}", ctx.channel());
                ctx.close();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {

        Channel channel = ctx.channel();

        if(Events.CONNECT_PACKET_TYPE == packet.getPacketType()) { //支持连接指令{0x03:心跳,0x05:LOGOUT}
            handleConnectPacket(channel, packet);
            return;
        }

        JsonNode node = mapper.readTree(packet.getData());
        int type = node.get("type").asInt();
        LOG.debug("event type is {}", type);

        EventHandler handler = eventHandlerFactory.getEventHandler(type);
        if(null != handler) {
            handler.onEvent(node, channel);
            return;
        }

        String playerName = channel.attr(PLAYERNAME_ATTR_KEY).get();
        String gameSessionID = channel.attr(GAMESESSION_ID_ATTR_KEY).get();
        if(playerName == null || gameSessionID == null) { //未登录
            closeChannelWithLoginFailure(channel,"请先登录再发送其他指令");
            return;
        }

        GameSession gameSession = gameSessionManager.lookupSession(gameSessionID); //游戏会话没了?
        if(gameSession == null) {
            closeChannelWithLoginFailure(channel,"没有配对的游戏会话");
            return;
        }

        switch (type) {
            case Events.GAME_RESET:
                gameSession.clear();
                gameSession.broadcast(null, new StatusEvent(StatusCode.RESET, playerName));
                break;
            case Events.RES_LOCK:
                gameSession.lock(playerName, mapper.readValue(node.traverse(), LockEvent.class));
                break;
            case Events.GAME_END:
                channel.attr(GAME_CLOSESTATE_ATTR_KEY).set(1);   //游戏结束或者提前退出游戏,提前退出游戏需要客户端自己判断
                gameSession.clear();
                gameSession.broadcast(playerName, new StatusEvent(StatusCode.OVER, playerName));
                break;
            default:
                gameSession.handler().onReceiveMessage(playerName, node);
                break;

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOG.warn(cause.getMessage(), cause);
        if(cause instanceof IOException && "Connection reset by peer".equals(cause.getMessage())) {
            ctx.channel().close();
        } else {
            ctx.channel().writeAndFlush(new ServerErrRespEvent(cause.getMessage()));
        }
    }

    protected void handleConnectPacket(Channel channel,Packet packet) {

        byte cmd = packet.getCmd();

        switch (cmd) {
            case Events.PING_COMMAND:  //心跳指令
                pong(channel);
                break;
            case Events.LOGOUT_COMMAND:  //断开连接指令
                channel.attr(GAME_CLOSESTATE_ATTR_KEY).set(1);
                channel.attr(GAMESESSION_ID_ATTR_KEY).remove();
                break;
            default:
                LOG.info("unknown command {} for the connect packet",cmd);
        }
    }

    private void pong(Channel channel ) {
        ByteBuf out = channel.alloc().buffer();
        out.writeByte(Packet.START);
        out.writeByte(1);
        out.writeByte(Events.CONNECT_PACKET_TYPE);
        out.writeByte(0);
        out.writeLong(System.currentTimeMillis());
        out.writeShort(1);
        out.writeByte(0);
        out.writeByte(Packet.END);
        out.writeByte(Events.PONG_COMMAND);
        channel.writeAndFlush(out);
    }

    private void closeChannelWithLoginFailure(Channel channel, String desc) {
        LOG.info("close channel {} for reason:{}",desc);
        ChannelFuture future = channel.writeAndFlush(new JoinRespEvent(Events.ACTION_FAILED, null, desc));
        future.addListener(ChannelFutureListener.CLOSE);
    }
}
