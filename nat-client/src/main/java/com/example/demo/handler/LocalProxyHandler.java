package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import com.example.demo.protocol.NatxMessage;
import com.example.demo.protocol.NatxMessageType;

import java.util.HashMap;

/**
 * Created by wucao on 2019/3/2.
 */
public class LocalProxyHandler extends NatxCommonHandler {

    private NatxCommonHandler proxyHandler;
    private String remoteChannelId;

    public LocalProxyHandler(NatxCommonHandler proxyHandler, String remoteChannelId) {
        this.proxyHandler = proxyHandler;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] data = (byte[]) msg;
        NatxMessage message = new NatxMessage();
        message.setType(NatxMessageType.DATA);
        message.setData(data);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("channelId", remoteChannelId);
        message.setMetaData(metaData);
        proxyHandler.getCtx().writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NatxMessage message = new NatxMessage();
        message.setType(NatxMessageType.DISCONNECTED);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("channelId", remoteChannelId);
        message.setMetaData(metaData);
        proxyHandler.getCtx().writeAndFlush(message);
    }
}
