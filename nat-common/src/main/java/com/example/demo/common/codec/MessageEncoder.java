package com.example.demo.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.example.demo.common.protocol.Message;

/**
 * 消息体 = 消息体总长度 + TYPE + length(CHANNEL_ID) + CHANNEL_ID + length(DATA) + DATA
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final int INT_SIZE = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int type = msg.getType().getCode();
        byte[] data = msg.getData();
        String channelId = msg.getChannelId();

        int messageLength = INT_SIZE;
        if (data != null) {
            messageLength += INT_SIZE;
            messageLength += data.length;
        }
        if (channelId != null) {
            messageLength += INT_SIZE;
            messageLength += channelId.getBytes().length;
        }

        out.writeInt(messageLength);
        out.writeInt(type);
        if (channelId != null) {
            out.writeInt(channelId.getBytes().length);
            out.writeBytes(channelId.getBytes()); // String to byte[]
        }
        if (data != null) {
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

}
