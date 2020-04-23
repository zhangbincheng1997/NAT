package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalProxyHandler extends SimpleChannelInboundHandler<byte[]> {

    private ChannelHandlerContext client2Server;
    private String remoteChannelId;

    public LocalProxyHandler(ChannelHandlerContext client2Server, String remoteChannelId) {
        this.client2Server = client2Server;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // NONE
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("返回数据：id={}", remoteChannelId);
        Message message = Message.of(MessageType.DATA, remoteChannelId, msg);
        client2Server.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("返回结果：id={}", remoteChannelId);
        Message message = Message.of(MessageType.DISCONNECTED, remoteChannelId);
        client2Server.writeAndFlush(message);
    }
}
