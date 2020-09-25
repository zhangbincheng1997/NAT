package com.example.demo.client.handler;

import com.example.demo.client.MainForm;
import io.netty.channel.ChannelHandlerContext;
import com.example.demo.common.protocol.Message;
import com.example.demo.common.protocol.MessageType;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalProxyHandler extends SimpleChannelInboundHandler<byte[]> {

    private ChannelHandlerContext client2Server;
    private String remoteProxyChannelId;

    public LocalProxyHandler(ChannelHandlerContext client2Server, String remoteProxyChannelId) {
        this.client2Server = client2Server;
        this.remoteProxyChannelId = remoteProxyChannelId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接成功：" + remoteProxyChannelId);
        MainForm.getInstance().showMessage("连接成功：" + remoteProxyChannelId);
        Message message = Message.of(MessageType.CONNECTED, remoteProxyChannelId);
        client2Server.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("转发成功：" + remoteProxyChannelId);
        MainForm.getInstance().showMessage("转发成功：" + remoteProxyChannelId);
        Message message = Message.of(MessageType.DATA, remoteProxyChannelId, msg);
        client2Server.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("请求关闭：" + remoteProxyChannelId);
        MainForm.getInstance().showMessage("请求关闭：" + remoteProxyChannelId);
        Message message = Message.of(MessageType.DISCONNECTED, remoteProxyChannelId);
        client2Server.writeAndFlush(message);
    }
}
