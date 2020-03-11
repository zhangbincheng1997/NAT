package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteProxyHandler extends SimpleChannelInboundHandler<byte[]> {

    private ChannelHandlerContext server2client;

    public RemoteProxyHandler(ChannelHandlerContext server2client) {
        this.server2client = server2client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("发送数据：{}", ctx.channel().id().asLongText());
        Message message = Message.of(MessageType.DATA, ctx.channel().id().asLongText(), msg);
        server2client.writeAndFlush(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立连接：{}", ctx.channel().id().asLongText());
        Message message = Message.of(MessageType.CONNECTED, ctx.channel().id().asLongText());
        server2client.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接：{}", ctx.channel().id().asLongText());
        Message message = Message.of(MessageType.DISCONNECTED, ctx.channel().id().asLongText());
        server2client.writeAndFlush(message);
    }
}
