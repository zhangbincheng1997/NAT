package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j // 10000 port
public class RemoteProxyHandler extends SimpleChannelInboundHandler<byte[]> {

    private ChannelHandlerContext server2client;

    public RemoteProxyHandler(ChannelHandlerContext server2client) {
        this.server2client = server2client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("发送数据：服务器->代理服务器->客户端 {}", ctx.channel().id().asLongText());
        Message message = new Message();
        message.setType(MessageType.DATA);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(msg);
        server2client.writeAndFlush(message); // 直接发送到客户端
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立连接：服务器->代理服务器->客户端 {}", ctx.channel().id().asLongText());
        Message message = new Message();
        message.setType(MessageType.CONNECTED);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(null);
        server2client.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接：服务器->代理服务器->客户端 {}", ctx.channel().id().asLongText());
        Message message = new Message();
        message.setType(MessageType.DISCONNECTED);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(null);
        server2client.writeAndFlush(message);
    }
}
