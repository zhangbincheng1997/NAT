package com.example.demo.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private static final int INT_SIZE = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // type
        int type = msg.readInt();
        // channel
        byte[] channel = null;
        if (msg.readableBytes() > INT_SIZE){
            int channelLength = msg.readInt();
            channel = new byte[channelLength];
            msg.readBytes(channel);
        }
        // data
        byte[] data = null;
        if (msg.readableBytes() > INT_SIZE){
            int dataLength = msg.readInt();
            data = new byte[dataLength];
            msg.readBytes(data);
        }

        Message message = new Message();
        message.setType(MessageType.valueOf(type));
        message.setChannelId(channel != null? new String(channel, StandardCharsets.UTF_8) : "");
        message.setData(data);

        out.add(message);

//        // type
//        int type = msg.readInt();
//        MessageType messageType = MessageType.valueOf(type);
//
//        // metaData
//        int metaDataLength = msg.readInt();
//        CharSequence metaDataString = msg.readCharSequence(metaDataLength, CharsetUtil.UTF_8); // readBytes() TODO
//        JSONObject metaData = JSONObject.parseObject(metaDataString.toString());
//
//        // data
//        byte[] data = null;
//        if (msg.isReadable()) {
//            data = ByteBufUtil.getBytes(msg);
//        }
//
//        Message message = new Message();
//        message.setType(messageType);
//        message.setMetaData(metaData);
//        message.setData(data);
//
//        out.add(message);
    }

}
