package com.handwin;

import com.handwin.entity.User;
import com.handwin.event.*;

import com.handwin.server.handler.Packet;
import com.handwin.server.handler.ProtocolDecoder;
import com.handwin.server.handler.ProtocolEncoder;
import com.handwin.util.Jackson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.ScheduledFuture;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

// 18d15c5650d74c599f7248ba2f6091fd 13914785569
// 3fb97892bc3f468490b67938328b58bf 13914785567
// 94b4b1e4684b45b0b08d8e0fb0079f82 139147623654
// 188979c5149c47b49969271795b383cd 3f9ace00e08c11e3861869f51b0e7dca 13214532654
// 0487a30e4db042a0816c88f0e9da12ed 7183f670e0ab11e3b91369f51b0e7dca 13214236145
/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-19 下午6:40
 */
class ClientHandler extends SimpleChannelInboundHandler<Packet> {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private static final ObjectMapper mapper = Jackson.newObjectMapper();

    private String sessionId;
    private int testType;

    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    void setTestType(int testType) {
        this.testType = testType;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LoginGameReqEvent loginGameReqEvent = new LoginGameReqEvent(sessionId, 2000);
        ctx.writeAndFlush(loginGameReqEvent);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             Packet packet) throws Exception {

        if(Events.CONNECT_PACKET_TYPE == packet.getPacketType()) {
            byte cmd = packet.getCmd();
            log.debug("接收：连接指令 {}", cmd);

            if(cmd == Events.PONG_COMMAND) {
                log.debug("接收：心跳指令");
            }
            return;
        }

        JsonNode node = mapper.readTree(packet.getData());

        int type = node.get("type").asInt();
        log.debug("event type is {}", type);

        if(Events.LOGIN_GAME == type) { // 玩家登陆游戏
            if(node.get("code").asInt() == 2) {
                log.debug("接收：登陆失败");
                return;
            }

            pingServer(ctx.channel());  // 定时发送心跳

            // 登陆后执行操作
            if(testType == Events.SAVE_SEX)
                saveUserSex(node, ctx.channel());
            else if(testType == Events.JOIN_WAIT_QUEUE)
                joinWaitQueue(node, ctx.channel());
            else if(testType == Events.ADD_FRIENDS)
                addFriend(node, ctx.channel());
            else if(testType == Events.GET_FRIENDS)
                getFriend(node, ctx.channel());
            else if(testType == Events.INVITE_PLAYER)
                inviteFriend(node, ctx.channel());
            else if(testType == Events.SAVE_GAME_INFO)
                saveGameInfo(node, ctx.channel());
            else if(testType == Events.SCORE_LIST)
                getScoreList(ctx.channel());
            else if(testType == Events.SEND_PUSH_MSG) {
                sendMsg(node, ctx.channel());
            }
        } else if(Events.JOIN_WAIT_QUEUE == type) {  // 随机匹配结果
            if(node.get("code").asInt() == 2) {
                log.debug("接收：匹配失败");
                return;
            }

            User user = Jackson.readValue(node.get("player"), User.class);
            log.debug("接收：匹配成功 对方: {}", node);

            ready(ctx.channel(), user.getId());
        } else if(Events.GET_FRIENDS == type) {  // 获取好友
            if(node.get("code").asInt() == 2) {
                log.debug("接收：获取好友失败");
                return;
            }
            log.debug("接收：获取好友成功: {}", node.get("friends"));
        } else if (Events.ADD_FRIENDS == type) { // 添加好友
            if(node.get("code").asInt() == 2) {
                log.debug("接收：匹配失败");
            } else {
                log.debug("接收：添加好友成功");
            }
        } else if(Events.INVITE_PLAYER == type) { // 邀请玩家玩游戏
            int code = node.get("code").asInt();
            if(code == 2) {
                log.debug("接收：匹配失败");
            } else if(code == 3) {
                log.debug("接收：玩家拒绝");
            } else {
                log.debug("接收：匹配成功, game session id is {}", node.get("game_session_id"));
            }
        } else if(Events.REPLY_INVITE == type) {
            String player = node.get("player").asText();
            log.debug("接收：玩家 {} 邀请你玩游戏", player);

            reply(ctx.channel(), player);
        } else if(Events.PLAYER_READY == type) {
            String player = node.get("player").asText();
            log.debug("对方玩家{}已经准备好", player);
        } else if(Events.GAME_START == type) {
            log.debug("玩家已经准备好, 游戏开始");
        } else if(Events.SCORE_LIST == type) {
            log.debug("获取到积分排行榜 {}", node.get("user_scores"));
        } else if(Events.FORWARD_PUSH_MSG == type) {
            log.debug("获取到消息 {}", node.get("msg").asText());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        log.warn(cause.getMessage(), cause);
        cause.printStackTrace();
        ctx.close();
    }

    private void getScoreList(Channel channel) {
        log.debug("客户端：获取积分列表");

        channel.writeAndFlush(new GetScoreListReqEvent(10));
    }

    private void ready(Channel channel, String player) {
        Event event = new Event();
        event.setType(Events.PLAYER_READY);
        channel.writeAndFlush(event);
    }

    private void saveGameInfo(JsonNode node, Channel channel) {
        log.debug("客户端：保存game info");
        channel.writeAndFlush(new SaveGameInfoReqEvent(1000));
    }

    private void addFriend(JsonNode node, Channel channel) {
        log.debug("客户端：添加好友");
        channel.writeAndFlush(new AddFriendReqEvent(new String[]{"37193710f90c11e3bdf269f51b0e7dca"}));
    }

    private void getFriend(JsonNode node, Channel channel) {
        log.debug("客户端：获取好友");
        channel.writeAndFlush(new GetFriendReqEvent());
    }

    private void inviteFriend(JsonNode node, Channel channel) {
        log.debug("客户端：邀请好友一起游戏");
        channel.writeAndFlush(new InviteReqEvent(new String[]{"3f9ace00e08c11e3861869f51b0e7dca"}));
    }

    private void reply(Channel channel, String player) {
        log.debug("客户端：回复 {} 游戏邀请", player);
        channel.writeAndFlush(new ReplyInviteRespEvent(player, Events.ACTION_SUCCESS));
    }

    private void pingServer(final Channel channel) {
        ScheduledFuture<?> future = channel.eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ByteBuf out = Unpooled.buffer();
                out.writeByte(Packet.START);
                out.writeByte(1);
                out.writeByte(Events.CONNECT_PACKET_TYPE);
                out.writeByte(0);
                out.writeLong(System.currentTimeMillis());
                out.writeShort(1);
                out.writeByte(0);
                out.writeByte(Packet.END);
                out.writeByte(Events.PING_COMMAND);

                log.debug("客户端：发送心跳");
                channel.writeAndFlush(out);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 设置性别
     * @param node
     * @param channel
     */
    private void saveUserSex(JsonNode node, Channel channel) {
        User user = Jackson.readValue(node.get("user"), User.class);
        log.debug("user sex is {}", user.getSex());

        channel.writeAndFlush(new SetSexReqEvent(1));
    }

    private void joinWaitQueue(JsonNode node, Channel channel) {
        User user = Jackson.readValue(node.get("user"), User.class);
        log.debug("user sex is {}", user.getSex());

        log.debug("send JOIN_WAIT_QUEUE time is {}", System.currentTimeMillis());
        channel.writeAndFlush(new WaitReqEvent(new int[]{1,3,4}));
    }

    private void sendMsg(JsonNode node, Channel channel) {
        log.debug("send msg");
        channel.writeAndFlush(new SendMsgReqEvent("111", "c6cc2ad0f8f211e385e869f51b0e7dca" ));
    }
}

public class ClientTest {

    private String sessionId;
    private int testEvent;

    public ClientTest(String sessionId, int testEvent) {
        this.sessionId = sessionId;
        this.testEvent = testEvent;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        final ClientHandler clientHandler = new ClientHandler();
        clientHandler.setSessionId(sessionId);
        clientHandler.setTestType(testEvent);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("test.v5.cn", 19001))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65551, 12, 2, 2, 0, true))
                                    .addLast(new ProtocolDecoder())
                                    .addLast(new ProtocolEncoder())
                                    .addLast(clientHandler);
                        }
                    });

            ChannelFuture f = b.connect().sync();

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();

        }
    }

    public static void main(String[] args) throws Exception {

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new ClientTest("0487a30e4db042a0816c88f0e9da12ed", Events.LOGIN_GAME).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/

        /*Thread.sleep(10000);  */

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new ClientTest("188979c5149c47b49969271795b383cd", Events.LOGIN_GAME).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
