package com.example.demo.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // type
        int type = msg.readInt();
        MessageType messageType = MessageType.valueOf(type);

        // metaData
        int metaDataLength = msg.readInt();
        CharSequence metaDataString = msg.readCharSequence(metaDataLength, CharsetUtil.UTF_8); // readBytes() TODO
        JSONObject metaData = JSONObject.parseObject(metaDataString.toString());

        // data
        byte[] data = null;
        if (msg.isReadable()) {
            data = ByteBufUtil.getBytes(msg);
        }

        Message message = new Message();
        message.setType(messageType);
        message.setMetaData(metaData);
        message.setData(data);

        out.add(message);
    }

}
