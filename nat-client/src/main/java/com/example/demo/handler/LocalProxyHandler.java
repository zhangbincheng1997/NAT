package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.util.HashMap;

public class LocalProxyHandler extends MessageHandler {

    // ChannelHandlerContext
    private MessageHandler proxyHandler;
    private String remoteChannelId;

    public LocalProxyHandler(MessageHandler proxyHandler, String remoteChannelId) {
        this.proxyHandler = proxyHandler;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DATA);
        message.setChannelId(remoteChannelId);
        message.setData((byte[]) msg);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DISCONNECTED);
        message.setChannelId(remoteChannelId);
        message.setData(null);
        proxyHandler.getCtx().writeAndFlush(message);
    }
}
