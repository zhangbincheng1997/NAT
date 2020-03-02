package codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import protocol.NatxMessage;
import protocol.NatxMessageType;

import java.util.List;
import java.util.Map;

/**
 * Created by wucao on 2019/3/2.
 */
public class NatxMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List out) throws Exception {

        int type = msg.readInt();
        NatxMessageType natxMessageType = NatxMessageType.valueOf(type);

        int metaDataLength = msg.readInt();
        CharSequence metaDataString = msg.readCharSequence(metaDataLength, CharsetUtil.UTF_8);
        JSONObject metaData = JSONObject.parseObject(metaDataString.toString());
//        Map<String, Object> metaData = jsonObject.toMap();

        byte[] data = null;
        if (msg.isReadable()) {
            data = ByteBufUtil.getBytes(msg);
        }

        NatxMessage natxMessage = new NatxMessage();
        natxMessage.setType(natxMessageType);
        natxMessage.setMetaData(metaData);
        natxMessage.setData(data);

        out.add(natxMessage);
    }

}
