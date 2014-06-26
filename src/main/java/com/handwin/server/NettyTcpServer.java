package com.handwin.server;

import com.handwin.server.handler.MultiplexerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-6 下午3:57
 */
public abstract class NettyTcpServer implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(NettyTcpServer.class);

    @Value("${server.port}")
    private int port;

    @Value("${recv.buffer.size}")
    private int recvBufferSize;

    @Value("${send.buffer.size}")
    private int sendBufferSize;

    @Value("${backlog}")
    private int backlog;

    @Autowired
    private MultiplexerChannelInitializer pipelineFactory;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    @Override
    public void destroy() throws Exception {
        log.info("server stop.");
        workGroup.shutdownGracefully();
    }

    protected void startServer() {
        bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("NETTY-BOSS"));
        workGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("NETTY-WORKER"));

        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, recvBufferSize)
                    .option(ChannelOption.SO_SNDBUF, sendBufferSize)
                    .option(ChannelOption.SO_BACKLOG, backlog);

            bootstrap.childHandler(pipelineFactory);

            ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).sync();
            log.info("GAME Server start");
            f.channel().closeFuture().sync();

            initializeGameServer();
        }catch (InterruptedException e) {
            log.error("run error", e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startServer();
    }

    public abstract void initializeGameServer();
}
