package com.handwin.server.handler;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * User: roger
 * Date: 13-12-13 上午11:17
 */
public class Packet {
    private static Logger LOG = LoggerFactory.getLogger(Packet.class);

    public static final byte START = (byte)0xb7;
    public static final byte END = (byte)0xa5;
    private byte start = START;
    private byte version;
    private byte packetType;
    private byte secret;
    private long timestamp;
    private int packetSize;
    private byte padding;
    private byte end = END;
    private byte cmd;
    private byte[]  data;

    public byte getStart() {
        return start;
    }

    public void setStart(byte start) {
        this.start = start;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public byte getSecret() {
        return secret;
    }

    public void setSecret(byte secret) {
        this.secret = secret;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public byte getPadding() {
        return padding;
    }

    public void setPadding(byte padding) {
        this.padding = padding;
    }

    public byte getEnd() {
        return end;
    }

    public void setEnd(byte end) {
        this.end = end;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public static Packet parseFrom(ByteBuf frame) {

        Packet packet = new Packet();

        packet.start = frame.readByte();
        packet.version = frame.readByte();
        packet.packetType = frame.readByte();
        packet.secret = frame.readByte();
        packet.timestamp = frame.readLong();
        packet.packetSize = frame.readUnsignedShort();
        packet.padding = frame.readByte();
        packet.end = frame.readByte();



        if(frame.readableBytes() != packet.packetSize) {
            LOG.warn("frame buf readable bytes:{},packet.packet_size={}", frame.readableBytes(), packet.packetSize);
        }

        packet.cmd = frame.readByte(); //ignore the game command

        packet.data = new byte[packet.packetSize - 1];
        frame.readBytes(packet.data);

        return packet;

    }

    @Override
    public String toString() {
        return "Packet{" +
                "start=" + start +
                ", version=" + version +
                ", packetType=" + packetType +
                ", secret=" + secret +
                ", timestamp=" + timestamp +
                ", packetSize=" + packetSize +
                ", padding=" + padding +
                ", end=" + end +
                ", cmd=" + cmd +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
