package com.zzz.nat.common.codec;

import com.zzz.nat.common.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.zzz.nat.common.protocol.MessageType;

import java.util.List;

/**
 * 消息体 = 消息体总长度 + TYPE + length(CHANNEL_ID) + CHANNEL_ID + length(DATA) + DATA
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private static final int INT_SIZE = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int type = msg.readInt();

        byte[] channelId = null;
        if (msg.readableBytes() > INT_SIZE) {
            int channelIdLength = msg.readInt();
            channelId = new byte[channelIdLength];
            msg.readBytes(channelId);
        }

        byte[] data = null;
        if (msg.readableBytes() > INT_SIZE) {
            int dataLength = msg.readInt();
            data = new byte[dataLength];
            msg.readBytes(data);
        }

        Message message = Message.of(
                MessageType.valueOf(type),
                channelId != null ? new String(channelId) : null, // byte[] to String
                data);
        out.add(message);
    }

}
