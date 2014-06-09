package com.handwin.server.handler;

import com.handwin.game.GameSessionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-9 上午10:08
 */
public class MultiplexerChannelInitializer extends ChannelInitializer<SocketChannel> implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(MultiplexerChannelInitializer.class);

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        log.debug("new SocketChannel coming");

        ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(120, 0,0));
        ch.pipeline().addLast("logging", new LoggingHandler());
        ch.pipeline().addLast("frame_decoder", new LengthFieldBasedFrameDecoder(65535, 12, 2, 2, 0, true));
        ch.pipeline().addLast("protocol_decoder", new ProtocolDecoder());
        ch.pipeline().addLast("handler", createServerEventHandler());
        ch.pipeline().addLast("frame_encoder", new ProtocolEncoder());
    }

    protected ServerEventHandler createServerEventHandler() {
        ServerEventHandler handler = new ServerEventHandler();

        handler.setGameSessionManager(context.getBean("gameSessionManager", GameSessionManager.class));

        handler.setEventHandlerFactory(context.getBean("eventHandlerFactory", EventHandlerFactory.class));

        return handler;
    }
}