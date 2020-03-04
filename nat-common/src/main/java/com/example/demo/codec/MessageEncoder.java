package com.example.demo.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            // type
            MessageType messageType = msg.getType();
            dataOutputStream.writeInt(messageType.getCode());

            // metaData
            JSONObject metaDataJson = new JSONObject(msg.getMetaData());
            byte[] metaDataBytes = metaDataJson.toString().getBytes(CharsetUtil.UTF_8); // readBytes() TODO
            dataOutputStream.writeInt(metaDataBytes.length);
            dataOutputStream.write(metaDataBytes);

            // data
            if (msg.getData() != null && msg.getData().length > 0) {
                dataOutputStream.write(msg.getData());
            }

            // 封装
            byte[] data = byteArrayOutputStream.toByteArray();
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

}
