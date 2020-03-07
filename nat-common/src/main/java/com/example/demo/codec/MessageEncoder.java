package com.example.demo.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.example.demo.protocol.Message;

/**
 * 自定义协议：
 * 报文 = 报文总长度 + Type + CHANNEL_ID长度 + CHANNEL_ID + DATA长度 + DATA
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final int TYPE_SIZE = 4;
    private static final int CHANNEL_ID_SIZE = 4;
    private static final int DATA_LENGTH_SIZE = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        //messageLength记录了总长度
        int messageLength = TYPE_SIZE + CHANNEL_ID_SIZE;

        // data
        byte[] data = msg.getData();
        if (data != null){
            int dataLength = data.length;
            messageLength += DATA_LENGTH_SIZE;
            messageLength += dataLength;
        }

        // channelId
        String channelId = msg.getChannelId();
        if (msg.getChannelId() != null){
            byte[] channelIdBytes = channelId.getBytes();
            messageLength += channelIdBytes.length;
        }

        out.writeInt(messageLength);
        int type = msg.getType().getCode();
        out.writeInt(type);
        if (msg.getChannelId() != null){
            byte[] channelIdBytes = channelId.getBytes();
            out.writeInt(channelIdBytes.length);
            out.writeBytes(channelIdBytes);
        }
        if (data != null){
            out.writeInt(data.length);
            out.writeBytes(data);
        }

//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try (DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
//            // type
//            MessageType messageType = msg.getType();
//            dataOutputStream.writeInt(messageType.getCode());
//
//            // metaData
//            JSONObject metaDataJson = new JSONObject(msg.getMetaData());
//            byte[] metaDataBytes = metaDataJson.toString().getBytes(CharsetUtil.UTF_8); // readBytes() TODO
//            dataOutputStream.writeInt(metaDataBytes.length);
//            dataOutputStream.write(metaDataBytes);
//
//            // data
//            if (msg.getData() != null && msg.getData().length > 0) {
//                dataOutputStream.write(msg.getData());
//            }
//
//            // 封装
//            byte[] data = byteArrayOutputStream.toByteArray();
//            out.writeInt(data.length);
//            out.writeBytes(data);
//        }
    }

}
