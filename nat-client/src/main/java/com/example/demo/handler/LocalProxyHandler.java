package com.example.demo.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalProxyHandler extends SimpleChannelInboundHandler<byte[]> {

    private Channel client2Server;
    private String remoteChannelId;

    public LocalProxyHandler(Channel client2Server, String remoteChannelId) {
        this.client2Server = client2Server;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("发送数据：{}", remoteChannelId);
        Message message = Message.of(MessageType.DATA, remoteChannelId, msg);
        client2Server.writeAndFlush(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // NONE
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Message message = Message.of(MessageType.DISCONNECTED, remoteChannelId);
        log.info("断开连接：{}", remoteChannelId);
        client2Server.writeAndFlush(message);
    }
}
