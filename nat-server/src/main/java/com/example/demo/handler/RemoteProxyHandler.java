package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.util.HashMap;

public class RemoteProxyHandler extends MessageHandler {

    // ChannelHandlerContext
    private MessageHandler proxyHandler;

    public RemoteProxyHandler(MessageHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DATA);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData((byte[]) msg);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Message message = new Message();
        message.setType(MessageType.CONNECTED);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(null);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DISCONNECTED);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(null);
        proxyHandler.getCtx().writeAndFlush(message);
    }
}
