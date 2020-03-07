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

    // 不需要channelActive

    // 此方法会在接收到服务器数据后调用
    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DATA);
        message.setChannelId(remoteChannelId);
        message.setData(msg);
        log.info("接受数据：客户端->代理客户端->服务端 {}", remoteChannelId);
        client2Server.writeAndFlush(message);// 直接转发回服务端
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Message message = new Message();
        message.setType(MessageType.DISCONNECTED);
        message.setChannelId(remoteChannelId);
        message.setData(null);
        log.info("断开连接：客户端->代理客户端->服务端 {}", remoteChannelId);
        client2Server.writeAndFlush(message);
    }
}
