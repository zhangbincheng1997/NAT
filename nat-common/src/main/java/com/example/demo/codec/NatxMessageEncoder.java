package com.example.demo.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import com.example.demo.protocol.NatxMessage;
import com.example.demo.protocol.NatxMessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Created by wucao on 2019/3/2.
 */
public class NatxMessageEncoder extends MessageToByteEncoder<NatxMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NatxMessage msg, ByteBuf out) throws Exception {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

            NatxMessageType natxMessageType = msg.getType();
            dataOutputStream.writeInt(natxMessageType.getCode());

            JSONObject metaDataJson = new JSONObject(msg.getMetaData());
            byte[] metaDataBytes = metaDataJson.toString().getBytes(CharsetUtil.UTF_8);
            dataOutputStream.writeInt(metaDataBytes.length);
            dataOutputStream.write(metaDataBytes);

            if (msg.getData() != null && msg.getData().length > 0) {
                dataOutputStream.write(msg.getData());
            }

            byte[] data = byteArrayOutputStream.toByteArray();
            out.writeInt(data.length);
            out.writeBytes(data);
        }

    }

}
