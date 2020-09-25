# 内网穿透

![alt text](docs/flow.png)

## 启动
1. java -jar nat-server.jar [port, defalut 8888]
2. java -jar nat-client.jar

## 规则（示例）
1. 外网地址：http://www.littleredhat1997.com/
2. 内网地址：127.0.0.1
3. http://www.littleredhat1997.com:10000/ <=> 127.0.0.1:8080

| 机器 | 标识 | 端口（默认） |
| :---: | :---: | :---: |
| 外网代理 | Remote | 10000 |
| 服务端 | Server | 8888 |
| 客户端 | Client | - |
| 内网代理 | Local | 8080 |

## 自定义协议
`消息体` = `消息体总长度` + `TYPE` + `length(CHANNEL_ID)` + `CHANNEL_ID` + `length(DATA)` + `DATA`
```
private MessageType type;
private String channelId;
private byte[] data;

UNKNOWN(0),
REGISTER(1),
REGISTER_SUCCESS(2),
REGISTER_FAILURE(3),
CONNECTED(4),
DISCONNECTED(5),
DATA(6),
KEEPALIVE(7);
```

## 拆包粘包 LengthFieldBasedFrameDecoder
Parameters:
1. maxFrameLength - the maximum length of the frame. If the length of the frame is greater than this value, TooLongFrameException will be thrown.
2. lengthFieldOffset - the offset of the length field
3. lengthFieldLength - the length of the length field
4. lengthAdjustment - the compensation value to add to the value of the length field
5. initialBytesToStrip - the number of first bytes to strip out from the decoded frame

示例：
```
new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4)

public class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // TODO
    }
}

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // TODO
    }
}
```

## 心跳检测 IdleStateHandler
Parameters:
1. readerIdleTimeSeconds - an IdleStateEvent whose state is IdleState.READER_IDLE will be triggered when no read was performed for the specified period of time. Specify 0 to disable.
2. writerIdleTimeSeconds - an IdleStateEvent whose state is IdleState.WRITER_IDLE will be triggered when no write was performed for the specified period of time. Specify 0 to disable.
3. allIdleTimeSeconds - an IdleStateEvent whose state is IdleState.ALL_IDLE will be triggered when neither read nor write was performed for the specified period of time. Specify 0 to disable.

示例：
```
new IdleStateHandler(60, 30, 0)

@Override
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            // TODO
        } else if (e.state() == IdleState.WRITER_IDLE) {
            // TODO
        } else if (e.state() == IdleState.ALL_IDLE) {
            // TODO
        }
    }
}
```
