package com.handwin.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * User: roger
 * Date: 13-12-13 上午11:15
 */
public class ProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf frame, List<Object> out) throws Exception {
        if(frame.readableBytes() >= 16) {
            out.add(Packet.parseFrom(frame));
        }
    }

}
