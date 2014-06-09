package com.handwin.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handwin.event.Event;
import com.handwin.util.Jackson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

/**
 * User: roger
 * Date: 13-12-13 下午2:08
 */
public class ProtocolEncoder extends MessageToByteEncoder<Event> {
    private static ObjectMapper mapper = Jackson.newObjectMapper();
    @Override
    protected void encode(ChannelHandlerContext ctx, Event event, ByteBuf out) throws Exception {

        String data = mapper.writeValueAsString(event);
        byte[] value = data.getBytes(Charset.forName("utf-8"));

        out.writeByte(Packet.START);
        out.writeByte(1);
        out.writeByte(2);
        out.writeByte(0);
        out.writeLong(System.currentTimeMillis());
        out.writeShort(value.length + 1);
        out.writeByte(0);
        out.writeByte(Packet.END);
        out.writeByte(0x82);
        out.writeBytes(value);
    }
}
